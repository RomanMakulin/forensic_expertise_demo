package com.example.auth.service.auth.impl;

import com.example.auth.dto.auth.AuthUserDetailsResponse;
import com.example.auth.dto.auth.RegistrationRequest;
import com.example.auth.dto.profile.ProfileDTO;
import com.example.auth.integrations.keycloak.KeycloakManager;
import com.example.auth.integrations.profile.ProfileIntegration;
import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.auth.OAuthVKService;
import com.example.auth.service.user.UserService;
import com.example.auth.util.JwtParser;
import com.example.auth.util.KeycloakConsts;
import exceptions.VkAuthException;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

/**
 * Сервис авторизации через ВКонтакте
 */
@Service
public class OAuthVKServiceImpl implements OAuthVKService {

    private final KeycloakManager keycloakManager;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ProfileIntegration profileIntegration;
    private final KeycloakConsts keycloakConsts;

    public OAuthVKServiceImpl(KeycloakManager keycloakManager,
                              UserService userService,
                              UserRepository userRepository,
                              ProfileIntegration profileIntegration,
                              KeycloakConsts keycloakConsts) {
        this.keycloakManager = keycloakManager;
        this.userService = userService;
        this.userRepository = userRepository;
        this.profileIntegration = profileIntegration;
        this.keycloakConsts = keycloakConsts;
    }

    @Override
    public AuthUserDetailsResponse processVkAuth(String code) {
        AccessTokenResponse tokenResponse = keycloakManager.exchangeCodeForToken(code);

        String keycloakUserId = keycloakManager.getKeycloakUserIdFromToken(tokenResponse.getToken());
        UserRepresentation keycloakUser = keycloakManager.getUserById(keycloakUserId);
        String email = keycloakUser.getEmail();

        // Обработка случая, если email отсутствует
        if (email == null || email.isBlank()) {
            String vkUserId = keycloakUser.getAttributes()
                    .getOrDefault("vk_user_id", Collections.emptyList())
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new VkAuthException("No email or vk_user_id provided by VK"));
            email = vkUserId + "@vk.placeholder";
        }

        User localUser = createUserIfNotExists(email, keycloakUser);

        ProfileDTO profileDTO = profileIntegration.getProfileRequest(localUser.getId(), tokenResponse.getToken());

        String userRole = JwtParser.extractRoleFromToken(tokenResponse.getToken(), keycloakConsts.getResource());

        return new AuthUserDetailsResponse(tokenResponse.getToken(), profileDTO.getId(), userRole);
    }

    /**
     * Создает пользователя в локальной базе данных, если он еще не существует.
     *
     * @param email        адрес электронной почты пользователя.
     * @param keycloakUser пользователь из Keycloak.
     * @return созданный или существующий пользователь.
     */
    private User createUserIfNotExists(String email, UserRepresentation keycloakUser) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            RegistrationRequest request = new RegistrationRequest();
            request.setEmail(email);
            request.setFirstName(keycloakUser.getFirstName());
            request.setLastName(keycloakUser.getLastName());
            request.setPassword(null);

            User newUser = userService.createUserMainTable(request, keycloakUser.getId());
            profileIntegration.createProfileRequest(newUser.getId(), email);
            newUser.setVerificationEmail(true);
            return userRepository.save(newUser);
        });
    }
}
