package com.devopsai.backend.controller;

import com.devopsai.backend.dto.AuthResponse;
import com.devopsai.backend.dto.LoginRequest;
import com.devopsai.backend.dto.RefreshTokenRequest;
import com.devopsai.backend.dto.RegisterRequest;
import com.devopsai.backend.dto.UserDto;
import com.devopsai.backend.entity.Role;
import com.devopsai.backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private UserDto userDto;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, "testuser", "test@devopsai.io", Role.DEVELOPER);
        authResponse = new AuthResponse("mock-access-token", "mock-refresh-token", 900000L, userDto);
    }

    @Test
    void register_ShouldReturnCreatedUser() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("testuser", "test@devopsai.io", "password123", Role.DEVELOPER);
        when(authService.register(any(RegisterRequest.class))).thenReturn(userDto);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@devopsai.io"))
                .andExpect(jsonPath("$.role").value("DEVELOPER"));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("testuser"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturn401Unauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void refresh_WithValidToken_ShouldReturnNewAuthTokens() throws Exception {
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest("mock-refresh-token");
        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"));
    }
}
