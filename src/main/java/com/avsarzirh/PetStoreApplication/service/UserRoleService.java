package com.avsarzirh.PetStoreApplication.service;

import com.avsarzirh.PetStoreApplication.entity.user.UserRole;
import com.avsarzirh.PetStoreApplication.enums.Role;
import com.avsarzirh.PetStoreApplication.repository.UserRoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;

    public List<UserRole> findAllUserRoles() {
        return userRoleRepository.findAll();
    }

    public UserRole findUserRoleByRole(Role role) {
        return userRoleRepository.findByRole(role).orElseThrow(
                () -> new EntityNotFoundException("No role found: " + role)
        );
    }
}
