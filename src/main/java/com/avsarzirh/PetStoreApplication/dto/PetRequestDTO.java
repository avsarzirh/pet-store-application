package com.avsarzirh.PetStoreApplication.dto;

import com.avsarzirh.PetStoreApplication.enums.AnimalType;
import com.avsarzirh.PetStoreApplication.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PetRequestDTO {

    @NotBlank(message = "Pet name cannot be left empty.")
    @Size(min = 2, max = 50, message = "Pet name must be between {min}-{max} characters.")
    private String name;

    @NotNull(message = "Age must be provided.")
    @Min(value = 0, message = "Age cannot be negative.")
    private Integer age;

    @NotNull(message = "Price must be provided.")
    @Positive(message = "Price must be strictly greater than 0.")
    private Double price;

    @NotNull(message = "Animal type must be provided (e.g., CAT, DOG, BIRD).")
    private AnimalType type;

    @NotNull(message = "Gender must be provided (e.g., MALE, FEMALE).")
    private Gender gender;

    // Hayvanın hangi mağazaya ait olacağı çok kritik. Dışarıdan sadece mağazanın ID'sini istiyoruz.
    // Kullanıcının kendisi (owner) zaten JWT token'dan bulunacak. Bu yüzden buraya eklemedik.
    @NotNull(message = "Store ID must be provided.")
    private Long storeId;
}
