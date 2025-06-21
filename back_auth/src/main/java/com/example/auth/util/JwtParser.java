package com.example.auth.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class JwtParser {

    private static final Logger log = LoggerFactory.getLogger(JwtParser.class);

    public static String extractRoleFromToken(String token, String clientId) {
        try {
            // Разбиваем токен на части: Header, Payload, Signature
            String[] tokenParts = token.split("\\.");

            if (tokenParts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token");
            }

            // Декодируем Payload из Base64Url
            String payload = tokenParts[1];
            String decodedPayload = new String(Base64.getUrlDecoder().decode(payload));

            // Преобразуем декодированный JSON в JsonObject
            JsonObject jsonPayload = new com.google.gson.JsonParser().parse(decodedPayload).getAsJsonObject();

            // Извлекаем роли из resource_access
            if (jsonPayload.has("resource_access")) {
                JsonObject resourceAccess = jsonPayload.getAsJsonObject("resource_access");
                if (resourceAccess.has(clientId)) {
                    JsonObject clientRoles = resourceAccess.getAsJsonObject(clientId);
                    if (clientRoles.has("roles")) {
                        // Получаем роли
                        JsonArray roles = clientRoles.getAsJsonArray("roles");
                        return roles.get(0).getAsString(); // Возвращаем первую роль
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Error extracting role from JWT token", e);
        }
        return null;
    }
}
