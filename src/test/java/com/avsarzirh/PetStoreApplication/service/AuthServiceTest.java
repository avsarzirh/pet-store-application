package com.avsarzirh.PetStoreApplication.service;

import com.avsarzirh.PetStoreApplication.dto.UserLoginRequestDTO;
import com.avsarzirh.PetStoreApplication.dto.UserRegisterRequestDTO;
import com.avsarzirh.PetStoreApplication.entity.user.User;
import com.avsarzirh.PetStoreApplication.entity.user.UserRole;
import com.avsarzirh.PetStoreApplication.enums.Role;
import com.avsarzirh.PetStoreApplication.exception.InvalidPasswordException;
import com.avsarzirh.PetStoreApplication.exception.UniquePropertyViolationException;
import com.avsarzirh.PetStoreApplication.security.jwt.JWTUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserService userService;
    @Mock private UserRoleService userRoleService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JWTUtils jwtUtils;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_ShouldThrowException_WhenUserExists() {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO("user", "test@test.com", "pass", false);
        when(userService.checkUniquePropertyViolations(dto.getUsername(), dto.getEmail())).thenReturn(true);

        assertThrows(UniquePropertyViolationException.class, () -> authService.register(dto));
        verify(userService, never()).save(any());
    }

    @Test
    void register_ShouldSaveUser_WhenValid() {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO("newuser", "test@test.com", "pass", false);
        when(userService.checkUniquePropertyViolations(dto.getUsername(), dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPass");
        
        UserRole customerRole = new UserRole();
        customerRole.setRole(Role.CUSTOMER);
        
        when(userRoleService.findUserRoleByRole(Role.CUSTOMER)).thenReturn(customerRole);
        
        User savedUser = new User();
        savedUser.setUsername("newuser");
        when(userService.save(any(User.class))).thenReturn(savedUser);

        Map<String, ?> result = authService.register(dto);
        assertEquals("newuser", result.get("username"));
        verify(userService).save(any(User.class));
    }

    @Test
    void login_ShouldThrowException_WhenPasswordInvalid() {
        UserLoginRequestDTO dto = new UserLoginRequestDTO("user", "wrongpass");
        User user = new User();
        user.setPassword("encodedPass");
        when(userService.findByUsernameOrEmail(dto.getLogin())).thenReturn(user);
        when(passwordEncoder.matches("wrongpass", "encodedPass")).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> authService.login(dto));
    }

    @Test
    void login_ShouldReturnJwt_WhenValid() {
        UserLoginRequestDTO dto = new UserLoginRequestDTO("user", "pass");
        User user = new User();
        user.setUsername("user");
        user.setPassword("encodedPass");

        when(userService.findByUsernameOrEmail(dto.getLogin())).thenReturn(user);
        when(passwordEncoder.matches("pass", "encodedPass")).thenReturn(true);
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtUtils.generateJWT(auth)).thenReturn("mock.jwt.token");

        Map<String, ?> result = authService.login(dto);
        assertEquals("mock.jwt.token", result.get("jwt"));
    }
}
