package com.example.adminservice.integration.mail;

import com.example.adminservice.config.AppConfig;
import com.example.adminservice.integration.mail.dto.MailRequest;
import org.junit.jupiter.api.Assertions;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 9999)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MailIntegrationImplTest {

    @Autowired
    private MailIntegration mailIntegration;

    @Autowired
    private AppConfig appConfig;

    @BeforeEach
    void setup() {
        appConfig.getPaths().getNotification().put("send-mail", "http://localhost:9999/mock-mail");

        // Устанавливаем фейковую аутентификацию, чтобы IntegrationHelper мог получить JWT
        Jwt jwt = Jwt.withTokenValue("test-token")
                .claim("sub", "admin")
                .header("alg", "none")
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(jwt, jwt)
        );
    }

    @Test
    void sendMail_shouldCallNotificationService() {
        // given
        stubFor(post(urlEqualTo("/mock-mail"))
                .willReturn(aResponse().withStatus(200)));

        MailRequest request = new MailRequest();
        request.setTo("test@example.com");
        request.setSubject("Тема");
        request.setBody("Сообщение");

        // when
        mailIntegration.sendMail(request);

        // then
        verify(postRequestedFor(urlEqualTo("/mock-mail"))
                .withHeader("Authorization", matching("Bearer .*")));
    }

    @Test
    void sendMail_shouldThrowExceptionOnError() {
        // given
        stubFor(post(urlEqualTo("/mock-mail"))
                .willReturn(aResponse().withStatus(500)));

        MailRequest request = new MailRequest();
        request.setTo("fail@example.com");
        request.setSubject("Ошибка");
        request.setBody("Ошибка");

        // expect
        Assertions.assertThrows(RuntimeException.class, () -> {
            mailIntegration.sendMail(request);
        });
    }
}
