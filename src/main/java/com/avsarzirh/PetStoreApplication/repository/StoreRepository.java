package com.avsarzirh.PetStoreApplication.repository;

import com.avsarzirh.PetStoreApplication.entity.Store;
import com.avsarzirh.PetStoreApplication.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// JpaRepository<Store, Long> diyoruz:
//   - Store → hangi tabloyla çalışıyoruz
//   - Long  → o tablonun primary key tipi (id alanı Long)
//
// Bu extends sayesinde findById, save, delete, findAll gibi metodlar
// hiç kod yazmadan bize hazır geliyor. Spring Data JPA bunları implement eder.

public interface StoreRepository extends JpaRepository<Store, Long> {

    // Spring bu metod adını okur ve şu SQL'i üretir:
    // SELECT * FROM ps_store WHERE owner_id = ?
    // Bir kullanıcının sahip olduğu mağazaları getirir.
    List<Store> findByOwner(User owner);

    // SELECT COUNT(*) > 0 FROM ps_store WHERE name = ?
    // Aynı isimde mağaza var mı kontrolü için kullanılacak.
    boolean existsByName(String name);
}
