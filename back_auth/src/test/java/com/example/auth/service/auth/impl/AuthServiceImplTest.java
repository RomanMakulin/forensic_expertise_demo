package com.example.auth.service.auth.impl;

import com.example.auth.dto.auth.AuthUserDetailsResponse;
import com.example.auth.dto.auth.LoginRequest;
import com.example.auth.dto.auth.RegistrationRequest;
import com.example.auth.model.User;
import com.example.auth.service.auth.LoginService;
import com.example.auth.service.auth.OAuthVKService;
import com.example.auth.service.auth.RegistrationService;
import com.example.auth.repository.UserRepository;
import com.example.auth.integrations.keycloak.KeycloakManager;
import com.example.auth.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.AccessTokenResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private RegistrationService registrationService;

    @Mock
    private LoginService loginService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KeycloakManager keycloakManager;

    @Mock
    private UserService userService;

    @Mock
    private OAuthVKService oAuthVKService;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setup() {
        // Фейковый Jwt и установка в SecurityContext
        Jwt jwt = Jwt.withTokenValue("test-token")
                .claim("email", "test@example.com")
                .header("alg", "none")
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(jwt, jwt)
        );
    }

    @Test
    void register() {
        RegistrationRequest registrationRequest = new RegistrationRequest();
        authService.register(registrationRequest);
        verify(registrationService).register(registrationRequest);
    }

    @Test
    void login() {
        LoginRequest request = new LoginRequest();
        AuthUserDetailsResponse response = new AuthUserDetailsResponse();
        when(loginService.login(request)).thenReturn(response);

        AuthUserDetailsResponse actual = authService.login(request);

        assertSame(response, actual);
        verify(loginService).login(request);
    }

    @Test
    void verifyRegistration() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setEmail("test@example.com");

        when(userService.getUserById(userId)).thenReturn(user);

        authService.verifyRegistration(userId);

        assertTrue(user.isVerificationEmail());
        verify(userRepository).save(user);
    }

    @Test
    void logout() {
        User user = new User();
        user.setKeycloakId("kc-id");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        authService.logout();

        verify(keycloakManager).logoutUserById("kc-id");
    }

    @Test
    void getAuthenticatedUser_shouldReturnUser() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        User result = authService.getAuthenticatedUser();

        assertEquals(user, result);
    }

    @Test
    void getAuthenticatedUser_shouldThrowIfNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authService.getAuthenticatedUser());

        assertEquals("Authenticated user not found", exception.getMessage());
    }

    @Test
    void handleVkCallback_shouldReturnResponse() {
        String code = "vk-code";
        AuthUserDetailsResponse response = new AuthUserDetailsResponse();
        when(oAuthVKService.processVkAuth(code)).thenReturn(response);

        AuthUserDetailsResponse actual = authService.handleVkCallback(code);

        assertSame(response, actual);
    }
}
