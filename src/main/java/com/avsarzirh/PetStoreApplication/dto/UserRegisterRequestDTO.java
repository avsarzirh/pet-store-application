package com.avsarzirh.PetStoreApplication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRequestDTO {

    @NotBlank(message = "Username cannot be empty.")
    @Size(min = 3, max = 32, message = "Username must be between {min}-{max} characters.")
    private String username;

    @NotBlank(message = "Email cannot be empty.")
    @Size(min = 6, max = 255, message = "Username must be between {min}-{max} characters.")
    @Email
    private String email;

    @NotBlank(message = "Password cannot be empty.")
    @Size(min = 8, max = 64, message = "Password must be between {min}-{max} characters.")
    private String password;

    @NotNull(message = "Store owner status must be declared. Either true or false.")
    private Boolean isStoreOwner;
}
