package com.avsarzirh.PetStoreApplication.security.service;

import com.avsarzirh.PetStoreApplication.entity.user.User;
import com.avsarzirh.PetStoreApplication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(
                //!!! Security AuthenticationException alindiginda islemi durdurur.
                //!!! Bu nedenle UsernameNotFoundException firlatiyoruz.
                () -> new UsernameNotFoundException("No user found with given username: " + username)
        );

        return UserDetailsImpl.build(user);
    }
}
