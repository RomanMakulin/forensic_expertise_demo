package com.example.auth.service.auth.impl;

import com.example.auth.integrations.keycloak.KeycloakManager;
import com.example.auth.model.PasswordResetToken;
import com.example.auth.model.User;
import com.example.auth.repository.PasswordResetTokenRepository;
import com.example.auth.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordTokenServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private KeycloakManager keycloakManager;

    @InjectMocks
    private PasswordTokenServiceImpl passwordTokenServiceImpl;

    private final String email = "test@example.com";
    private final String token = "token-123";
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setKeycloakId("kc-123");
    }

    @Test
    void createPasswordResetToken_shouldGenerateAndSaveToken() {
        // Arrange
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId("kc-123");

        when(keycloakManager.getUserByEmail(email)).thenReturn(userRepresentation);
        when(userService.getUserByKeycloakId("kc-123")).thenReturn(user);

        // Act
        String resultToken = passwordTokenServiceImpl.createPasswordResetToken(email);

        // Assert
        assertNotNull(resultToken);
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void validateToken_shouldPassForValidToken() {
        // Arrange
        PasswordResetToken validToken = new PasswordResetToken(token, user, LocalDateTime.now().plusMinutes(30));
        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(validToken));

        // Act + Assert
        assertDoesNotThrow(() -> passwordTokenServiceImpl.validateToken(token));
    }

    @Test
    void validateToken_shouldThrowIfTokenExpired() {
        // Arrange
        PasswordResetToken expiredToken = new PasswordResetToken(token, user, LocalDateTime.now().minusMinutes(5));
        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(expiredToken));

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> passwordTokenServiceImpl.validateToken(token));
        assertEquals("Время ожидания сброса пароля вышло. Пожалуйста, попробуйте снова", ex.getMessage());
        verify(passwordResetTokenRepository).delete(expiredToken);
    }

    @Test
    void validateToken_shouldThrowIfTokenNotFound() {
        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> passwordTokenServiceImpl.validateToken(token));
        assertEquals("Invalid token", ex.getMessage());
    }

    @Test
    void deleteToken_shouldDeleteByToken() {
        passwordTokenServiceImpl.deleteToken(token);
        verify(passwordResetTokenRepository).deleteByToken(token);
    }

    @Test
    void getUser_shouldReturnUserByToken() {
        PasswordResetToken tokenEntity = new PasswordResetToken(token, user, LocalDateTime.now().plusMinutes(30));
        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(tokenEntity));

        User result = passwordTokenServiceImpl.getUser(token);
        assertEquals(user, result);
    }

    @Test
    void deleteAllExpiredTokens_shouldCallRepository() {
        passwordTokenServiceImpl.deleteAllExpiredTokens();
        verify(passwordResetTokenRepository).deleteExpiredTokens();
    }
}
