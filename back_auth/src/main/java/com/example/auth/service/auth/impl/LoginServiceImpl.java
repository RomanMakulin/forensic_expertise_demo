package com.example.auth.service.auth.impl;

import com.example.auth.dto.auth.AuthUserDetailsResponse;
import com.example.auth.dto.auth.LoginRequest;
import com.example.auth.dto.profile.ProfileDTO;
import com.example.auth.integrations.keycloak.KeycloakManager;
import com.example.auth.integrations.profile.ProfileIntegration;
import com.example.auth.model.User;
import com.example.auth.service.auth.LoginService;
import com.example.auth.service.user.UserService;
import com.example.auth.util.JwtParser;
import com.example.auth.util.KeycloakConsts;
import jakarta.validation.constraints.NotNull;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LoginServiceImpl implements LoginService {

    private static final Logger log = LoggerFactory.getLogger(LoginServiceImpl.class);
    private final KeycloakConsts keycloakConsts;
    private final UserService userService;
    private final ProfileIntegration profileIntegration;
    private final KeycloakManager keycloakManager;
    private final RestTemplate restTemplate;

    public LoginServiceImpl(KeycloakConsts keycloakConsts,
                            UserService userService,
                            ProfileIntegration profileIntegration,
                            KeycloakManager keycloakManager) {
        this.keycloakConsts = keycloakConsts;
        this.userService = userService;
        this.profileIntegration = profileIntegration;
        this.keycloakManager = keycloakManager;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public AuthUserDetailsResponse login(LoginRequest request) {
        verifyEmail(request.getEmail());

        Keycloak keycloakForLogin = KeycloakBuilder.builder()
                .serverUrl(keycloakConsts.getAuthServerUrlAdmin())
                .realm(keycloakConsts.getRealm())
                .clientId(keycloakConsts.getResource())
                .clientSecret(keycloakConsts.getSecret())
                .grantType(OAuth2Constants.PASSWORD)
                .username(request.getEmail())
                .password(request.getPassword())
                .build();

        AccessTokenResponse accessTokenResponse = keycloakForLogin.tokenManager().getAccessToken();
        User user = userService.getUserByEmail(request.getEmail());

        ProfileDTO profileDTO = profileIntegration.getProfileRequest(user.getId(), accessTokenResponse.getToken());

        String userRole = JwtParser.extractRoleFromToken(accessTokenResponse.getToken(), keycloakConsts.getResource());

        return new AuthUserDetailsResponse(accessTokenResponse.getToken(), accessTokenResponse.getRefreshToken(), profileDTO.getId(), userRole);
    }

    @Override
    public AuthUserDetailsResponse loginByRefreshToken(@NotNull String refreshToken) {
        // Запрос на получение нового access token
        AccessTokenResponse accessTokenResponse = keycloakManager.refreshToken(refreshToken);

        // Получение ID пользователя из нового access_token
        String keycloakUserId = keycloakManager.getKeycloakUserIdFromToken(accessTokenResponse.getToken());

        UserRepresentation keycloakUser = keycloakManager.getUserById(keycloakUserId);
        User user = userService.getUserByEmail(keycloakUser.getEmail());

        String userRole = JwtParser.extractRoleFromToken(accessTokenResponse.getToken(), keycloakConsts.getResource());

        ProfileDTO profileDTO = profileIntegration.getProfileRequest(user.getId(), accessTokenResponse.getToken());
        return new AuthUserDetailsResponse(accessTokenResponse.getToken(), accessTokenResponse.getRefreshToken(), profileDTO.getId(), userRole);
    }

    /**
     * Проверяет, подтвержден ли адрес электронной почты пользователя.
     *
     * @param email Адрес электронной почты пользователя.
     */
    private void verifyEmail(String email) {
        boolean isVerified = userService.getUserByEmail(email).isVerificationEmail();
        if (!isVerified) {
            throw new RuntimeException("Email is not verified");
        }
    }

}