package com.avsarzirh.PetStoreApplication.service;

import com.avsarzirh.PetStoreApplication.dto.PetRequestDTO;
import com.avsarzirh.PetStoreApplication.dto.PetResponseDTO;
import com.avsarzirh.PetStoreApplication.entity.Pet;
import com.avsarzirh.PetStoreApplication.entity.Store;
import com.avsarzirh.PetStoreApplication.entity.user.User;
import com.avsarzirh.PetStoreApplication.repository.PetRepository;
import com.avsarzirh.PetStoreApplication.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import com.avsarzirh.PetStoreApplication.exception.UniquePropertyViolationException;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final StoreRepository storeRepository;
    private final UserService userService;

    // --- YARDIMCI METOTLAR (Gizli) ---

    // 1. O an sisteme JWT ile giriş yapmış olan kullanıcının adını getirir
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    // 2. Karmaşık Pet (Entity) nesnesini süzüp güvenli DTO'ya dönüştürür (Manuel Mapper)
    private PetResponseDTO toResponseDTO(Pet pet) {
        PetResponseDTO dto = new PetResponseDTO();
        dto.setId(pet.getId());
        dto.setName(pet.getName());
        dto.setAge(pet.getAge());
        dto.setPrice(pet.getPrice());
        dto.setType(pet.getType());
        dto.setGender(pet.getGender());

        // İlişkili tablolardan güvenli verileri çekiyoruz
        dto.setStoreId(pet.getStore().getId());
        dto.setStoreName(pet.getStore().getName());
        dto.setOwnerUsername(pet.getOwner().getUsername());

        return dto;
    }

    // --- TEMEL İŞLEMLER ---
    // ─── EVCİL HAYVAN OLUŞTUR ──────────────────────────────────────────────────
    public PetResponseDTO createPet(PetRequestDTO dto) {
        // 1. Önce DTO içindeki ID ile veritabanında öyle bir mağaza var mı bakıyoruz.
        Store store = storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new EntityNotFoundException("No store found with id: " + dto.getStoreId()));
                
        // 2. Güvenlik ve İş Kuralı: İşlemi atan kişi mağazanın sahibi değilse hata ver!
        String currentUsername = getCurrentUsername();
        if (!store.getOwner().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You are not the owner of this store. You cannot add pets to it.");
        }

        // 3. İş Kuralı: Aynı mağazada aynı isimde bir hayvan var mı?
        if (petRepository.existsByNameAndStoreId(dto.getName(), dto.getStoreId())) {
            throw new UniquePropertyViolationException("A pet with this name already exists in this store.");
        }
        
        // 4. Giriş yapan gerçek kullanıcıyı (Owner) veri tabanından tam teşekküllü Entity olarak çektik.
        User owner = userService.findByUsernameOrEmail(currentUsername);
        
        // 5. Yeni bir Pet (Entity) yarat, içini DTO'dan gelen taze verilerle doldur
        Pet pet = new Pet();
        pet.setName(dto.getName());
        pet.setAge(dto.getAge());
        pet.setPrice(dto.getPrice());
        pet.setType(dto.getType());
        pet.setGender(dto.getGender());
        
        // İlişkileri kuruyoruz
        pet.setStore(store);  // Dükkan
        pet.setOwner(owner);  // Sahibi
        
        // 6. Spring Data JPA ile veritabanına kaydet
        Pet savedPet = petRepository.save(pet);
        
        // 7. Döndürmeden önce kirli Entity'i => toResponseDTO <= süzgecimizden geçirip DTO yap.
        return toResponseDTO(savedPet);
    }

    // ─── TÜM EVCİL HAYVANLARI LİSTELE ──────────────────────────────────────────
    public List<PetResponseDTO> getAllPets() {
        return petRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // ─── TEK EVCİL HAYVAN GETİR ────────────────────────────────────────────────
    public PetResponseDTO getPetById(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No pet found with id: " + id));
        return toResponseDTO(pet);
    }

    // ─── BİR MAĞAZANIN EVCİL HAYVANLARINI GETİR ────────────────────────────────
    public List<PetResponseDTO> getPetsByStoreId(Long storeId) {
        return petRepository.findByStoreId(storeId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // ─── EVCİL HAYVAN GÜNCELLE ─────────────────────────────────────────────────
    public PetResponseDTO updatePet(Long id, PetRequestDTO dto) {
        // 1. Hayvan veritabanında var mı?
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No pet found with id: " + id));

        // 2. Güvenlik: İsteği atan kişi hayvanın sahibi mi?
        String currentUsername = getCurrentUsername();
        if (!pet.getOwner().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You are not the owner of this pet.");
        }
        
        // 3. İsim veya Mağaza değişikliği varsa benzersizliği kontrol et
        boolean nameChanged = !pet.getName().equals(dto.getName());
        boolean storeChanged = !pet.getStore().getId().equals(dto.getStoreId());
        
        if (nameChanged || storeChanged) {
             if (petRepository.existsByNameAndStoreId(dto.getName(), dto.getStoreId())) {
                 throw new UniquePropertyViolationException("A pet with this name already exists in the target store.");
             }
        }
        
        // 4. Mağaza değişimi varsa ilişkiyi güncelle
        if (storeChanged) {
            Store newStore = storeRepository.findById(dto.getStoreId())
                    .orElseThrow(() -> new EntityNotFoundException("No store found with id: " + dto.getStoreId()));
            if (!newStore.getOwner().getUsername().equals(currentUsername)) {
                throw new AccessDeniedException("You are not the owner of the target store.");
            }
            // Artık güvenli, hayvanın mağazasını değiştirebiliriz
            pet.setStore(newStore);
        }

        // 5. Güncelleme
        pet.setName(dto.getName());
        pet.setAge(dto.getAge());
        pet.setPrice(dto.getPrice());
        pet.setType(dto.getType());
        pet.setGender(dto.getGender());

        Pet updatedPet = petRepository.save(pet);
        return toResponseDTO(updatedPet);
    }

    // ─── EVCİL HAYVAN SİL ──────────────────────────────────────────────────────
    public void deletePet(Long id) {
        // 1. Hayvan var mı?
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No pet found with id: " + id));

        // 2. Sahibi mi siliyor?
        String currentUsername = getCurrentUsername();
        if (!pet.getOwner().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You are not the owner of this pet.");
        }

        petRepository.delete(pet);
    }

}
