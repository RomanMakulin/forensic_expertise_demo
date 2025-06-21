package com.example.auth.service.auth.impl;

import com.example.auth.dto.MailRequest;
import com.example.auth.dto.auth.ResetPassword;
import com.example.auth.config.AppConfig;
import com.example.auth.integrations.keycloak.KeycloakManager;
import com.example.auth.integrations.mail.MailIntegration;
import com.example.auth.service.auth.PasswordService;
import com.example.auth.service.auth.PasswordTokenService;
import org.springframework.stereotype.Service;

@Service
public class PasswordServiceImpl implements PasswordService {

    /**
     * Сервис для отправки писем
     */
    private final MailIntegration mailIntegration;

    /**
     * Сервис для работы с токенами сброса пароля
     */
    private final PasswordTokenService passwordTokenService;

    /**
     * Сервис для работы с админкой Keycloak
     */
    private final KeycloakManager keycloakManager;

    private final AppConfig appConfig;


    public PasswordServiceImpl(MailIntegration mailIntegration,
                               PasswordTokenService passwordTokenService,
                               KeycloakManager keycloakManager,
                               AppConfig appConfig) {
        this.mailIntegration = mailIntegration;
        this.passwordTokenService = passwordTokenService;
        this.keycloakManager = keycloakManager;
        this.appConfig = appConfig;
    }

    /**
     * Запрос на сброс пароля по электронной почте.
     *
     * @param email электронная почта
     */
    @Override
    public void resetPasswordRequest(String email) {

        String token = passwordTokenService.createPasswordResetToken(email);

        String apiPath = appConfig.getPaths().getFrontend().get("recovery-request");

        // Формируем ссылку для восстановления пароля
        String resetLink = apiPath + "?token=" + token;

        // Создаём HTML-письмо с кликабельной ссылкой
        String message = "<p>Для восстановления пароля перейдите по ссылке: " +
                "<a href='" + resetLink + "'>Восстановить пароль</a></p>";

        MailRequest mailRequest = new MailRequest(email, "Reset password", message);

        mailIntegration.publicSendMail(mailRequest);
    }

    /**
     * Сброс пароля.
     *
     * @param resetPassword  объект DTO с данными для сброса пароля
     */
    @Override
    public void resetPassword(ResetPassword resetPassword) {
        passwordTokenService.validateToken(resetPassword.getToken()); // проверяем токен

        String keycloakId = passwordTokenService.getUser(resetPassword.getToken()).getKeycloakId();
        keycloakManager.resetPassword(keycloakId, resetPassword.getPassword());
    }
}
