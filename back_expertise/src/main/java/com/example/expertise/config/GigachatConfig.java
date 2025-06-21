package com.example.expertise.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация приложения для работы с API GigaChat
 */
@Configuration
@Getter
public class GigachatConfig {

    @Value("${gigachat.api.key}")
    private String apiKey;

    @Value("${gigachat.api.url}")
    private String apiUrl;

    @Value("${gigachat.api.client-id}")
    private String clientId;

    @Value("${gigachat.api.auth-header}")
    private String authHeader;

    @Value("${gigachat.api.token-url}")
    private String tokenUrl;

    @Value("${gigachat.api.body-scope}")
    private String bodyScope;

    @Bean
    public String gigachatApiKey() {
        return apiKey;
    }

}
