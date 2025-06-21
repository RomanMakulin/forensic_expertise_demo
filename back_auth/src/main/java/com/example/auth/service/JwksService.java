package com.example.auth.service;

import com.example.auth.util.KeycloakConsts;
import com.nimbusds.jose.jwk.JWKSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

/**
 * Сервис для загрузки JWKS (JSON Web Key Set) из Keycloak.
 */
@Service
public class JwksService {
    private static final Logger log = LoggerFactory.getLogger(JwksService.class);
    private final KeycloakConsts keycloakConsts;

    /**
     * Конструктор класса JwksService.
     *
     * @param keycloakConsts объект KeycloakConsts, содержащий константы Keycloak.
     */
    public JwksService(KeycloakConsts keycloakConsts) {
        this.keycloakConsts = keycloakConsts;
    }

    /**
     * Загружает JWKS из Keycloak.
     *
     * @return объект JWKSet, содержащий ключи JWKS.
     * @throws IOException если возникает ошибка при загрузке JWKS.
     */
    @Cacheable("jwks")
    public JWKSet loadJwks() throws IOException {
        String jwksUrl = keycloakConsts.getAuthServerUrlAdmin() + "/realms/" + keycloakConsts.getRealm() + "/protocol/openid-connect/certs";
        log.info("Loading JWKS from {}", jwksUrl);
        try {
            return JWKSet.load(new URL(jwksUrl));
        } catch (IOException | ParseException e) {
            log.error("Failed to load JWKS from {}: {}", jwksUrl, e.getMessage());
            throw new IOException("Unable to load JWKS", e);
        }
    }
}
