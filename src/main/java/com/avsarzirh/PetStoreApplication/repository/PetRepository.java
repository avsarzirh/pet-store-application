package com.avsarzirh.PetStoreApplication.repository;

import com.avsarzirh.PetStoreApplication.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {

    // SELECT COUNT(*) > 0 FROM ps_pet WHERE name = ? AND store_id = ?
    boolean existsByNameAndStoreId(String name, Long storeId);

    // SELECT * FROM ps_pet WHERE store_id = ?
    List<Pet> findByStoreId(Long storeId);
}
