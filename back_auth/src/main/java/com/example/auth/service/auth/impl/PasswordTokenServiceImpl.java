package com.example.auth.service.auth.impl;

import com.example.auth.model.PasswordResetToken;
import com.example.auth.model.User;
import com.example.auth.repository.PasswordResetTokenRepository;
import com.example.auth.integrations.keycloak.KeycloakManager;
import com.example.auth.service.auth.PasswordTokenService;
import com.example.auth.service.user.UserService;
import exceptions.PasswordResetTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Реализация сервиса для работы с токенами для восстановления пароля.
 */
@Service
public class PasswordTokenServiceImpl implements PasswordTokenService {

    private static final Logger log = LoggerFactory.getLogger(PasswordTokenServiceImpl.class);
    /**
     * Сервис управления пользователями
     */
    private final UserService userService;

    /**
     * Репозиторий для хранения токенов
     */
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    /**
     * Сервис для работы с админкой Keycloak
     */
    private final KeycloakManager keycloakManager;

    private final PasswordResetTokenRepository resetTokenRepository;

    public PasswordTokenServiceImpl(UserService userService,
                                    PasswordResetTokenRepository passwordResetTokenRepository,
                                    KeycloakManager keycloakManager,
                                    PasswordResetTokenRepository resetTokenRepository) {
        this.userService = userService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.keycloakManager = keycloakManager;
        this.resetTokenRepository = resetTokenRepository;
    }

    /**
     * Создание токена для восстановления пароля.
     *
     * @param email электронная почта
     * @return токен
     */
    @Override
    public String createPasswordResetToken(String email) {
        log.debug("Creating password reset token for email {}", email);

        String keycloakId = keycloakManager.getUserByEmail(email).getId();
        User user = userService.getUserByKeycloakId(keycloakId);

        // Генерация токена
        String token = UUID.randomUUID().toString();

        // Срок действия токена (1 час)
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);

        // Создание токена и сохранение в базу
        try {
            PasswordResetToken resetToken = new PasswordResetToken(token, user, expiryDate);
            passwordResetTokenRepository.save(resetToken);
            log.debug("Password reset token created: {}", resetToken);
            return token;
        } catch (PasswordResetTokenException e) {
            throw new PasswordResetTokenException();
        }

    }

    /**
     * Проверка валидности токена.
     *
     * @param token токен
     */
    @Override
    public void validateToken(String token) {
        log.debug("Validating password reset token {}", token);

        // Поиск токена в базе
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        // Проверка срока действия
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new RuntimeException("Время ожидания сброса пароля вышло. Пожалуйста, попробуйте снова");
        }
        log.debug("Password reset token successfully validated: {}", token);
    }

    /**
     * Удаление токена.
     *
     * @param token токен
     */
    @Override
    public void deleteToken(String token) {
        passwordResetTokenRepository.deleteByToken(token);
        log.debug("Password reset token successfully deleted: {}", token);
    }

    /**
     * Получение пользователя по токену.
     *
     * @param token токен
     * @return юзер
     */
    @Override
    public User getUser(String token) {
        return resetTokenRepository.findByToken(token).orElseThrow(() -> new RuntimeException("Invalid token for find user")).getUser();
    }

    /**
     * Удаление всех просроченных токенов.
     */
    @Override
    public void deleteAllExpiredTokens() {
        resetTokenRepository.deleteExpiredTokens();
    }

}
