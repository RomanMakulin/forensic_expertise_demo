package com.example.tariff_plans.integration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * Вспомогательный класс для интеграций в части формирования запросов
 */
@Service
public class IntegrationHelper {

    /**
     * Создаем заголовки для авторизации
     *
     * @return заголовки для авторизации
     */
    public HttpHeaders createAuthHeaders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("Невозможно получить JWT из SecurityContext");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt.getTokenValue());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Создание URL с параметрами
     */
    public String urlBuilder(String baseUrl, Map<String, String> params) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl);

        if (params != null) {
            params.forEach(builder::queryParam);
        }

        return builder.toUriString();
    }

}
