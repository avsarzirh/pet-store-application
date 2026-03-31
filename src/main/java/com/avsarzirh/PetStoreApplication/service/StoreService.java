package com.avsarzirh.PetStoreApplication.service;

import com.avsarzirh.PetStoreApplication.dto.StoreCreateRequestDTO;
import com.avsarzirh.PetStoreApplication.dto.StoreResponseDTO;
import com.avsarzirh.PetStoreApplication.entity.Store;
import com.avsarzirh.PetStoreApplication.entity.user.User;
import com.avsarzirh.PetStoreApplication.exception.UniquePropertyViolationException;
import com.avsarzirh.PetStoreApplication.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

// @Service → Spring'e "bu sınıfı bir bean olarak yönet" diyoruz.
// Bean nedir? Spring'in oluşturup yönettiği nesne.
// @RequiredArgsConstructor → final alanlar için constructor üretir (Lombok).
// Bu sayede @Autowired yerine constructor injection yapılıyor — daha test edilebilir kod.

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserService userService;

    // ─── YARDIMCI METOD ───────────────────────────────────────────────────────
    // SecurityContext'ten o anki giriş yapmış kullanıcının username'ini alır.
    // SecurityContext: Spring Security'nin "şu an bu kullanıcı giriş yapmış"
    // bilgisini tuttuğu hafıza alanıdır. Her HTTP isteği için ayrı tutulur.
    // JWTAuthenticationFilter zaten bu bilgiyi istek başında oraya koyuyor.
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // JWT'deki subject (username)
    }

    // ─── Entity → DTO Dönüşümü ────────────────────────────────────────────────
    // Bu dönüşüm Service'te yapılır, Controller'da değil.
    // Neden? Controller sadece "gelen isteği al, cevabı gönder" yapar.
    // Veriyi nasıl şekillendireceğimiz iş mantığının parçası → Service'te.
    private StoreResponseDTO toResponseDTO(Store store) {
        StoreResponseDTO dto = new StoreResponseDTO();
        dto.setId(store.getId());
        dto.setName(store.getName());
        dto.setOwnerUsername(store.getOwner().getUsername());
        return dto;
    }

    // ─── MAĞAZA OLUŞTUR ───────────────────────────────────────────────────────
    public StoreResponseDTO createStore(StoreCreateRequestDTO dto) {

        // İş Kuralı 1: Aynı isimde mağaza var mı?
        if (storeRepository.existsByName(dto.getName())) {
            throw new UniquePropertyViolationException(
                "A store with this name already exists: " + dto.getName()
            );
        }

        // İş Kuralı 2: Sahibi kim?
        // JWT'den gelen username ile User nesnesini çekiyoruz.
        // Client bize "benim username'im şu" demiyor — zaten biliyoruz.
        String currentUsername = getCurrentUsername();
        User owner = userService.findByUsernameOrEmail(currentUsername);

        // Entity oluştur ve alanları doldur
        Store store = new Store();
        store.setName(dto.getName());
        store.setOwner(owner); // Sahibi bu kullanıcı

        // Kaydet ve DTO olarak döndür
        Store savedStore = storeRepository.save(store);
        return toResponseDTO(savedStore);
    }

    // ─── TÜM MAĞAZALARI LİSTELE ───────────────────────────────────────────────
    public List<StoreResponseDTO> getAllStores() {
        // findAll() → JpaRepository'den hazır gelen metod, SQL yazmadık.
        // Listedeki her Store nesnesini toResponseDTO ile DTO'ya çeviriyoruz.
        return storeRepository.findAll()
                .stream()
                .map(this::toResponseDTO) // her Store için toResponseDTO çağır
                .toList();

    }

    // ─── TEK MAĞAZA GETİR ─────────────────────────────────────────────────────
    public StoreResponseDTO getStoreById(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No store found with id: " + id));
        // orElseThrow: Optional içi boşsa exception fırlat.
        // Optional nedir? "Değer olabilir de olmayabilir de" demek.
        // null yerine Optional kullanmak NullPointerException'ı önler.
        return toResponseDTO(store);
    }

    // ─── MAĞAZA GÜNCELLE ──────────────────────────────────────────────────────
    public StoreResponseDTO updateStore(Long id, StoreCreateRequestDTO dto) {

        // Adım 1: Mağaza var mı?
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No store found with id: " + id));

        // Adım 2: İsteği atan kişi bu mağazanın sahibi mi?
        // Bu kontrol çok önemli! Olmasa herkes başkasının mağazasını güncelleyebilir.
        String currentUsername = getCurrentUsername();
        if (!store.getOwner().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You are not the owner of this store.");
        }

        // Adım 3: Yeni isim başka bir mağazada kullanılıyor mu?
        if (!store.getName().equals(dto.getName()) && storeRepository.existsByName(dto.getName())) {
            throw new UniquePropertyViolationException(
                "A store with this name already exists: " + dto.getName()
            );
        }

        store.setName(dto.getName());
        Store updatedStore = storeRepository.save(store);
        return toResponseDTO(updatedStore);
    }

    // ─── MAĞAZA SİL ───────────────────────────────────────────────────────────
    public void deleteStore(Long id) {

        // Adım 1: Mağaza var mı?
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No store found with id: " + id));

        // Adım 2: Sahibi mi siliyor?
        String currentUsername = getCurrentUsername();
        if (!store.getOwner().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You are not the owner of this store.");
        }

        storeRepository.delete(store);
        // void döndürüyoruz çünkü silme işleminde gösterilecek bir veri yok.
    }
}
