package com.example.expertise.integration.profile;

import com.example.expertise.config.AppConfig;
import com.example.expertise.dto.profile.ProfileResponseDto;
import com.example.expertise.integration.IntegrationHelper;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Реализация сервиса для взаимодействия с модулем профилей
 */
@Service
public class ProfileIntegrationImpl implements ProfileIntegration {

    private static final Logger log = LoggerFactory.getLogger(ProfileIntegrationImpl.class);
    private final IntegrationHelper integrationHelper;
    private final AppConfig appConfig;

    public ProfileIntegrationImpl(IntegrationHelper integrationHelper, AppConfig appConfig) {
        this.integrationHelper = integrationHelper;
        this.appConfig = appConfig;
    }

    /**
     * Возвращает список id файлов (по типу) по профилю
     *
     * @param profileId id профиля
     * @return список id дополнительных дипломов
     */
    @Override
    public List<String> getFileIdsList(@NotNull(message = "profileId is null") String profileId,
                                       @NotNull(message = "type is null") String type) {
        String requestUrl = integrationHelper.urlBuilder(appConfig.getPaths().getProfile().get("get-files-id-list"),
                Map.of("profileId", profileId, "type", type));

        HttpHeaders headers = integrationHelper.createAuthHeaders(null);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String[]> response = integrationHelper.executeRequest(
                requestUrl, HttpMethod.GET, requestEntity, String[].class, "Ошибка получения списка id файлов по профилю: " + profileId + " по типу: " + type);

        String[] body = response.getBody();
        return body != null ? Arrays.asList(body) : Collections.emptyList();
    }

    /**
     * Возвращает профиль пользователя
     *
     * @param profileId id профиля пользователя
     * @return профиль пользователя
     */
    @Override
    public ProfileResponseDto getProfile(UUID profileId) {
        String requestUrl = integrationHelper.urlBuilder(appConfig.getPaths().getProfile().get("get-by-profile-id"), Map.of("id", profileId.toString()));

        HttpHeaders headers = integrationHelper.createAuthHeaders(null);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<ProfileResponseDto> response = integrationHelper.executeRequest(
                requestUrl, HttpMethod.GET, requestEntity, ProfileResponseDto.class, "Ошибка получения профиля по id: " + profileId);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Получены данные профиля: {}", response.getBody());
            return response.getBody();
        } else {
            throw new RuntimeException("Не удалось получить профиль по id: " + profileId);
        }
    }
}
