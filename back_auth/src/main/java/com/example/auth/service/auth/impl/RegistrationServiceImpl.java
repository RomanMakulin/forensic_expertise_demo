package com.example.auth.service.auth.impl;

import com.example.auth.config.AppConfig;
import com.example.auth.integrations.profile.ProfileIntegration;
import com.example.auth.model.User;
import com.example.auth.dto.MailRequest;
import com.example.auth.dto.auth.RegistrationRequest;
import com.example.auth.repository.UserRepository;
import com.example.auth.integrations.keycloak.KeycloakManager;
import com.example.auth.integrations.mail.MailIntegration;
import com.example.auth.service.auth.RegistrationService;
import com.example.auth.service.user.UserService;
import exceptions.EmailServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Сервис для регистрации пользователей.
 */
@Service
public class RegistrationServiceImpl implements RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationServiceImpl.class);

    private final AppConfig appConfig;

    private final UserRepository userRepository;
    private final KeycloakManager keycloakManager;
    private final UserService userService;
    private final MailIntegration mailIntegration;
    private final ProfileIntegration profileIntegration;

    public RegistrationServiceImpl(AppConfig appConfig,
                                   UserRepository userRepository,
                                   KeycloakManager keycloakManager,
                                   UserService userService,
                                   MailIntegration mailIntegration,
                                   ProfileIntegration profileIntegration) {
        this.appConfig = appConfig;
        this.userRepository = userRepository;
        this.userService = userService;
        this.keycloakManager = keycloakManager;
        this.mailIntegration = mailIntegration;
        this.profileIntegration = profileIntegration;
    }

    /**
     * Регистрация пользователя.
     *
     * @param request запрос на регистрацию, содержащий информацию о пользователе
     */
    @Override
    public void register(RegistrationRequest request) {
        User user = userService.createUser(request);
        profileIntegration.createProfileRequest(user.getId(), request.getEmail());
        sendVerificationEmail(user);
    }

    /**
     * Отправка письма с подтверждением регистрации.
     *
     * @param user пользователь
     */
    private void sendVerificationEmail(User user) {
        try {
            String apiPath = appConfig.getPaths().getAuth().get("verification-request");

            String endPointUrl = apiPath + "/" + user.getId();

            String message = "<p>Пожалуйста, подтвердите регистрацию на сайте: " +
                    "<a href='" + endPointUrl + "'>Подтвердить</a></p>";

            MailRequest mailRequest = new MailRequest(user.getEmail(), "Подтверждение регистрации", message);

            mailIntegration.publicSendMail(mailRequest);
        } catch (Exception e) {
            log.error("Error sending verification email", e);
            keycloakManager.deleteUserByEmail(user.getEmail()); // удаляем пользователя из Keycloak
            userRepository.delete(user); // удаляем пользователя из БД
            throw new EmailServiceException();
        }

    }

}

