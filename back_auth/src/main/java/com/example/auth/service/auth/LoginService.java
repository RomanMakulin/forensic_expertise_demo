package com.example.auth.service.auth;

import com.example.auth.dto.auth.AuthUserDetailsResponse;
import com.example.auth.dto.auth.LoginRequest;
import jakarta.validation.constraints.NotNull;

/**
 * Интерфейс для сервиса аутентификации.
 */
public interface LoginService {

    /**
     * Метод для аутентификации пользователя.
     *
     * @param request запрос на аутентификацию, содержащий информацию о пользователе
     * @return токен пользователя
     */
    AuthUserDetailsResponse login(LoginRequest request);

    /**
     * Метод для аутентификации пользователя с помощью refresh
     * @param refreshToken токен для получения access токена
     * @return данные авторизации
     */
    AuthUserDetailsResponse loginByRefreshToken(@NotNull String refreshToken);
}
