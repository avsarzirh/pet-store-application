package com.avsarzirh.PetStoreApplication.service;

import com.avsarzirh.PetStoreApplication.entity.user.User;
import com.avsarzirh.PetStoreApplication.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Boolean checkUniquePropertyViolations(String username, String email){
        return userRepository.existsByUsernameIgnoreCaseOrEmail(username, email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User findByUsernameOrEmail(String login) {
        return userRepository.findByUsernameOrEmail(login, login).orElseThrow(
                () -> new EntityNotFoundException("No user found with this login information: " + login)
        );
    }

    //!!! Adminin yapacagi kayit islemi icin overload yapilabilir.
    /*
    public User save(UserRegisterRequestDTO user) {
        return userRepository.save(user);
    }
    */
}
