package com.example.expertise.integration.profile;

import com.example.expertise.dto.profile.ProfileResponseDto;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс интеграции с микросервисом профилей.
 */
public interface ProfileIntegration {

    /**
     * Получение списка ID файлов по типу
     *
     * @param profileId id профиля
     * @param type      тип файла
     * @return список id дополнительных дипломов
     */
    List<String> getFileIdsList(@NotNull(message = "profileId is null") String profileId,
                                @NotNull(message = "type is null") String type);

    /**
     * Получение профиля пользователя
     *
     * @param profileId id профиля пользователя
     * @return данные профиля пользователя
     */
    ProfileResponseDto getProfile(@NotNull UUID profileId);

}
