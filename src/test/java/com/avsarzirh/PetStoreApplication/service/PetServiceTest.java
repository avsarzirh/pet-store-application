package com.avsarzirh.PetStoreApplication.service;

import com.avsarzirh.PetStoreApplication.dto.PetRequestDTO;
import com.avsarzirh.PetStoreApplication.dto.PetResponseDTO;
import com.avsarzirh.PetStoreApplication.entity.Pet;
import com.avsarzirh.PetStoreApplication.entity.Store;
import com.avsarzirh.PetStoreApplication.entity.user.User;
import com.avsarzirh.PetStoreApplication.enums.AnimalType;
import com.avsarzirh.PetStoreApplication.enums.Gender;
import com.avsarzirh.PetStoreApplication.exception.UniquePropertyViolationException;
import com.avsarzirh.PetStoreApplication.repository.PetRepository;
import com.avsarzirh.PetStoreApplication.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock private PetRepository petRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private UserService userService;

    @InjectMocks
    private PetService petService;

    private void mockSecurityContext(String username) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createPet_ShouldThrowException_WhenStoreNotFound() {
        PetRequestDTO dto = new PetRequestDTO();
        dto.setStoreId(99L);
        when(storeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> petService.createPet(dto));
    }

    @Test
    void createPet_ShouldThrowAccessDenied_WhenUserNotStoreOwner() {
        mockSecurityContext("hacker");

        PetRequestDTO dto = new PetRequestDTO();
        dto.setStoreId(1L);

        Store store = new Store();
        User realOwner = new User();
        realOwner.setUsername("realOwner");
        store.setOwner(realOwner);

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

        assertThrows(AccessDeniedException.class, () -> petService.createPet(dto));
    }

    @Test
    void createPet_ShouldThrowException_WhenPetNameAlreadyExists() {
        mockSecurityContext("realOwner");

        PetRequestDTO dto = new PetRequestDTO();
        dto.setStoreId(1L);
        dto.setName("Fluffy");

        Store store = new Store();
        User realOwner = new User();
        realOwner.setUsername("realOwner");
        store.setOwner(realOwner);

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(petRepository.existsByNameAndStoreId("Fluffy", 1L)).thenReturn(true);

        assertThrows(UniquePropertyViolationException.class, () -> petService.createPet(dto));
    }

    @Test
    void createPet_ShouldSaveAndReturnDto_WhenValid() {
        mockSecurityContext("realOwner");

        PetRequestDTO dto = new PetRequestDTO();
        dto.setStoreId(1L);
        dto.setName("Fluffy");
        dto.setAge(2);
        dto.setPrice(100.0);
        dto.setType(AnimalType.CAT);
        dto.setGender(Gender.MALE);

        User realOwner = new User();
        realOwner.setUsername("realOwner");
        
        Store store = new Store();
        store.setId(1L);
        store.setName("HappyStore");
        store.setOwner(realOwner);

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(petRepository.existsByNameAndStoreId("Fluffy", 1L)).thenReturn(false);
        when(userService.findByUsernameOrEmail("realOwner")).thenReturn(realOwner);

        Pet savedPet = new Pet();
        savedPet.setId(10L);
        savedPet.setName("Fluffy");
        savedPet.setAge(2);
        savedPet.setPrice(100.0);
        savedPet.setType(AnimalType.CAT);
        savedPet.setGender(Gender.MALE);
        savedPet.setStore(store);
        savedPet.setOwner(realOwner);

        when(petRepository.save(any(Pet.class))).thenReturn(savedPet);

        PetResponseDTO result = petService.createPet(dto);

        assertNotNull(result);
        assertEquals("Fluffy", result.getName());
        assertEquals(10L, result.getId());
        assertEquals("realOwner", result.getOwnerUsername());
        assertEquals("HappyStore", result.getStoreName());
    }

    @Test
    void getAllPets_ShouldReturnPage_WhenCalled() {
        User owner = new User();
        owner.setUsername("owner");
        
        Store store = new Store();
        store.setId(1L);
        store.setName("HappyStore");
        store.setOwner(owner);

        Pet p1 = new Pet();
        p1.setId(1L);
        p1.setStore(store);
        p1.setOwner(owner);

        Page<Pet> page = new PageImpl<>(List.of(p1));
        when(petRepository.findAllPetsWithFilters(any(), any(), any())).thenReturn(page);

        Page<PetResponseDTO> result = petService.getAllPets(null, null, PageRequest.of(0, 10));
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
    }
}
