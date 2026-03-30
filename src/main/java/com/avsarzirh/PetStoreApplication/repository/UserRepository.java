package com.avsarzirh.PetStoreApplication.repository;

import com.avsarzirh.PetStoreApplication.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsernameIgnoreCaseOrEmail(String username, String email);

    Optional<User> findByUsernameOrEmail(String username, String email);
}
