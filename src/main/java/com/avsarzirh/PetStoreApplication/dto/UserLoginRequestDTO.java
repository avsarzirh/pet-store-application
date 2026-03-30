package com.avsarzirh.PetStoreApplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginRequestDTO {

    @NotBlank(message = "Username or email cannot be empty.")
    @Size(min = 3, max = 255)
    private String login;

    @NotBlank(message = "Password cannot be empty.")
    @Size(min = 8, max = 64, message = "Password must be between {min}-{max} characters.")
    private String password;
}
