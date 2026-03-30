package com.avsarzirh.PetStoreApplication.controller;

import com.avsarzirh.PetStoreApplication.dto.UserLoginRequestDTO;
import com.avsarzirh.PetStoreApplication.dto.UserRegisterRequestDTO;
import com.avsarzirh.PetStoreApplication.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, ?>> register(@RequestBody @Valid UserRegisterRequestDTO dto) {
        return new ResponseEntity<>(authService.register(dto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, ?>> login(@RequestBody @Valid UserLoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}
