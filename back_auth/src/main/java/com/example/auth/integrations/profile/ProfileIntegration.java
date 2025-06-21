package com.example.auth.integrations.profile;

import com.example.auth.dto.profile.ProfileDTO;

import java.util.UUID;

/**
 * Интерфейс интеграции с модулем профиля
 */
public interface ProfileIntegration {

    /**
     * Получает профиль пользователя
     *
     * @param userId идентификатор пользователя
     * @return профиль пользователя
     */
    ProfileDTO getProfileRequest(UUID userId, String token);

    /**
     * Создает профиль пользователя - пустой
     */
    void createProfileRequest(UUID userId, String email);

}
