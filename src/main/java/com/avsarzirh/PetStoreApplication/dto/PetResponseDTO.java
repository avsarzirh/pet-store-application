package com.avsarzirh.PetStoreApplication.dto;

import com.avsarzirh.PetStoreApplication.enums.AnimalType;
import com.avsarzirh.PetStoreApplication.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PetResponseDTO {

    // Veritabanında atanan ID
    private Long id;

    // Temel hayvan bilgileri
    private String name;
    private Integer age;
    private Double price;
    private AnimalType type;
    private Gender gender;

    // İhtiyaç duyulabileceği için ek bilgilendirici (Store ve Owner) dataları
    private Long storeId;
    private String storeName;
    private String ownerUsername;
}
