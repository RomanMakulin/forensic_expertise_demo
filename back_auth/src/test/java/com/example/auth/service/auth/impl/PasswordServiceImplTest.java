package com.example.auth.service.auth.impl;

import com.example.auth.config.AppConfig;
import com.example.auth.dto.MailRequest;
import com.example.auth.dto.auth.ResetPassword;
import com.example.auth.integrations.keycloak.KeycloakManager;
import com.example.auth.integrations.mail.MailIntegration;
import com.example.auth.model.User;
import com.example.auth.service.auth.PasswordTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PasswordServiceImplTest {

    private MailIntegration mailIntegration;
    private PasswordTokenService passwordTokenService;
    private KeycloakManager keycloakManager;
    private AppConfig appConfig;
    private PasswordServiceImpl passwordService;

    @BeforeEach
    void setUp() {
        mailIntegration = mock(MailIntegration.class);
        passwordTokenService = mock(PasswordTokenService.class);
        keycloakManager = mock(KeycloakManager.class);
        appConfig = new AppConfig();


        passwordService = new PasswordServiceImpl(mailIntegration, passwordTokenService, keycloakManager, appConfig);
    }

    @Test
    void resetPasswordRequest_shouldSendMailWithCorrectLink() {
        // arrange
        String email = "user@example.com";
        String token = "abc123";
        when(passwordTokenService.createPasswordResetToken(email)).thenReturn(token);

        // создаём ApiPaths и AppConfig вручную
        AppConfig.ApiPaths apiPaths = new AppConfig.ApiPaths();
        apiPaths.setFrontend(Map.of("recovery-request", "http://frontend/recover"));
        appConfig = new AppConfig();
        appConfig.setPaths(apiPaths);

        passwordService = new PasswordServiceImpl(mailIntegration, passwordTokenService, keycloakManager, appConfig);

        ArgumentCaptor<MailRequest> captor = ArgumentCaptor.forClass(MailRequest.class);

        // act
        passwordService.resetPasswordRequest(email);

        // assert
        verify(mailIntegration).publicSendMail(captor.capture());
        MailRequest mail = captor.getValue();
        assertEquals(email, mail.getTo());
        assertEquals("Reset password", mail.getSubject());
        assertEquals(
                "<p>Для восстановления пароля перейдите по ссылке: <a href='http://frontend/recover?token=abc123'>Восстановить пароль</a></p>",
                mail.getBody()
        );
    }


    @Test
    void resetPassword_shouldCallKeycloakReset() {
        // arrange
        String token = "reset-token";
        String newPassword = "newPass";

        ResetPassword resetPassword = new ResetPassword();
        resetPassword.setToken(token);
        resetPassword.setPassword(newPassword);

        User user = new User();
        user.setKeycloakId("kc-id");

        when(passwordTokenService.getUser(token)).thenReturn(user);

        // act
        passwordService.resetPassword(resetPassword);

        // assert
        verify(passwordTokenService).validateToken(token);
        verify(keycloakManager).resetPassword("kc-id", newPassword);
    }
}
