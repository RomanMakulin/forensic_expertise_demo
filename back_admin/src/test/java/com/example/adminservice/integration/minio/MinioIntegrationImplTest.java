package com.example.adminservice.integration.minio;

import com.example.adminservice.config.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 9999)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MinioIntegrationImplTest {

    @Autowired
    private MinioIntegration minioIntegration;

    @Autowired
    private AppConfig appConfig;

    @BeforeEach
    void setup() {
        appConfig.getPaths().getMinio().put("get-file-by-params", "http://localhost:9999/api/minio/file");
        appConfig.getPaths().getMinio().put("delete-file-by-params", "http://localhost:9999/api/minio/delete");

        Jwt jwt = Jwt.withTokenValue("test-token")
                .claim("sub", "admin")
                .header("alg", "none")
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(jwt, jwt)
        );
    }

    @Test
    void getFileRequest_shouldReturnUrlString() {
        // given
        stubFor(get(urlPathEqualTo("/api/minio/file"))
                .withQueryParam("fileName", equalTo("test-file"))
                .withQueryParam("fileExtension", equalTo("jpg"))
                .withQueryParam("fileBucket", equalTo("photos"))
                .withQueryParam("link", equalTo("true"))
                .willReturn(okJson("\"http://localhost:9999/fake-url.jpg\"")));

        // when
        String result = minioIntegration.getFileRequest(
                "test-file", "jpg", "photos", true, String.class);

        // then
        assertEquals("http://localhost:9999/fake-url.jpg", result.replaceAll("\"", ""));
    }

    @Test
    void deleteFileRequest_shouldSucceed() {
        // given
        stubFor(delete(urlPathEqualTo("/api/minio/delete"))
                .withQueryParam("fileName", equalTo("test-file"))
                .withQueryParam("fileExtension", equalTo("jpg"))
                .withQueryParam("fileBucket", equalTo("photos"))
                .willReturn(aResponse().withStatus(200)));

        // when & then (should not throw)
        assertDoesNotThrow(() ->
                minioIntegration.deleteFileRequest("test-file", "jpg", "photos"));
    }
}
