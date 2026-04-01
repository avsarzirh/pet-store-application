package com.avsarzirh.PetStoreApplication.service;

import com.avsarzirh.PetStoreApplication.entity.user.User;
import com.avsarzirh.PetStoreApplication.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void checkUniquePropertyViolations_ShouldReturnTrue_WhenUserExists() {
        when(userRepository.existsByUsernameIgnoreCaseOrEmail("testuser", "test@test.com")).thenReturn(true);
        assertTrue(userService.checkUniquePropertyViolations("testuser", "test@test.com"));
        verify(userRepository).existsByUsernameIgnoreCaseOrEmail("testuser", "test@test.com");
    }

    @Test
    void findByUsernameOrEmail_ShouldReturnUser_WhenUserExists() {
        User user = new User();
        user.setUsername("testuser");
        when(userRepository.findByUsernameOrEmail("testuser", "testuser")).thenReturn(Optional.of(user));

        User result = userService.findByUsernameOrEmail("testuser");
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void findByUsernameOrEmail_ShouldThrowException_WhenUserDoesNotExist() {
        when(userRepository.findByUsernameOrEmail("testuser", "testuser")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.findByUsernameOrEmail("testuser"));
    }
}
