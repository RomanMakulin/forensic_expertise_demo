package com.example.tariff_plans.integration.profile;

import com.example.tariff_plans.config.AppConfig;
import com.example.tariff_plans.integration.IntegrationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
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

    public ProfileIntegrationImpl(RestTemplate restTemplate,
                                  AppConfig appConfig,
                                  IntegrationHelper integrationHelper) {
        this.restTemplate = restTemplate;
        this.appConfig = appConfig;
        this.integrationHelper = integrationHelper;
    }

    /**
     * Запрос на назначение тарифа конкретному профилю
     *
     * @param profileId ID профиля
     * @param tariffId  ID тарифа
     */
    @Override
    public void chargeTariffRequest(UUID profileId, UUID tariffId) {
        String baseUrl = appConfig.getPaths().getProfile().get("update-plan");

        String requestUrl = integrationHelper.urlBuilder(
                baseUrl,
                Map.of("profileId", profileId.toString(), "planId", tariffId.toString()));

        HttpHeaders headers = integrationHelper.createAuthHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(requestUrl, HttpMethod.POST, entity, Void.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Error while updating tariff plan. Response status: {}", response.getStatusCode().value());
                throw new RuntimeException("Error while updating tariff plan");
            }
            log.info("Tariff plan was successfully updated");
        } catch (RestClientResponseException e) {
            log.error("Error while updating tariff plan. Response status: {}, Body: {}",
                    e.getStatusCode().value(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("Error while updating tariff plan", e);
        } catch (RestClientException e) {
            log.error("Error while updating tariff plan (connection issue)", e);
            throw new RuntimeException("Error while updating tariff plan", e);
        }
    }
    /**
     * Запрос на назначение тарифа конкретному профилю
     * @param profileId ID профиля
     */
    @Override
    public void deletePlanRequest(UUID profileId) {
        String baseUrl = appConfig.getPaths().getProfile().get("delete-plan");

        String requestUrl = integrationHelper.urlBuilder(
                baseUrl,
                Map.of("profileId", profileId.toString()));

        HttpHeaders headers = integrationHelper.createAuthHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(requestUrl, HttpMethod.GET, entity, Void.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Error while deleting tariff plan. Response status: {}", response.getStatusCode().value());
                throw new RuntimeException("Error while deleting tariff plan");
            }
            log.info("Tariff plan was successfully deleted");
        } catch (RestClientResponseException e) {
            log.error("Error while deleting tariff plan. Response status: {}, Body: {}",
                    e.getStatusCode().value(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("Error while deleting tariff plan", e);
        } catch (RestClientException e) {
            log.error("Error while deleting tariff plan (connection issue)", e);
            throw new RuntimeException("Error while deleting tariff plan", e);
        }
    }



}
