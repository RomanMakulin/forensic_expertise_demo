package com.example.auth.service.auth;

import com.example.auth.dto.auth.AuthUserDetailsResponse;

/**
 * Интерфейс для взаимодействия с сервисом авторизации через ВКонтакте
 */
public interface OAuthVKService {
    /**
     * Обработка авторизации через ВКонтакте
     *
     * @param code код авторизации
     * @return информация о пользователе
     */
    AuthUserDetailsResponse processVkAuth(String code);
}
