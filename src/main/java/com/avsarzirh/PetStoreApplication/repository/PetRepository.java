package com.avsarzirh.PetStoreApplication.repository;

import com.avsarzirh.PetStoreApplication.entity.Pet;
import com.avsarzirh.PetStoreApplication.enums.AnimalType;
import com.avsarzirh.PetStoreApplication.enums.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {

    // SELECT COUNT(*) > 0 FROM ps_pet WHERE name = ? AND store_id = ?
    boolean existsByNameAndStoreId(String name, Long storeId);

    // SELECT * FROM ps_pet WHERE store_id = ?
    List<Pet> findByStoreId(Long storeId);

    // Dynamic Filter with Pagination
    @Query("SELECT p FROM Pet p WHERE " +
           "(:gender IS NULL OR p.gender = :gender) AND " +
           "(:type IS NULL OR p.type = :type)")
    Page<Pet> findAllPetsWithFilters(@Param("gender") Gender gender, @Param("type") AnimalType type, Pageable pageable);
}
