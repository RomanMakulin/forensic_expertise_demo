package com.example.auth.service.auth.impl;

import com.example.auth.config.AppConfig;
import com.example.auth.dto.MailRequest;
import com.example.auth.dto.auth.RegistrationRequest;
import com.example.auth.integrations.keycloak.KeycloakManager;
import com.example.auth.integrations.mail.MailIntegration;
import com.example.auth.integrations.profile.ProfileIntegration;
import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.user.UserService;
import exceptions.EmailServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @Mock
    private AppConfig appConfig;
    @Mock
    private AppConfig.ApiPaths paths;
    @Mock
    private UserRepository userRepository;
    @Mock
    private KeycloakManager keycloakManager;
    @Mock
    private UserService userService;
    @Mock
    private MailIntegration mailIntegration;
    @Mock
    private ProfileIntegration profileIntegration;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    private RegistrationRequest request;
    private User user;

    @BeforeEach
    void setUp() {
        request = new RegistrationRequest();
        request.setEmail("test@example.com");

        user = new User();
        user.setEmail("test@example.com");
        user.setId(UUID.randomUUID());
    }

    @Test
    void register() {
        // when
        when(userService.createUser(request)).thenReturn(user);
        when(appConfig.getPaths()).thenReturn(paths);
        when(paths.getAuth()).thenReturn(Map.of("verification-request", "http://localhost/verify"));

        registrationService.register(request);

        // then
        verify(userService).createUser(request);
        verify(profileIntegration).createProfileRequest(user.getId(), request.getEmail());
        verify(mailIntegration).publicSendMail(any(MailRequest.class));
    }

    @Test
    void register_shouldDeleteUserIfEmailSendFails() {
        // given
        when(userService.createUser(request)).thenReturn(user);
        when(appConfig.getPaths()).thenReturn(paths);
        when(paths.getAuth()).thenReturn(Map.of("verification-request", "http://localhost/verify"));

        doThrow(new RuntimeException("SMTP error")).when(mailIntegration).publicSendMail(any());

        // then
        assertThrows(EmailServiceException.class, () -> registrationService.register(request));

        // verify cleanup
        verify(keycloakManager).deleteUserByEmail(user.getEmail());
        verify(userRepository).delete(user);
    }
}