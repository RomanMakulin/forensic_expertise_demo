package com.example.auth.service.auth.impl;

import com.example.auth.dto.auth.AuthUserDetailsResponse;
import com.example.auth.dto.profile.ProfileDTO;
import com.example.auth.integrations.profile.ProfileIntegration;
import com.example.auth.model.User;
import com.example.auth.dto.auth.LoginRequest;
import com.example.auth.dto.auth.RegistrationRequest;
import com.example.auth.repository.UserRepository;
import com.example.auth.integrations.keycloak.KeycloakManager;
import com.example.auth.service.auth.AuthService;
import com.example.auth.service.auth.LoginService;
import com.example.auth.service.auth.OAuthVKService;
import com.example.auth.service.auth.RegistrationService;
import com.example.auth.service.user.UserService;
import jakarta.validation.constraints.NotNull;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final RegistrationService registrationService;
    private final LoginService loginService;
    private final UserRepository userRepository;
    private final KeycloakManager keycloakManager;
    private final UserService userService;
    private final OAuthVKService oAuthVKService;

    public AuthServiceImpl(RegistrationService registrationService,
                           LoginService loginService,
                           UserRepository userRepository,
                           @Lazy KeycloakManager keycloakManager,
                           UserService userService,
                           OAuthVKService oAuthVKService) {
        this.registrationService = registrationService;
        this.loginService = loginService;
        this.userRepository = userRepository;
        this.keycloakManager = keycloakManager;
        this.userService = userService;
        this.oAuthVKService = oAuthVKService;
    }

    @Override
    public void register(RegistrationRequest request) {
        registrationService.register(request);
    }

    @Override
    public AuthUserDetailsResponse login(LoginRequest request) {
        return loginService.login(request);
    }

    @Override
    public void verifyRegistration(UUID userId) {
        User user = userService.getUserById(userId);
        user.setVerificationEmail(true);
        userRepository.save(user);
        log.info("Регистрация пользователя {} была успешно проверена (email).", user.getEmail());
    }

    @Override
    public void logout() {
        keycloakManager.logoutUserById(getAuthenticatedUser().getKeycloakId());
    }

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            throw new RuntimeException("No authenticated user found");
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String email = jwt.getClaim("email");
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    @Override
    public AuthUserDetailsResponse handleVkCallback(@NotNull String code) {
        return oAuthVKService.processVkAuth(code);
    }

}