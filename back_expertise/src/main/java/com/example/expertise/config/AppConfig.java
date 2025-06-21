package com.example.expertise.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Общая конфигурация приложения
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    /**
     * Пути API
     */
    private ApiPaths paths;

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
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        return restTemplate;
    }

    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("PhotoUpload-");
        executor.initialize();
        return executor;
    }

}