package com.avsarzirh.PetStoreApplication.controller;

import com.avsarzirh.PetStoreApplication.dto.StoreCreateRequestDTO;
import com.avsarzirh.PetStoreApplication.dto.StoreResponseDTO;
import com.avsarzirh.PetStoreApplication.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController → Bu sınıf HTTP isteklerini karşılar.
// Her metodun dönüş değeri otomatik olarak JSON'a çevrilir.
// (@Controller olsaydı, HTML sayfası döndürürdü — biz API yapıyoruz.)

// @RequestMapping("/store") → Bu sınıftaki tüm endpoint'ler /stores ile başlar.

@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // ─── MAĞAZA OLUŞTUR ───────────────────────────────────────────────────────
    // @PreAuthorize: metod çağrılmadan Spring Security kontrol eder.
    // "Bu kullanıcının 'STORE_OWNER' yetkisi var mı?"
    // Yoksa → 403 Forbidden otomatik döner, biz kod yazmadık.
    @PostMapping
    @PreAuthorize("hasAuthority('STORE_OWNER')")
    public ResponseEntity<StoreResponseDTO> createStore(@RequestBody @Valid StoreCreateRequestDTO dto) {
        // @RequestBody → HTTP isteğinin body'sindeki JSON'ı DTO'ya çevir
        StoreResponseDTO response = storeService.createStore(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED); // 201
    }

    // ─── TÜM MAĞAZALARI LİSTELE ───────────────────────────────────────────────
    // Herkes görebilir (giriş yapmış olması yeterli — JWT filter halleder)
    @GetMapping
    public ResponseEntity<List<StoreResponseDTO>> getAllStores() {
        return ResponseEntity.ok(storeService.getAllStores()); // 200
    }

    // ─── TEK MAĞAZA GETİR ─────────────────────────────────────────────────────
    // @PathVariable → URL'deki {id} kısmını alır
    @GetMapping("/{id}")
    public ResponseEntity<StoreResponseDTO> getStoreById(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.getStoreById(id));
    }

    // ─── MAĞAZA GÜNCELLE ──────────────────────────────────────────────────────
    // PUT /store/5
    // Sadece STORE_OWNER rolü bu isteği yapabilir.
    // Kendi mağazası mı kontrolü → StoreService'te yapılıyor.
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('STORE_OWNER')")
    public ResponseEntity<StoreResponseDTO> updateStore(
            @PathVariable Long id,
            @RequestBody @Valid StoreCreateRequestDTO dto) {
        return ResponseEntity.ok(storeService.updateStore(id, dto));
    }

    // ─── MAĞAZA SİL ───────────────────────────────────────────────────────────
    // DELETE /store/5
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('STORE_OWNER')")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
        storeService.deleteStore(id);
        // Silme başarılıysa body yok, sadece 204 No Content dön.
        // 204 = "yaptım ama gösterecek bir şey yok"
        return ResponseEntity.noContent().build();
    }
}
