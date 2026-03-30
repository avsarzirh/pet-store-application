package com.avsarzirh.PetStoreApplication.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// @RestControllerAdvice → Tüm Controller'lardan fırlayan exception'ları burada yakalar.
// "Advice" = "danışman" — Controller'lara danışmanlık yapıyor.
// Normal @ControllerAdvice ile fark: @RestControllerAdvice JSON döndürür (REST API için).
//
// Neden bu sınıf var?
// Her Service, her Controller'da try-catch yazsaydık kod tekrarı olurdu.
// Bunun yerine "hangi exception fırlarsa fırlasın, şu şekilde yanıt ver" diyoruz.

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 Not Found
    // EntityNotFoundException → "Aradığın şey veritabanında yok"
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    // 409 Conflict
    // UniquePropertyViolationException → "Bu isim/email zaten kullanılıyor"
    @ExceptionHandler(UniquePropertyViolationException.class)
    public ResponseEntity<Map<String, String>> handleUniqueViolation(UniquePropertyViolationException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    // 400 Bad Request
    // InvalidPasswordException → "Şifre yanlış"
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPassword(InvalidPasswordException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    // 403 Forbidden
    // AccessDeniedException → "Bu işlemi yapmaya yetkisiz değilsin"
    // Not: Spring Security de AccessDeniedException fırlatır (role uyuşmadığında)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    // 400 Bad Request — Validation hataları
    // @Valid annotation'ı devreye girdiğinde, kural ihlali varsa bu exception fırlar.
    // Örneğin: name boş bırakıldığında, password 8 karakterden kısa olduğunda.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        // Birden fazla alan hatalı olabilir. Hepsini topla.
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            // alanAdı: hataMessajı şeklinde map oluştur
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    // 400 Bad Request — JSON parse hatası
    // Body gönderilmediğinde veya JSON formatı bozuksa bu exception fırlar.
    // Örnek: body boş gönderildi, JSON syntax hatası var vs.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Request body is missing or malformed: " + ex.getMessage()));
    }

    // 500 Internal Server Error — Beklenmeyen tüm hatalar için fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", ex.getClass().getSimpleName() + ": " + ex.getMessage()));
    }
}
