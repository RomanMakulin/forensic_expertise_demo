package com.example.auth.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.keycloak.representations.AccessTokenResponse;

/**
 * DTO для получения информации о пользователе при успешной авторизации
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AuthUserDetailsResponse {
    /**
     * Токен пользователя
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * Токен обновления пользователя.
     * Нужен для серверной логики, игнорируется клиентом
     */
    @JsonIgnore
    private String refreshToken;

    /**
     * ID пользователя
     */
    @JsonProperty("profile_id")
    private String profileId;

    /**
     * Роль пользователя
     */
    @JsonProperty("role")
    private String role;

    public AuthUserDetailsResponse(String accessToken, String profileId, String role) {
        this.accessToken = accessToken;
        this.profileId = profileId;
        this.role = role;
    }
}
