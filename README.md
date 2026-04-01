# 🐾 Pet Store API Application

Modern Web Teknolojileri (**Spring Boot 3 & Java 21**) kullanılarak geliştirilmiş, uçtan uca güvenli, yüksek performanslı ve tam donanımlı bir **Pet Store (Evcil Hayvan Mağazası) API** projesidir.

## 📖 Proje Ne Yapar? (Kapsamı Nedir?)
Bu proje, geliştiricilerin modern bir e-ticaret/yönetim backend sisteminin nasıl kurgulanması gerektiğini anlaması için tasarlanmış profesyonel bir altyapıdır. Sistem üç ana yetki üzerine (Müşteri, Mağaza Sahibi ve Admin) kurgulanmıştır.

- **Kullanıcılar:** Sisteme kayıt olabilir, JWT tabanlı güvenli oturum (login) açabilirler.
- **Mağaza Sahipleri (`STORE_OWNER`):** Kendi dükkanlarını açabilir, dükkanlarına evcil hayvan (kedi, köpek, kuş vb.) ekleyebilir, fiyat, yaş ve cinsiyet gibi özelliklerini yönetebilir. Başkasının mağazasına müdahale edemezler.
- **Güvenli Geçişler:** Tüm istekler güçlü bir Güvenlik Filtresinden (Spring Security & JWT) geçerek kullanıcıyı anında tanır.
- **Akıllı Arama:** Veriler binlerce kayda karşı "Sayfalama" (Pagination) ve "Filtreleme" (örn. Sadece Erkek Kediler) özellikleri ile optimum hızda sunulur.

## 🛠️ Kullanılan Teknolojiler
Bu proje sektör standartlarına tamamen uygun şekilde, en güncel teknolojilerle derlenmiştir:
- **Core:** Java 21, Spring Boot 3.5.x
- **Veritabanı:** PostgreSQL (Canlı), H2 Database (Test)
- **Güvenlik:** Spring Security 6, JSON Web Tokens (JWT)
- **Veri Erişimi:** Spring Data JPA, Hibernate
- **Dokümantasyon:** Swagger / OpenAPI 3 (Springdoc 2.8.x)
- **Test ve Kalite:** JUnit 5, Mockito (18+ Kapsamlı Otomatik Test)
- **Yardımcı Araçlar:** Lombok (Boilerplate azaltmak için), Maven

## 🚀 Kurulum ve Çalıştırma

Projeyi kendi bilgisayarınızda ayağa kaldırmak oldukça basittir:

### Ön Gereksinimler
* Sisteminizde **Java 21** veya üstü bir sürüm kurulu olmalıdır.
* Bilgisayarınızda **PostgreSQL** sunucusu açık olmalı ve `pet_store_db` adında boş bir veritabanı bulunmalıdır.

### Adım Adım Kurulum

1. **Projeyi Klonlayın:**
   ```bash
   git clone https://github.com/KULLANICI_ADINIZ/PetStoreApplication.git
   cd PetStoreApplication
   ```

2. **Veritabanı Şifrenizi Ayarlayın:**
   `src/main/resources/application.properties` dosyasına giderek kendi PostgreSQL bilgilerinizi (`techpront` ve `password` alanlarını) sisteminize göre güncelleyin.
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/pet_store_db
   spring.datasource.username=SİZİN_DB_KULLANICI_ADINIZ
   spring.datasource.password=SİZİN_DB_ŞİFRENİZ
   ```

3. **Çalıştırın:**
   Maven aracı sayesinde ekstra kütüphane kurmanıza gerek kalmaz, tek emirle projeyi derleyip çalıştırabilirsiniz:
   ```bash
   mvn spring-boot:run
   ```

4. **Kullanıma Başlayın (Swagger):**
   Proje `8080` portunda ayağa kalkacaktır. Tarayıcınızı açıp aşağıdaki adrese giderseniz projenin görsel arayüzü olan API dokümantasyonuna ulaşırsınız:
   👉 **`http://localhost:8080/swagger-ui.html`**

## 🧪 Otomatik Testleri Çalıştırma
Projenin kırılmaz güvenlik (Business/Security Rule) yasaları, saniyeler içinde Unit Test ile kanıtlanabilir:
```bash
mvn test
```
Bu komut arka planda sanal bir H2 veritabanı kurarak 18+ kritik testi koşturur ve `BUILD SUCCESS` raporunu verir.
