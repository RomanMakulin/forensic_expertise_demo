package com.example.adminservice.service;

import com.example.adminservice.dto.profile.ProfileDto;
import com.example.adminservice.dto.profileCancel.ProfileCancelFromFront;

import java.util.List;

/**
 * Интерфейс сервиса администратора
 */
public interface AdminService {

    /**
     * Получить список профилей, которые не прошли проверку администратором
     *
     * @return список пользователей
     */
    List<ProfileDto> getUnverifiedProfiles();

    /**
     * Получить список всех профилей
     *
     * @return список пользователей
     */
    List<ProfileDto> getAllProfiles();

    /**
     * Подтверждает профиль пользователя администратором
     *
     * @param profileId идентификатор профиля
     */
    void verifyProfile(String profileId);

    /**
     * Отклоняет профиль пользователя администратором
     *
     * @param profileDto объект с некорректными данными профиля
     */
    void cancelValidationProfile(ProfileCancelFromFront profileDto);

}
