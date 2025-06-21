package com.example.auth.service.auth.impl;

import com.example.auth.dto.auth.AuthUserDetailsResponse;
import com.example.auth.dto.auth.LoginRequest;
import com.example.auth.dto.profile.ProfileDTO;
import com.example.auth.integrations.keycloak.KeycloakManager;
import com.example.auth.integrations.profile.ProfileIntegration;
import com.example.auth.model.User;
import com.example.auth.service.user.UserService;
import com.example.auth.util.JwtParser;
import com.example.auth.util.KeycloakConsts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {

    @Mock
    private KeycloakConsts keycloakConsts;

    @Mock
    private UserService userService;

    @Mock
    private ProfileIntegration profileIntegration;

    @Mock
    private KeycloakManager keycloakManager;

    @InjectMocks
    private LoginServiceImpl loginService;

    private LoginRequest loginRequest;
    private User user;

    private final UUID profileId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        user = new User();
        user.setEmail("test@example.com");
        user.setVerificationEmail(true);
        user.setId(profileId);
    }

    @Test
    void login_shouldReturnAuthUserDetailsResponse_whenCredentialsAreCorrect() {
        // arrange
        AccessTokenResponse tokenResponse = new AccessTokenResponse();
        tokenResponse.setToken("access-token");
        tokenResponse.setRefreshToken("refresh-token");

        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setId(profileId.toString());

        when(userService.getUserByEmail("test@example.com")).thenReturn(user);
        when(keycloakConsts.getAuthServerUrlAdmin()).thenReturn("http://localhost:8080");
        when(keycloakConsts.getRealm()).thenReturn("realm");
        when(keycloakConsts.getResource()).thenReturn("client-id");
        when(keycloakConsts.getSecret()).thenReturn("client-secret");

        // мок Keycloak
        Keycloak keycloakMock = mock(Keycloak.class, RETURNS_DEEP_STUBS);
        when(keycloakMock.tokenManager().getAccessToken()).thenReturn(tokenResponse);

        // мокируем статический билдер
        try (var mockStatic = mockStatic(KeycloakBuilder.class)) {
            KeycloakBuilder builderMock = mock(KeycloakBuilder.class, RETURNS_DEEP_STUBS);
            when(builderMock.serverUrl("http://localhost:8080")).thenReturn(builderMock);
            when(builderMock.realm("realm")).thenReturn(builderMock);
            when(builderMock.clientId("client-id")).thenReturn(builderMock);
            when(builderMock.clientSecret("client-secret")).thenReturn(builderMock);
            when(builderMock.grantType("password")).thenReturn(builderMock);
            when(builderMock.username("test@example.com")).thenReturn(builderMock);
            when(builderMock.password("password")).thenReturn(builderMock);
            when(builderMock.build()).thenReturn(keycloakMock);

            mockStatic.when(KeycloakBuilder::builder).thenReturn(builderMock);

            // статик JwtParser
            try (var jwtMock = mockStatic(JwtParser.class)) {
                jwtMock.when(() -> JwtParser.extractRoleFromToken("access-token", "client-id")).thenReturn("user");

                when(profileIntegration.getProfileRequest(profileId, "access-token")).thenReturn(profileDTO);

                // act
                AuthUserDetailsResponse result = loginService.login(loginRequest);

                // assert
                assertNotNull(result);
                assertEquals("access-token", result.getAccessToken());
                assertEquals("refresh-token", result.getRefreshToken());
                assertEquals(profileId.toString(), result.getProfileId());
                assertEquals("user", result.getRole());
            }
        }
    }

    @Test
    void login_shouldThrowException_ifEmailNotVerified() {
        // arrange
        user.setVerificationEmail(false);
        when(userService.getUserByEmail("test@example.com")).thenReturn(user);

        // act & assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> loginService.login(loginRequest));
        assertEquals("Email is not verified", ex.getMessage());
    }
}
