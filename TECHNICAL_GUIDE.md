# Pet Store API - Teknik Yapım Kılavuzu

Bu doküman, `PetStoreApplication` projesinin benzerini sıfırdan oluşturmak, mimari kararları anlamak ve kod okuryazarlığını artırmak isteyen geliştiriciler için **Adım Adım Yapım Kılavuzudur.**

---

## 1. İlk Adım: Projenin Doğuşu (Spring Initializr)
Proje sıfırdan **[start.spring.io](https://start.spring.io/)** üzerinden başlatıldı.
- **Proje Tipi:** Maven
- **Dil:** Java 21
- **Spring Boot Sürümü:** 3.5.x
- **Seçilen Bağımlılıklar (Dependencies):** Spring Web, Spring Data JPA, PostgreSQL Driver, Validation, Spring Security, Lombok.
  *(Not: JWT olan `jjwt-api` ve Swagger UI olan `springdoc-openapi-starter-webmvc-ui` kütüphaneleri daha sonradan `pom.xml` içerisine manuel olarak eklendi.)*

---

## Genel Mimari

Proje klasik N-Tier (Çok Katmanlı) mimariye sahiptir. Her katman yalnızca bir altındaki katmanla konuşur.

- Security Layer — gelen her isteği filtreler
- Controller Layer — HTTP isteklerini karşılar, route yönetimi
- Service Layer — iş kuralları (Business Logic)
- Repository Layer — veritabanı sorguları
- Entity Layer — veritabanı tablo modelleri

---

## 0. Bootstrap Katmanı

### PetStoreApplication
- Uygulamanın giriş noktasıdır.
- `@SpringBootApplication` zorunludur (otomatik konfigürasyon + component scan).
- `main(String[] args)` — `SpringApplication.run()` ile Spring konteyneri başlatılır.

### DataInitializer
- `implements CommandLineRunner` — Spring Boot ayağa kalktıktan hemen sonra `run()` çalışır.
- `run(String... args)` — `Role` enum değerlerini (ADMIN, STORE_OWNER, CUSTOMER, USER) döner. Her biri için veritabanında kayıt var mı kontrol eder, yoksa `UserRole` oluşturup kaydeder.
- İdempotent çalışır: birden fazla kez çalıştırılması zararsızdır.
- Bu sınıf olmadan `AuthService.register()` çalışırken `EntityNotFoundException` fırlar.

---

## 1. Entity Katmanı

`@Entity` zorunludur. `spring.jpa.hibernate.ddl-auto=update` ayarıyla JPA tabloları otomatik oluşturur.

### User — Tablo: ps_user
- `id` (Long, @GeneratedValue — Primary Key, otomatik artar)
- `username` (String, unique=true, length=32, nullable=false)
- `email` (String, unique=true, nullable=false)
- `password` (String, nullable=false — BCrypt ile hashlenmiş olarak kaydedilir, ham şifre tutulmaz)
- `firstName` (String, nullable)
- `lastName` (String, nullable)
- `roles` (Set&lt;UserRole&gt;, @ManyToMany — Set seçilmiştir çünkü aynı rol iki kez atanamaz)
- `ownedPets` (List&lt;Pet&gt;, @OneToMany mappedBy="owner")
- Köprü tablosu: `ps_user_role` (user_id ve role_id sütunları)

### UserRole — Tablo: ps_roles_user
- `id` (Long, @GeneratedValue — Primary Key)
- `role` (Role enum, @Enumerated(EnumType.STRING) — STRING zorunludur; ORDINAL kullanılsaydı enum sırası değişince tüm veriler bozulurdu)

### Store — Tablo: ps_store
- `id` (Long, @GeneratedValue — Primary Key)
- `name` (String, unique=true, nullable=false)
- `owner` (User, @ManyToOne, @JoinColumn(name="owner_id"))
- `pets` (List&lt;Pet&gt;, @OneToMany mappedBy="store")

### Pet — Tablo: ps_pet
- `id` (Long, @GeneratedValue — Primary Key)
- `name` (String, nullable=false)
- `age` (Integer, nullable=false)
- `price` (Double, nullable=false)
- `type` (AnimalType enum, @Enumerated(EnumType.STRING))
- `gender` (Gender enum, @Enumerated(EnumType.STRING))
- `store` (Store, @ManyToOne, @JoinColumn(name="store_id"))
- `owner` (User, @ManyToOne, @JoinColumn(name="owner_id") — DTO'dan alınmaz; servis katmanı JWT'den otomatik tespit eder)

---

## 2. Enum Katmanı

### Role
- ADMIN, STORE_OWNER, CUSTOMER, USER

### AnimalType
- CAT, DOG, BIRD, ...

### Gender
- MALE, FEMALE

---

## 3. Repository Katmanı

`JpaRepository<Entity, IdTipi>` extend edilir. `save`, `findById`, `findAll`, `delete` gibi metotlar hazır gelir. Özel sorgular Method Naming Convention veya `@Query` (JPQL) ile yazılır.

### UserRepository — extends JpaRepository&lt;User, Long&gt;
- `findByUsername(String username)` — Optional&lt;User&gt; döner. JWT filtresinde kullanıcı yüklemek için.
- `existsByUsernameIgnoreCaseOrEmail(String username, String email)` — Boolean döner. Kayıt öncesi benzersizlik kontrolü; IgnoreCase eki büyük/küçük harf duyarsız sorgular.
- `findByUsernameOrEmail(String username, String email)` — Optional&lt;User&gt; döner. Login'de hem kullanıcı adı hem e-posta kabul edilmesi için.

### UserRoleRepository — extends JpaRepository&lt;UserRole, Long&gt;
- `findByRole(Role role)` — Optional&lt;UserRole&gt; döner. DataInitializer ve AuthService tarafından kullanılır.

### StoreRepository — extends JpaRepository&lt;Store, Long&gt;
- `findByOwner(User owner)` — List&lt;Store&gt; döner. Bir kullanıcıya ait mağazaları getirir.
- `existsByName(String name)` — Boolean döner. Mağaza adı benzersizlik kontrolü.

### PetRepository — extends JpaRepository&lt;Pet, Long&gt;
- `existsByNameAndStoreId(String name, Long storeId)` — Boolean döner. Aynı mağazada aynı isimde hayvan var mı kontrolü.
- `findByStoreId(Long storeId)` — List&lt;Pet&gt; döner. Belirli mağazanın hayvanları.
- `findAllPetsWithFilters(Gender gender, AnimalType type, Pageable pageable)` — Page&lt;Pet&gt; döner. `@Query` ile yazılmış dinamik JPQL sorgusu.
  - `(:gender IS NULL OR p.gender = :gender) AND (:type IS NULL OR p.type = :type)` yapısı sayesinde parametre gönderilmezse o filtre otomatik devre dışı kalır.

---

## 4. DTO Katmanı

Entity doğrudan istemciye açılmaz. DTO kullanılması şifre ve hassas alanların sızmasını önler, validation kurallarını merkezileştirir.

### UserRegisterRequestDTO
- `username` (String, @NotBlank, @Size(min=3, max=32))
- `email` (String, @NotBlank, @Email)
- `password` (String, @NotBlank, @Size(min=8, max=64) — servis katmanı BCrypt ile hashler)
- `isStoreOwner` (Boolean, @NotNull — true ise STORE_OWNER, false ise CUSTOMER rolü atanır)

### UserLoginRequestDTO
- `login` (String — kullanıcı adı ya da e-posta kabul edilir)
- `password` (String — ham şifre)

### StoreCreateRequestDTO
- `name` (String, @NotBlank, @Size(min=2, max=50))

### PetRequestDTO
- `name` (String, @NotBlank, @Size(min=2, max=50))
- `age` (Integer, @NotNull, @Min(0) — negatif olamaz)
- `price` (Double, @NotNull, @Positive — sıfır dahil negatif olamaz)
- `type` (AnimalType, @NotNull)
- `gender` (Gender, @NotNull)
- `storeId` (Long, @NotNull — owner DTO'dan alınmaz; JWT'den otomatik tespit edilir)

### PetResponseDTO
- `id`, `name`, `age`, `price`, `type`, `gender`, `storeId`, `storeName`, `ownerUsername`

### StoreResponseDTO
- `id`, `name`, `ownerUsername`

---

## 5. Exception Katmanı

### ApiErrorResponse
- `@Builder` ile inşa edilir.
- `timestamp` (LocalDateTime, format: "yyyy-MM-dd HH:mm:ss")
- `status` (int — HTTP durum kodu)
- `error` (String — "Forbidden", "Not Found" vb.)
- `message` (String — hatayı açıklayan mesaj)
- `path` (String — isteğin atıldığı URL)
- `validationErrors` (Map&lt;String, String&gt;, @JsonInclude(NON_NULL) — yalnızca validation hatası varsa dolu olur)

### GlobalExceptionHandler
- `@RestControllerAdvice` — tüm Controller'lardan fırlayan exception'ları tek noktada yakalar.
- Tüm handler metotları ortak `buildErrorResponse(ex, status, request)` private metoduna iş bırakır.
- `handleEntityNotFound` → EntityNotFoundException → 404 Not Found
- `handleUniqueViolation` → UniquePropertyViolationException → 409 Conflict
- `handleInvalidPassword` → InvalidPasswordException → 400 Bad Request
- `handleAccessDenied` → AccessDeniedException → 403 Forbidden
- `handleValidationErrors` → MethodArgumentNotValidException → 400 Bad Request (validationErrors map'ini doldurur)
- `handleNotReadable` → HttpMessageNotReadableException → 400 Bad Request
- `handleGeneral` → Exception → 500 Internal Server Error

### Özel Exception Sınıfları
- `UniquePropertyViolationException` (extends RuntimeException — aynı isimde kayıt var)
- `InvalidPasswordException` (extends RuntimeException — şifre yanlış)

---

## 6. Security Katmanı

### JWTUtils — @Component
- `@Value("${app.jwtSecret}")` ve `@Value("${app.jwtExpiartionMs}")` ile application.properties'ten değerler enjekte edilir.
- `getSigningKey()` (private) — jwtSecret string'inden HMAC-SHA256 SecretKey üretir.
- `generateJWT(Authentication authentication)` — Kullanıcının username'ini subject olarak içeren imzalı JWT oluşturur; üretim ve bitiş tarihi eklenerek compact() ile string'e dönüştürülür.
- `validateJWT(String jwt)` — Token parse edilmeye çalışılır; JwtException veya IllegalArgumentException fırlarsa false döner, başarılıysa true döner.
- `extractUsernameFromJWT(String jwt)` — Token'ın payload.subject alanından username okur.

### JWTAuthenticationFilter — extends OncePerRequestFilter
- Bir istek için tam olarak bir kez çalışır.
- `doFilterInternal(request, response, filterChain)` — Ana filtre metodu.
  - `extractJWTFromRequest()` ile token alınır.
  - Token geçerliyse `extractUsernameFromJWT()` ile kullanıcı adı çıkarılır.
  - `userDetailsService.loadUserByUsername()` ile UserDetails yüklenir.
  - `UsernamePasswordAuthenticationToken` oluşturulup SecurityContextHolder'a yerleştirilir.
  - `filterChain.doFilter()` ile istek bir sonraki filtreye iletilir.
- `shouldNotFilter(request)` — /auth/register ve /auth/login için true döner; bu iki path filtreyi atlar.
- `extractJWTFromRequest(request)` (private) — Authorization header okunur; "Bearer " ile başlıyorsa ilk 7 karakter kırpılır ve saf token döner.

### UserDetailsImpl — implements UserDetails
- Spring Security kendi User sınıfını tanımaz; bu sınıf adaptör görevi üstlenir.
- Alanlar: `id`, `username`, `email`, `password`, `grantedAuthorities` (Set&lt;GrantedAuthority&gt;)
- `build(User user)` (static) — User entity'sini alır; roles setini SimpleGrantedAuthority'ye çevirerek yeni UserDetailsImpl nesnesi döner.
- `getAuthorities()` — grantedAuthorities koleksiyonunu döner; @PreAuthorize bu metodu kullanır.
- `isAccountNonExpired()`, `isAccountNonLocked()`, `isCredentialsNonExpired()`, `isEnabled()` — şimdilik hepsi true döner.

### UserDetailsServiceImpl — implements UserDetailsService, @Service
- `loadUserByUsername(String username)` (@Transactional, override) — UserRepository.findByUsername() ile kullanıcı çekilir. Bulunamazsa UsernameNotFoundException fırlatılır. Bulunan User, UserDetailsImpl.build(user) ile dönüştürülür. @Transactional zorunludur; aksi hâlde Lazy Loading'de LazyInitializationException fırlar.

### WebSecurityConfig — @Configuration, @EnableWebSecurity, @EnableMethodSecurity
- `filterChain(HttpSecurity http)` — CSRF kapatılır, session STATELESS yapılır, herkese açık path'ler belirlenir (/auth/login, /auth/register, /error, /swagger-ui/**, /v3/api-docs/**), diğer tüm istekler authenticated() gerektirir, JWTAuthenticationFilter zincire UsernamePasswordAuthenticationFilter'dan önce eklenir.
- `passwordEncoder()` — BCryptPasswordEncoder(strength=12) döner; strength=12 brute-force saldırılarına dayanıklıdır.
- `authenticationProvider()` — DaoAuthenticationProvider döner; UserDetailsServiceImpl ve BCryptPasswordEncoder birleştirilir.
- `authenticationManager(AuthenticationConfiguration)` — AuthService.login() içindeki authenticate() çağrısı için gereklidir.
- `corsConfigurationSource()` — Tüm origin'lere izin verir; GET, POST, PUT, DELETE, OPTIONS metotları ve Authorization, Content-Type header'ları kabul edilir.

### AuthEntryPoint — implements AuthenticationEntryPoint
- Token olmadan korunan endpoint'e erişilince tetiklenir ve 401 Unauthorized yanıtı yazar.

---

## 7. Service Katmanı

İş kuralları yalnızca bu katmanda yazılır. Controller asla iş mantığı içermez.

### UserService — @Service
- `checkUniquePropertyViolations(String username, String email)` — UserRepository.existsByUsernameIgnoreCaseOrEmail() çağırır, Boolean döner.
- `save(User user)` — UserRepository.save() çağırır, kaydedilen User döner.
- `findByUsernameOrEmail(String login)` — Kullanıcıyı döner; bulunamazsa EntityNotFoundException fırlatır.

### UserRoleService — @Service
- `findAllUserRoles()` — Tüm rolleri listeler, List&lt;UserRole&gt; döner.
- `findUserRoleByRole(Role role)` — Belirli rolü döner; bulunamazsa EntityNotFoundException fırlatır. DataInitializer çalışmadan bu metot çağrılamaz.

### AuthService — @Service

`register(UserRegisterRequestDTO dto)`
- Kullanıcı adı veya e-posta alınmışsa UniquePropertyViolationException fırlatır.
- Şifreyi passwordEncoder.encode() ile hashler.
- isStoreOwner değerine göre STORE_OWNER veya CUSTOMER rolü atar.
- userService.save() ile kaydeder.
- `{"message": "...", "username": "..."}` döner; JWT dönmez, kullanıcı /auth/login ile ayrıca giriş yapmalıdır.

`login(UserLoginRequestDTO dto)`
- Kullanıcıyı bulur; bulunamazsa EntityNotFoundException fırlatır.
- passwordEncoder.matches() ile şifre doğrular; yanlışsa InvalidPasswordException fırlatır.
- authenticationManager.authenticate() ile Spring Security doğrulama akışını çalıştırır.
- jwtUtils.generateJWT() ile JWT üretir.
- `{"message": "...", "jwt": "..."}` döner.

### StoreService — @Service

Private yardımcı metotlar:
- `getCurrentUsername()` — SecurityContextHolder.getContext().getAuthentication().getName() ile giriş yapmış kullanıcının adını okur.
- `toResponseDTO(Store store)` — Ham Store entity'sini StoreResponseDTO'ya çevirir.

`createStore(StoreCreateRequestDTO dto)`
- Aynı isimde mağaza varsa UniquePropertyViolationException fırlatır.
- getCurrentUsername() ile sahibi belirler, owner olarak atar ve kaydeder.

`getAllStores()` — Güvenlik kuralı yoktur; giriş yapmış herkes görebilir.

`getStoreById(Long id)` — Bulunamazsa EntityNotFoundException fırlatır.

`updateStore(Long id, StoreCreateRequestDTO dto)`
- Mağaza yoksa EntityNotFoundException fırlatır.
- getCurrentUsername() sahibiyle eşleşmiyorsa AccessDeniedException (403) fırlatır.
- Yeni isim başkası tarafından kullanılıyorsa UniquePropertyViolationException (409) fırlatır.

`deleteStore(Long id)`
- Mağaza yoksa EntityNotFoundException fırlatır.
- getCurrentUsername() sahibiyle eşleşmiyorsa AccessDeniedException (403) fırlatır.

### PetService — @Service

Private yardımcı metotlar:
- `getCurrentUsername()` — StoreService ile aynı mantık.
- `toResponseDTO(Pet pet)` — pet.getStore() ve pet.getOwner() üzerinden mağaza adı ve sahip bilgisini güvenli şekilde DTO'ya doldurur.

`createPet(PetRequestDTO dto)`
- Mağaza yoksa EntityNotFoundException fırlatır.
- getCurrentUsername() mağaza sahibiyle eşleşmiyorsa AccessDeniedException (403) fırlatır.
- Aynı mağazada aynı isimde hayvan varsa UniquePropertyViolationException (409) fırlatır.
- owner JWT'den tespit edilir, DTO'dan alınmaz.

`getAllPets(Gender gender, AnimalType type, Pageable pageable)` — Page&lt;PetResponseDTO&gt; döner. Her iki filtre de opsiyoneldir.

`getPetById(Long id)` — Bulunamazsa EntityNotFoundException fırlatır.

`getPetsByStoreId(Long storeId)` — List&lt;PetResponseDTO&gt; döner.

`updatePet(Long id, PetRequestDTO dto)`
- Hayvan yoksa EntityNotFoundException fırlatır.
- Sahibi değilse AccessDeniedException (403) fırlatır.
- İsim veya mağaza değiştiyse benzersizlik kontrolü, UniquePropertyViolationException (409) riski.
- Mağaza değiştiyse yeni mağaza yoksa EntityNotFoundException; yeni mağazanın sahibi de değilse AccessDeniedException (403) fırlatır.

`deletePet(Long id)`
- Hayvan yoksa EntityNotFoundException fırlatır.
- Sahibi değilse AccessDeniedException (403) fırlatır.

---

## 8. Controller Katmanı

HTTP isteklerini karşılar ve Service'e delege eder. İş mantığı içermez.
- `@RestController` — dönüş değerleri otomatik JSON'a çevrilir.
- `@Valid` — DTO'daki @NotBlank, @Size vb. kuralları tetikler.
- `@PreAuthorize` — metot çalışmadan Spring Security rolü kontrol eder; uygunsuzsa 403 döner.

### AuthController — /auth (herkese açık)
- `POST /auth/register` — 201 Created döner.
- `POST /auth/login` — 200 OK döner.

### StoreController — /store (JWT zorunlu)
- `POST /store` — @PreAuthorize("hasAuthority('STORE_OWNER')") — 201 Created döner.
- `GET /store` — 200 OK döner.
- `GET /store/{id}` — 200 OK döner.
- `PUT /store/{id}` — STORE_OWNER + mağaza sahibi — 200 OK döner.
- `DELETE /store/{id}` — STORE_OWNER + mağaza sahibi — 204 No Content döner.

### PetController — /pet (JWT zorunlu)
- `POST /pet` — STORE_OWNER + mağaza sahibi — 201 Created döner.
- `GET /pet` — query params: gender, type, page, size, sort — 200 OK döner.
- `GET /pet/{id}` — 200 OK döner.
- `GET /pet/store/{storeId}` — 200 OK döner.
- `PUT /pet/{id}` — STORE_OWNER + hayvan sahibi — 200 OK döner.
- `DELETE /pet/{id}` — STORE_OWNER + hayvan sahibi — 204 No Content döner.
- Sayfalama örneği: `GET /pet?page=1&size=5&sort=price,desc&gender=MALE&type=CAT`

---

## 9. Config Katmanı

### OpenApiConfig — @Configuration
- Metot içermez; sınıf düzeyinde anotasyonlarla Swagger'ı konfigüre eder.
- `@OpenAPIDefinition(info = @Info(...))` — Swagger UI başlığı ve açıklaması.
- `@SecurityRequirement(name = "bearerAuth")` — Tüm endpoint'lere global JWT zorunluluğu ekler.
- `@SecurityScheme(type=HTTP, scheme="bearer", bearerFormat="JWT")` — Swagger UI'a "Authorize" butonu ekler; token girilince Authorization: Bearer &lt;token&gt; header'ı otomatik eklenir.
- Swagger UI adresi: `http://localhost:8080/swagger-ui.html`

---

## 10. Test Katmanı

Tüm testler `@ExtendWith(MockitoExtension.class)` ile çalışır. Gerçek veritabanı yerine mock nesneler kullanılır.

SecurityContext Taklit Deseni (her test sınıfında private yardımcı metot olarak tekrar eder):
- `Authentication auth = mock(Authentication.class)` oluşturulur.
- `when(auth.getName()).thenReturn(username)` ile sahte kullanıcı adı atanır.
- SecurityContextHolder'a set edilir.
- Bu yöntemle gerçek HTTP isteği veya JWT olmadan "bu kullanıcı giriş yapmış" senaryosu canlandırılır.

### UserServiceTest — 3 test
- Repository true dönünce servisin de true döndüğünü doğrular.
- Kullanıcı bulununca doğru nesnenin döndüğünü doğrular.
- Repository Optional.empty() dönünce EntityNotFoundException fırladığını doğrular.

### AuthServiceTest — 4 test
- Çakışma varsa UniquePropertyViolationException fırlar ve save() hiç çağrılmaz.
- Her şey uygunsa save() çağrılır ve username dönüş değerinde bulunur.
- Şifre eşleşmiyorsa InvalidPasswordException fırlar.
- Geçerli girişte jwt anahtarı dönüş map'inde bulunur.

### StoreServiceTest — 5 test
- İsim alınmışsa UniquePropertyViolationException; userService hiç çağrılmaz.
- Mağaza doğru owner ile kaydedilir ve DTO döner.
- mockSecurityContext("hacker") ile sahte sahiplik testi; AccessDeniedException beklenir.
- Olmayan mağaza için EntityNotFoundException beklenir.
- Gerçek sahibi sildiğinde repository.delete() çağrıldığı doğrulanır.

### PetServiceTest — 5 test
- Olmayan mağaza ID'si ile EntityNotFoundException beklenir.
- mockSecurityContext("hacker") ile yabancı mağazaya ekleme denemesi; AccessDeniedException beklenir.
- Aynı mağazada aynı isimde hayvan varsa UniquePropertyViolationException beklenir.
- Happy path testi; tüm alanların doğru DTO'ya eşlendiği assertEquals ile doğrulanır.
- PageImpl ile mock sayfa döndürülür; sonuç boyutu ve ID'si doğrulanır.

---

## 11. Konfigürasyon Dosyaları

### src/main/resources/application.properties
- `server.port=8080`
- `spring.datasource.url` — PostgreSQL bağlantı adresi
- `spring.datasource.username` ve `spring.datasource.password` — veritabanı kimlik bilgileri
- `spring.jpa.hibernate.ddl-auto=update` — tablo şemasını günceller; create-drop kullanılsaydı her restart'ta tüm veri silinirdi
- `app.jwtSecret` — HMAC-SHA256 imzalama anahtarı (minimum 32 karakter; üretimde gizli tutulmalı)
- `app.jwtExpiartionMs=86400000` — 24 saat
- `logging.file.name=log/pet_app.log`

### src/test/resources/application.properties
- Testlerde PostgreSQL yerine bellek içi H2 kullanılır. Bu dosya olmadan Spring Boot test context yüklenirken gerçek PostgreSQL'e bağlanmaya çalışır ve hata alınır.
- `spring.datasource.url=jdbc:h2:mem:testdb`
- `app.jwtSecret=TestSecretKey1234567890TestSecretKey1234567890`
