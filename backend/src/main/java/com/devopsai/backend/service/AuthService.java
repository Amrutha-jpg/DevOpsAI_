package com.devopsai.backend.service;

import com.devopsai.backend.dto.*;
import com.devopsai.backend.entity.RefreshToken;
import com.devopsai.backend.entity.User;
import com.devopsai.backend.repository.RefreshTokenRepository;
import com.devopsai.backend.repository.UserRepository;
import com.devopsai.backend.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public UserDto register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = new User(
            request.getUsername(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            request.getRole()
        );

        User savedUser = userRepository.save(user);
        return UserDto.fromEntity(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsernameOrEmail(),
                request.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateAccessToken(authentication);

        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
            .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        // Delete old refresh tokens for clean state
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = createRefreshToken(user);

        return new AuthResponse(
            accessToken,
            refreshToken.getToken(),
            tokenProvider.getAccessTokenExpirationMs(),
            UserDto.fromEntity(user)
        );
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
            .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (refreshToken.isRevoked() || refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token was expired or revoked");
        }

        User user = refreshToken.getUser();
        String newAccessToken = tokenProvider.generateAccessTokenForUser(user.getId(), user.getUsername(), user.getRole().name());

        return new AuthResponse(
            newAccessToken,
            refreshToken.getToken(),
            tokenProvider.getAccessTokenExpirationMs(),
            UserDto.fromEntity(user)
        );
    }

    private RefreshToken createRefreshToken(User user) {
        String tokenStr = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusMillis(tokenProvider.getRefreshTokenExpirationMs());

        RefreshToken refreshToken = new RefreshToken(user, tokenStr, expiryDate);
        return refreshTokenRepository.save(refreshToken);
    }
}
