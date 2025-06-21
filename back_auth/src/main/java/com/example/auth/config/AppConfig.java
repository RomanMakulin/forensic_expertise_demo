package com.example.auth.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Общая конфигурация приложения
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private String gatewayUrl;               // CORS конфигурация
    private ApiPaths paths;                // Пути API

    @Data
    public static class ApiPaths {
        private Map<String, String> notification;
        private Map<String, String> auth;
        private Map<String, String> frontend;
        private Map<String, String> profile;
        private Map<String, String> minio;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Настраивает Swagger UI
     *
     * @return конфигурация OpenAPI
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("AFE project: auth-service").version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

}