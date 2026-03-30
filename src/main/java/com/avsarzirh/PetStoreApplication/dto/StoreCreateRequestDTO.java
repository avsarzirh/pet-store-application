package com.avsarzirh.PetStoreApplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Bu DTO, client'tan (frontend veya Postman) gelecek veridir.
// Sadece "name" alıyoruz çünkü sahibini JWT token'dan öğreneceğiz.
// Client'ın "ben şu kullanıcıyım" demesine gerek yok → güvenli.

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoreCreateRequestDTO {

    // Mağaza adı boş gelemez, 2-100 karakter arası olmalı.
    // @NotBlank → null veya boş string ("") geçersiz sayar.
    @NotBlank(message = "Store name cannot be empty.")
    @Size(min = 2, max = 100, message = "Store name must be between {min}-{max} characters.")
    private String name;
}
