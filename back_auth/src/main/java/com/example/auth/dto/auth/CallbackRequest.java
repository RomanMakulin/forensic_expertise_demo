package com.example.auth.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO для запроса авторизации через OAuth2.0.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CallbackRequest {

    @NotNull
    private String code;

    @JsonProperty("redirect_uri")
    @NotNull
    private String redirectUri;
}
