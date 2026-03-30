package com.avsarzirh.PetStoreApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Bu DTO, biz client'a geri döndüreceğimiz veridir.
// Entity'yi (Store) direkt göndermiyoruz çünkü:
//   1. Store → owner (User) → password alanı var! Hassas veri sızar.
//   2. Gereksiz alanlar (Hibernate lazy proxy nesneleri vs.) JSON'a dönüşünce sorun çıkarır.
// Çözüm: Sadece göstermek istediğimiz alanları bu DTO'ya koyarız.

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoreResponseDTO {

    private Long id;

    private String name;

    // Sahibin tüm bilgilerini değil, sadece kullanıcı adını gösteriyoruz.
    // Bu sayede hassas bilgiler (email, password) dışarıya çıkmıyor.
    private String ownerUsername;
}
