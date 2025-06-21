package com.example.expertise.integration.gigachat;

import com.example.expertise.config.GigachatConfig;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.UUID;

/**
 * Сервис для получения токена авторизации для API GigaChat
 */
@Service
public class GigaChatAuth {

    /**
     * REST-клиент для выполнения HTTP-запросов
     */
    private final RestTemplate restTemplate;

    /**
     * Конфигурация для доступа к API GigaChat
     */
    private final GigachatConfig gigachatConfig;

    /**
     * Кэшированный токен авторизации
     */
    private String cachedToken;

    /**
     * Время истечения кэшированного токена авторизации
     */
    private long tokenExpirationTime;

    /**
     * Создать новый экземпляр сервиса для работы с API GigaChat
     *
     * @param gigachatConfig конфигурация для доступа к API GigaChat
     * @param restTemplate   REST-клиент для выполнения HTTP-запросов
     */
    public GigaChatAuth(GigachatConfig gigachatConfig, RestTemplate restTemplate) {
        this.gigachatConfig = gigachatConfig;
        this.restTemplate = restTemplate;
    }

    /**
     * Получить токен авторизации для API GigaChat
     *
     * @return токен авторизации для API GigaChat
     */
    public synchronized String getAuthToken() {
        // Проверяем, валиден ли текущий токен
        if (cachedToken == null || System.currentTimeMillis() > tokenExpirationTime) {
            fetchNewToken();
        }
        return cachedToken;
    }

    /**
     * Извлекает токен авторизации из ответа сервера
     */
    private void fetchNewToken() {
        HttpHeaders headers = createHeaders();
        String body = gigachatConfig.getBodyScope();
        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                gigachatConfig.getTokenUrl(), HttpMethod.POST, requestEntity, String.class);

        String json = Objects.requireNonNull(response.getBody());
        cachedToken = extractToken(json);
        tokenExpirationTime = extractExpirationTime(json); // Нужно извлечь expires_in из ответа
    }

    /**
     * Извлекает токен авторизации из JSON-ответа
     *
     * @param json JSON-ответ сервера
     * @return токен авторизации
     */
    private String extractToken(String json) {
        if (json == null || !json.contains("\"access_token\":\"")) {
            throw new IllegalStateException("Ответ от API не содержит access_token: " + json);
        }
        String[] parts = json.split("\"access_token\":\"");
        if (parts.length < 2) {
            throw new IllegalStateException("Некорректный формат ответа от API: " + json);
        }
        return parts[1].split("\"")[0];
    }

    /**
     * Извлекает время истечения токена авторизации из JSON-ответа
     *
     * @param json JSON-ответ сервера
     * @return время истечения токена авторизации
     */
    private long extractExpirationTime(String json) {
        if (json == null || !json.contains("\"expires_at\":")) {
            return System.currentTimeMillis() + (1800 * 1000L) - 300_000L; // 30 минут по умолчанию
        }
        String[] parts = json.split("\"expires_at\":");
        if (parts.length < 2) {
            System.out.println("Некорректный формат expires_at в ответе: " + json);
            return System.currentTimeMillis() + (1800 * 1000L) - 300_000L;
        }
        String expiresAtStr = parts[1].split("}")[0].trim(); // Извлекаем до закрывающей скобки
        long expiresAtMillis = Long.parseLong(expiresAtStr);
        return expiresAtMillis - 300_000L; // Минус 5 минут для запаса
    }

    /**
     * Создает заголовки для HTTP-запроса
     *
     * @return заголовки для HTTP-запроса
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("RqUID", UUID.randomUUID().toString());
        headers.set("Authorization", gigachatConfig.getAuthHeader());
        return headers;
    }

}
