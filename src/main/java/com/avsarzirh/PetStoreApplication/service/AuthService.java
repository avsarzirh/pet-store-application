package com.avsarzirh.PetStoreApplication.service;

import com.avsarzirh.PetStoreApplication.dto.UserLoginRequestDTO;
import com.avsarzirh.PetStoreApplication.dto.UserRegisterRequestDTO;
import com.avsarzirh.PetStoreApplication.entity.user.User;
import com.avsarzirh.PetStoreApplication.enums.Role;
import com.avsarzirh.PetStoreApplication.exception.InvalidPasswordException;
import com.avsarzirh.PetStoreApplication.exception.UniquePropertyViolationException;
import com.avsarzirh.PetStoreApplication.security.jwt.JWTUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public Map<String,?> register(UserRegisterRequestDTO dto) {
        //!!! 1 - DTO'daki username ve email ile eslesen bir User halihazirda kayitli mi?
        if (userService.checkUniquePropertyViolations(dto.getUsername(), dto.getEmail())) {
            //User var.
            throw new UniquePropertyViolationException("A user with this username or email already exists.");
        }

        //!!! 2 - Bos bir user olusturup DTO -> POJO donusumu (mapping) yapalim
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); //Sifreyi hashledik.
        //! Roller icin UserRoleService'e gidilmeli.
        //1. YOL - tum rolleri getirip gerekeni al
        /*
        List<UserRole> userRoles = userRoleService.findAllUserRoles();

        for (UserRole role : userRoles) {
            if (dto.getIsStoreOwner()) {
                if (role.getRole().name().equals("STORE_OWNER")) {
                    user.getRoles().add(role);
                }
            } else if (role.getRole().name().equals("CUSTOMER")) {
                user.getRoles().add(role);
            }
        }
        */

        //2. YOL - ihtiyacimiz olan rolu getir
        if (dto.getIsStoreOwner()) {
            user.getRoles().add(userRoleService.findUserRoleByRole(Role.STORE_OWNER));
        } else {
            user.getRoles().add(userRoleService.findUserRoleByRole(Role.CUSTOMER));
        }

        //!!! 3 - Kaydi tamamla
        User savedUser = userService.save(user);

        return Map.of("message", "User created successfully.", "username", savedUser.getUsername());
    }

    public Map<String,?> login(UserLoginRequestDTO dto) {
        //!!! 1 - Kullanıcıyı bul
        User foundUser = userService.findByUsernameOrEmail(dto.getLogin());

        //!!! 2 - Şifre doğru mu kontrol et
        if (!passwordEncoder.matches(dto.getPassword(), foundUser.getPassword())) {
            throw new InvalidPasswordException("Invalid password for user: " + dto.getLogin());
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(foundUser.getUsername(), dto.getPassword()));

        //!!! 3 - JWT olusturma ve response hazirlama
        String jwt = jwtUtils.generateJWT(authentication);

        return Map.of("message", "Login successful.", "jwt", jwt);
    }
}
