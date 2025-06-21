package com.example.auth.service.auth;

import com.example.auth.model.User;

/**
 * Интерфейс сервиса для работы с токенами
 */
public interface PasswordTokenService {

    /**
     * Создает токен для сброса пароля.
     *
     * @param email email пользователя
     * @return токен
     */
    String createPasswordResetToken(String email);

    /**
     * Проверяет валидность токена.
     *
     * @param token токен
     */
    void validateToken(String token);

    /**
     * Удаляет токен.
     *
     * @param token токен
     */
    void deleteToken(String token);

    /**
     * Получает пользователя по токену.
     *
     * @param token токен
     * @return пользователь
     */
    User getUser(String token);

    /**
     * Удаляет все просроченные токены.
     */
    void deleteAllExpiredTokens();

}
