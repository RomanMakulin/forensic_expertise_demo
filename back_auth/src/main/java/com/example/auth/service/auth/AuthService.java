package com.example.auth.service.auth;

import com.example.auth.dto.auth.AuthUserDetailsResponse;
import com.example.auth.model.User;
import com.example.auth.dto.auth.LoginRequest;
import com.example.auth.dto.auth.RegistrationRequest;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Интерфейс для сервиса аутентификации.
 */
public interface AuthService {

    /**
     * Регистрирует нового пользователя.
     *
     * @param request запрос на регистрацию, содержащий информацию о пользователе
     */
    void register(RegistrationRequest request);

    /**
     * Авторизует пользователя.
     *
     * @param request запрос на авторизацию, содержащий информацию о пользователе
     * @return токен пользователя
     */
    AuthUserDetailsResponse login(LoginRequest request);

    /**
     * Подтверждает регистрацию пользователя.
     *
     * @param userId идентификатор пользователя
     */
    void verifyRegistration(UUID userId);

    /**
     * Выходит из системы текущего пользователя.
     */
    void logout();

    /**
     * Получить текущего авторизованного пользователя.
     *
     * @return текущий авторизованный пользователь
     */
    User getAuthenticatedUser();

    /**
     * Обработать код авторизации OAuth 2 для ВК
     *
     * @param code код авторизации
     * @return новые данные авторизации
     */
    AuthUserDetailsResponse handleVkCallback(@NotNull String code);
}
