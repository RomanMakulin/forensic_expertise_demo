package com.example.expertise.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * Помощник для запросов интеграций
 */
@Component
public class IntegrationHelper {

    private static final Logger log = LoggerFactory.getLogger(IntegrationHelper.class);

    private final RestTemplate restTemplate;

    public IntegrationHelper(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Создание HTTP-заголовков с Bearer-токеном для авторизации
     */
    public HttpHeaders createAuthHeaders(MediaType contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        headers.set("Authorization", "Bearer " + getTokenFromRequest());
        return headers;
    }

    /**
     * Создание URL с параметрами
     */
    public String urlBuilder(String baseUrl, Map<String, String> params, boolean encode) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl);

        if (params != null) {
            params.forEach(builder::queryParam);
        }

        UriComponents uriComponents = encode ? builder.build().encode() : builder.build();
        return uriComponents.toUriString();
    }

    // Оригинальный метод по умолчанию — безопасно оставить
    public String urlBuilder(String baseUrl, Map<String, String> params) {
        return urlBuilder(baseUrl, params, true); // по умолчанию кодирует
    }

    /**
     * Получить текущий токен авторизации
     *
     * @return токен авторизации
     */
    public String getTokenFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("Request attributes not found");
        }

        String authorizationHeader = attributes.getRequest().getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalStateException("Authorization header is missing or invalid: " + authorizationHeader);
        }

        return authorizationHeader.substring("Bearer ".length()).trim();
    }

    /**
     * Выполнить запрос с обработкой ошибок
     *
     * @param url           URL запроса
     * @param method        HTTP-метод запроса
     * @param requestEntity Запрос
     * @param responseType  Тип ожидаемого ответа
     * @param errorContext  Контекст ошибки
     * @param <T>           Тип ожидаемого ответа
     * @return Ответ от сервера
     */
    public  <T> ResponseEntity<T> executeRequest(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, String errorContext) {
        try {
            ResponseEntity<T> response = restTemplate.exchange(url, method, requestEntity, responseType);
            if (!response.getStatusCode().is2xxSuccessful()) {
                String errorMsg = String.format("Ошибка при выполнении запроса [%s]: статус %s", errorContext, response.getStatusCode());
                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            return response;
        } catch (HttpClientErrorException e) {
            String errorMsg = String.format("Клиентская ошибка [%s]: %s - %s", errorContext, e.getStatusCode(), e.getResponseBodyAsString());
            log.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } catch (HttpServerErrorException e) {
            String errorMsg = String.format("Серверная ошибка [%s]: %s", errorContext, e.getStatusCode());
            log.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } catch (RestClientException e) {
            String errorMsg = String.format("Неизвестная ошибка [%s]: %s", errorContext, e.getMessage());
            log.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

}
