package com.example.adminservice.integration;

import com.example.adminservice.exceptions.IntegrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * Вспомогательный сервис для интеграций с другими сервисам
 */
@Service
public class IntegrationHelper {

    private static final Logger log = LoggerFactory.getLogger(IntegrationHelper.class);
    private final RestTemplate restTemplate;

    public IntegrationHelper(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Создание HTTP-заголовков с Bearer-токеном для авторизации
     */
    public HttpHeaders createAuthHeaders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("Невозможно получить JWT из SecurityContext");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt.getTokenValue());
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

    /**
     * Универсальный метод для отправки HTTP-запросов (GET, POST, PUT, DELETE и т. д.)
     *
     * @param requestUrl   URL запроса
     * @param method       HTTP-метод (GET, POST, PUT, DELETE и т. д.)
     * @param requestBody  Тело запроса (может быть null для GET/DELETE)
     * @param responseType Класс ожидаемого ответа
     * @return Объект ответа
     */
    public <T, R> R sendRequest(String requestUrl, HttpMethod method, T requestBody, Class<R> responseType) {
        return executeRequest(requestUrl, method, requestBody, responseType, null);
    }

    /**
     * Универсальный метод для отправки HTTP-запросов (GET, POST, PUT, DELETE и т. д.) с сложными типами ответа
     *
     * @param requestUrl   URL запроса
     * @param method       HTTP-метод (GET, POST, PUT, DELETE и т. д.)
     * @param requestBody  Тело запроса (может быть null для GET/DELETE)
     * @param responseType Тип ожидаемого ответа (используется `ParameterizedTypeReference`)
     * @return Объект ответа
     */
    public <T, R> R sendRequest(String requestUrl, HttpMethod method, T requestBody, ParameterizedTypeReference<R> responseType) {
        return executeRequest(requestUrl, method, requestBody, null, responseType);
    }

    /**
     * Выполнение HTTP-запроса
     *
     * @param requestUrl   URL запроса
     * @param method       HTTP-метод (GET, POST, PUT, DELETE и т. д.)
     * @param requestBody  Тело запроса (может быть null для GET/DELETE)
     * @param responseType Класс ожидаемого ответа (используется, если ответ — это простой объект)
     * @param typeReference Тип ожидаемого ответа (используется, если ответ — это сложный объект, например, `List<T>`)
     * @return Объект ответа
     */
    private <T, R> R executeRequest(String requestUrl, HttpMethod method, T requestBody,
                                    Class<R> responseType, ParameterizedTypeReference<R> typeReference) {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<T> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<R> response;
            if (responseType != null) {
                response = restTemplate.exchange(requestUrl, method, entity, responseType);
            } else if (typeReference != null) {
                response = restTemplate.exchange(requestUrl, method, entity, typeReference);
            } else {
                throw new IllegalArgumentException("Не указан тип ответа");
            }

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Успешный запрос {} {}: статус {}", method, requestUrl, response.getStatusCode());
                return response.getBody();
            } else {
                log.error("Ошибка запроса {} {}: статус {}, тело ответа {}", method, requestUrl, response.getStatusCode(), response.getBody());
                throw new IntegrationException("Ошибка выполнения запроса: " + response.getStatusCode());
            }
        } catch (RestClientResponseException e) {
            log.error("Ошибка API {} {}: статус {}, ответ: {}", method, requestUrl, e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new IntegrationException("Ошибка API: " + e.getStatusCode().value() + " " + e.getMessage(), e);
        } catch (RestClientException e) {
            log.error("Ошибка соединения с сервисом {} {}: {}", method, requestUrl, e.getMessage());
            throw new IntegrationException("Ошибка соединения с сервисом", e);
        }
    }




}
