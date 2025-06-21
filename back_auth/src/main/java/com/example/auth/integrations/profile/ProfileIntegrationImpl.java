package com.example.auth.integrations.profile;

import com.example.auth.dto.profile.ProfileDTO;
import com.example.auth.config.AppConfig;
import com.example.auth.integrations.IntegrationHelper;
import com.example.auth.integrations.keycloak.KeycloakManager;
import com.example.auth.service.user.UserService;
import exceptions.UserCreateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

/**
 * Реализация сервиса интеграций с модулем профиля
 */
@Service
public class ProfileIntegrationImpl implements ProfileIntegration {

    private static final Logger log = LoggerFactory.getLogger(ProfileIntegrationImpl.class);
    private final RestTemplate restTemplate;
    private final AppConfig appConfig;
    private final IntegrationHelper integrationHelper;
    private final KeycloakManager keycloakManager;
    private final UserService userService;

    public ProfileIntegrationImpl(RestTemplate restTemplate,
                                  AppConfig appConfig,
                                  IntegrationHelper integrationHelper,
                                  KeycloakManager keycloakManager,
                                  UserService userService) {
        this.restTemplate = restTemplate;
        this.appConfig = appConfig;
        this.integrationHelper = integrationHelper;
        this.keycloakManager = keycloakManager;
        this.userService = userService;
    }

    /**
     * Получает профиль пользователя
     *
     * @param userId идентификатор пользователя
     * @return профиль пользователя
     */
    @Override
    public ProfileDTO getProfileRequest(UUID userId, String token) {
        String baseUrl = appConfig.getPaths().getProfile().get("get-by-user-id");
        String requestUrl = integrationHelper.urlBuilder(baseUrl, Map.of("id", userId.toString()));

        HttpHeaders headers = integrationHelper.createAuthHeaders(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ProfileDTO> response = restTemplate.exchange(requestUrl, HttpMethod.GET, entity, ProfileDTO.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Ошибка при получении профиля. User id: {}, {}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * Создает профиль пользователя - пустой
     */
    @Override
    public void createProfileRequest(UUID userId, String email) {
        String baseUrl = appConfig.getPaths().getProfile().get("create-empty");

        baseUrl = baseUrl + "/" + userId.toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            restTemplate.exchange(baseUrl, HttpMethod.GET, entity, Void.class);
            log.info("Профиль создан.");
        } catch (HttpClientErrorException e) {
            log.error("Ошибка клиента: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Ошибка при создании профиля: {}", e.getMessage());
            keycloakManager.deleteUserByEmail(email);
            userService.deleteUserById(userId);
            throw new UserCreateException();
        }
    }

}
