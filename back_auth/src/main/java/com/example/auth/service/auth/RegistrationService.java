package com.example.auth.service.auth;

import com.example.auth.dto.auth.RegistrationRequest;

/**
 * Интерфейс для сервиса регистрации пользователей.
 */
public interface RegistrationService {

    /**
     * Регистрирует нового пользователя.
     *
     * @param request запрос на регистрацию, содержащий информацию о пользователе
     */
    void register(RegistrationRequest request);

}
