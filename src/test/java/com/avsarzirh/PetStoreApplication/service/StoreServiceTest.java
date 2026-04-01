package com.avsarzirh.PetStoreApplication.service;

import com.avsarzirh.PetStoreApplication.dto.StoreCreateRequestDTO;
import com.avsarzirh.PetStoreApplication.dto.StoreResponseDTO;
import com.avsarzirh.PetStoreApplication.entity.Store;
import com.avsarzirh.PetStoreApplication.entity.user.User;
import com.avsarzirh.PetStoreApplication.exception.UniquePropertyViolationException;
import com.avsarzirh.PetStoreApplication.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class StoreServiceTest {

    @Mock private StoreRepository storeRepository;
    @Mock private UserService userService;

    @InjectMocks
    private StoreService storeService;

    private void mockSecurityContext(String username) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createStore_ShouldThrowException_WhenNameExists() {
        StoreCreateRequestDTO dto = new StoreCreateRequestDTO();
        dto.setName("MyStore");
        when(storeRepository.existsByName("MyStore")).thenReturn(true);

        assertThrows(UniquePropertyViolationException.class, () -> storeService.createStore(dto));
        verify(userService, never()).findByUsernameOrEmail(any());
    }

    @Test
    void createStore_ShouldCreate_WhenValid() {
        mockSecurityContext("ownerName");
        StoreCreateRequestDTO dto = new StoreCreateRequestDTO();
        dto.setName("MyStore");
        
        when(storeRepository.existsByName("MyStore")).thenReturn(false);
        User owner = new User();
        owner.setUsername("ownerName");
        when(userService.findByUsernameOrEmail("ownerName")).thenReturn(owner);

        Store savedStore = new Store();
        savedStore.setId(1L);
        savedStore.setName("MyStore");
        savedStore.setOwner(owner);
        when(storeRepository.save(any(Store.class))).thenReturn(savedStore);

        StoreResponseDTO result = storeService.createStore(dto);
        assertEquals("MyStore", result.getName());
        assertEquals("ownerName", result.getOwnerUsername());
    }

    @Test
    void updateStore_ShouldThrowAccessDenied_WhenNotOwner() {
        mockSecurityContext("hacker");
        
        Store store = new Store();
        store.setId(1L);
        User realOwner = new User();
        realOwner.setUsername("realOwner");
        store.setOwner(realOwner);

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        StoreCreateRequestDTO dto = new StoreCreateRequestDTO();
        
        assertThrows(AccessDeniedException.class, () -> storeService.updateStore(1L, dto));
        verify(storeRepository, never()).save(any());
    }

    @Test
    void deleteStore_ShouldThrowEntityNotFound_WhenStoreMissing() {
        when(storeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> storeService.deleteStore(99L));
    }

    @Test
    void deleteStore_ShouldDelete_WhenOwnerIsValid() {
        mockSecurityContext("realOwner");
        
        Store store = new Store();
        store.setId(1L);
        User realOwner = new User();
        realOwner.setUsername("realOwner");
        store.setOwner(realOwner);

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

        storeService.deleteStore(1L);
        verify(storeRepository).delete(store);
    }
}
