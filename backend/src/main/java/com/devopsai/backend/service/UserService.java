package com.devopsai.backend.service;

import com.devopsai.backend.dto.UserDto;
import com.devopsai.backend.entity.User;
import com.devopsai.backend.repository.UserRepository;
import com.devopsai.backend.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new IllegalStateException("No authenticated user found");
        }

        User user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + principal.getId()));

        return UserDto.fromEntity(user);
    }
}
