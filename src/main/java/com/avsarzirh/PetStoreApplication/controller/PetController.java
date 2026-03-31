package com.avsarzirh.PetStoreApplication.controller;

import com.avsarzirh.PetStoreApplication.dto.PetRequestDTO;
import com.avsarzirh.PetStoreApplication.dto.PetResponseDTO;
import com.avsarzirh.PetStoreApplication.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pet")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    // ─── 1. EVCİL HAYVAN OLUŞTUR ───────────────────────────────────────────────
    // POST /pet
    @PostMapping
    @PreAuthorize("hasAuthority('STORE_OWNER')")
    public ResponseEntity<PetResponseDTO> createPet(@RequestBody @Valid PetRequestDTO dto) {
        PetResponseDTO createdPet = petService.createPet(dto);
        return new ResponseEntity<>(createdPet, HttpStatus.CREATED); // 201
    }

    // ─── 2. TÜM HAYVANLARI LİSTELE ─────────────────────────────────────────────
    // GET /pet
    @GetMapping
    public ResponseEntity<List<PetResponseDTO>> getAllPets() {
        return ResponseEntity.ok(petService.getAllPets()); // 200
    }

    // ─── 3. BELİRLİ BİR HAYVANI GETİR ──────────────────────────────────────────
    // GET /pet/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PetResponseDTO> getPetById(@PathVariable Long id) {
        return ResponseEntity.ok(petService.getPetById(id)); // 200
    }

    // ─── 4. BİR MAĞAZANIN HAYVANLARINI GETİR ───────────────────────────────────
    // GET /pet/store/{storeId}
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<PetResponseDTO>> getPetsByStoreId(@PathVariable Long storeId) {
        return ResponseEntity.ok(petService.getPetsByStoreId(storeId)); // 200
    }

    // ─── 5. EVCİL HAYVAN GÜNCELLE ──────────────────────────────────────────────
    // PUT /pet/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('STORE_OWNER')")
    public ResponseEntity<PetResponseDTO> updatePet(
            @PathVariable Long id, 
            @Valid @RequestBody PetRequestDTO dto) {
        return ResponseEntity.ok(petService.updatePet(id, dto)); // 200
    }

    // ─── 6. EVCİL HAYVAN SİL ───────────────────────────────────────────────────
    // DELETE /pet/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('STORE_OWNER')")
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        petService.deletePet(id);
        return ResponseEntity.noContent().build(); // 204
    }
}
