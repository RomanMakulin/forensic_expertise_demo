package com.example.adminservice.integration.profile;

import com.example.adminservice.dto.profile.original.OriginalProfileDto;
import com.example.adminservice.dto.profileCancel.ProfileCancelForProfile;

import java.util.List;

/**
 * Интерфейс сервиса администратора
 */
public interface ProfileIntegration {

    /**
     * Получить профили всех пользователей
     *
     * @return список пользователей
     */
    List<OriginalProfileDto> requestForAllProfiles();

    /**
     * Получить профили всех пользователей, которые не прошли проверку администратором
     *
     * @return список пользователей
     */
    List<OriginalProfileDto> requestForUnverifiedProfiles();

    /**
     * Подтверждает профиль пользователя
     *
     * @param profileId идентификатор профиля
     */
    void requestForVerifyProfile(String profileId);

    /**
     * Отклонить профиль пользователя
     *
     * @param profileDto объект с некорректными данными профиля
     */
    void requestForCancelVerifyProfile(ProfileCancelForProfile profileDto);

}
