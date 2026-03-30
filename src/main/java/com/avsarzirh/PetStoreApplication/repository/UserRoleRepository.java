package com.avsarzirh.PetStoreApplication.repository;

import com.avsarzirh.PetStoreApplication.entity.user.UserRole;
import com.avsarzirh.PetStoreApplication.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    Optional<UserRole> findByRole(Role role);
}
