package com.example.adminservice.service.impl;

import com.example.adminservice.config.AppConfig;
import com.example.adminservice.dto.profileCancel.ProfileCancelFromFront;
import com.example.adminservice.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 9999) // фиксированный порт для моков
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminServiceIntegrationTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AppConfig appConfig;

    private final String profileId = UUID.randomUUID().toString();

    @BeforeEach
    void setup() {
        // Подменяем адреса внешних сервисов на WireMock
        appConfig.getPaths().getNotification().put("send-mail", "http://localhost:9999/mock-mail");
        appConfig.getPaths().getProfile().put("cancel-validation", "http://localhost:9999/api/profile/cancel-validation");
        appConfig.getPaths().getProfile().put("get-all-profiles", "http://localhost:9999/api/profile/get-all-profiles");
        appConfig.getPaths().getProfile().put("verify-profile", "http://localhost:9999/api/profile/verify");


        // Ставим фейковый JWT
        Jwt jwt = Jwt.withTokenValue("test-token")
                .claim("sub", "admin")
                .header("alg", "none")
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(jwt, jwt)
        );
    }

    @Test
    void cancelValidationProfile_shouldCallExternalServices() {
        // given
        ProfileCancelFromFront request = new ProfileCancelFromFront();
        request.setProfileId(profileId);
        request.setUserMail("test@example.com");
        request.setNeedDiplomaDelete(false);
        request.setNeedPassportDelete(false);

        // Мокаем внешние вызовы
        stubFor(post(urlEqualTo("/mock-mail"))
                .willReturn(aResponse().withStatus(200)));

        stubFor(post(urlEqualTo("/api/profile/cancel-validation"))
                .willReturn(aResponse().withStatus(200)));

        // when
        adminService.cancelValidationProfile(request);

        // then
        verify(postRequestedFor(urlEqualTo("/mock-mail")));
        verify(postRequestedFor(urlEqualTo("/api/profile/cancel-validation")));
    }

    @Test
    void getAllProfiles_shouldCallProfileService() {
        // given
        stubFor(get(urlEqualTo("/api/profile/get-all-profiles"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));

        // when
        adminService.getAllProfiles();

        // then
        verify(getRequestedFor(urlEqualTo("/api/profile/get-all-profiles")));
    }

    @Test
    void verifyProfile_shouldCallProfileService() {
        stubFor(get(urlEqualTo("/api/profile/verify/" + profileId))
                .willReturn(aResponse().withStatus(200)));

        // when
        adminService.verifyProfile(profileId);

        // then
        verify(getRequestedFor(urlEqualTo("/api/profile/verify/" + profileId)));
    }

}
