package com.example.auth.config;

import com.example.auth.util.KeycloakConsts;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Keycloak.
 */
@Configuration
public class KeycloakConfig {

    /**
     * Переменные для хранения конфигурации Keycloak.
     */
    private final KeycloakConsts keycloakConsts;

    public KeycloakConfig(KeycloakConsts keycloakConsts) {
        this.keycloakConsts = keycloakConsts;
    }

    /**
     * Создание клиента Keycloak для администрирования.
     *
     * @return клиент Keycloak для администрирования
     */
    @Bean
    public Keycloak keycloakAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakConsts.getAuthServerUrlAdmin())
                .realm(keycloakConsts.getRealmAdmin())
                .clientId(keycloakConsts.getClientIdAdmin())
                .username(keycloakConsts.getUsernameAdmin())
                .password(keycloakConsts.getPasswordAdmin())
                .build();
    }

}
