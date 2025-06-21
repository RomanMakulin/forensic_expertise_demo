package com.example.auth.integrations.keycloak.impl;

import com.example.auth.dto.auth.RegistrationRequest;
import com.example.auth.integrations.keycloak.KeycloakManager;
import com.example.auth.service.JwksService;
import com.example.auth.util.KeycloakConsts;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import exceptions.CannotFindKeycloakUserException;
import exceptions.KeycloakUserCreatException;
import exceptions.ResetPasswordException;
import exceptions.UserAlreadyExistsException;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с админкой Keycloak.
 */
@Service
public class KeycloakManagerImpl implements KeycloakManager {

    private static final Logger log = LoggerFactory.getLogger(KeycloakManagerImpl.class);
    private final Keycloak keycloak;
    private final KeycloakConsts keycloakConsts;
    private final RestTemplate restTemplate;
    private final JwksService jwksService;

    public KeycloakManagerImpl(Keycloak keycloak, KeycloakConsts keycloakConsts, RestTemplate restTemplate, JwksService jwksService) {
        this.keycloak = keycloak;
        this.keycloakConsts = keycloakConsts;
        this.restTemplate = restTemplate;
        this.jwksService = jwksService;
    }

    /**
     * Создаёт пользователя Keycloak.
     *
     * @param request данные пользователя для регистрации (DTO)
     * @return представление пользователя
     */
    @Transactional
    @Override
    public void createKeycloakUser(RegistrationRequest request) {
        try {
            // Создаём объект пользователя для Keycloak
            UserRepresentation userRepresentation = fillKeycloakNewUser(request);

            UsersResource usersResource = keycloak.realm(keycloakConsts.getRealm()).users();
            Response response = usersResource.create(userRepresentation);

            int status = response.getStatus();
            response.close();

            if (status == 201) {
                log.info("User successfully created in Keycloak table: {}", userRepresentation);
            } else if (status == 409) {
                log.error("User already exists in Keycloak table: {}", userRepresentation);
                throw new UserAlreadyExistsException("Такой пользователь уже существует (409).");
            } else {
                throw new RuntimeException("Keycloak user creation failed. Status: " + status);
            }
        } catch (KeycloakUserCreatException e) {
            log.error("Error creating user (keycloak). Message: {}", e.getMessage());
            throw new KeycloakUserCreatException();
        }
    }

    /**
     * Создание пользователя в Keycloak.
     *
     * @param request запрос на регистрацию (DTO)
     * @return созданный пользователь в Keycloak (UserRepresentation)
     */
    private UserRepresentation fillKeycloakNewUser(RegistrationRequest request) {
        // Создаём пользователя в Keycloak
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(request.getEmail());  // так как вход стоит по email
        userRepresentation.setEmail(request.getEmail());
        userRepresentation.setFirstName(request.getFirstName());
        userRepresentation.setLastName(request.getLastName());
        // сюда же можно добавить роль
        userRepresentation.setEnabled(true);

        // Установим пароль
        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setTemporary(false);
        cred.setValue(request.getPassword());

        userRepresentation.setCredentials(List.of(cred));

        return userRepresentation;
    }

    /**
     * Удаление пользователя из Keycloak
     *
     * @param keycloakUserId id пользователя
     */
    @Transactional
    @Override
    public void deleteUserById(String keycloakUserId) {
        try {
            keycloak.realm(keycloakConsts.getRealm())
                    .users()
                    .delete(keycloakUserId);
        } catch (Exception e) {
            log.error("Error deleting user (keycloak). Message: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Удаление пользователя из Keycloak по соответствующему email
     *
     * @param email почта пользователя
     */
    @Transactional
    @Override
    public void deleteUserByEmail(String email) {
        UserRepresentation user = getUserByEmail(email);
        deleteUserById(user.getId());
    }

    /**
     * Получить пользователя по id юзера Keycloak
     *
     * @param keycloakUserId id пользователя
     * @return юзер Keycloak
     */
    @Override
    public UserRepresentation getUserById(String keycloakUserId) {
        try {
            return keycloak.realm(keycloakConsts.getRealm())
                    .users()
                    .get(keycloakUserId)
                    .toRepresentation();
        } catch (Exception e) {
            log.error("Error retrieving user (keycloak). Message: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Сбросить пароль пользователя (установить новый).
     *
     * @param keycloakId  идентификатор пользователя
     * @param newPassword новый пароль
     */
    @Transactional
    @Override
    public void resetPassword(String keycloakId, String newPassword) {
        try {
            CredentialRepresentation cred = new CredentialRepresentation();
            cred.setType(CredentialRepresentation.PASSWORD);
            cred.setValue(newPassword);
            cred.setTemporary(false); // не временный пароль

            UserRepresentation user = getUserById(keycloakId);

            // Устанавливаем новый пароль через Keycloak Admin API
            keycloak.realm(keycloakConsts.getRealm())
                    .users()
                    .get(user.getId()) // ID пользователя
                    .resetPassword(cred);

            log.info("Password reset for user: {}", user.getEmail());
        } catch (ResetPasswordException e) {
            log.error("Error updating password (keycloak). Message: {}", e.getMessage());
            throw new ResetPasswordException();
        }
    }

    /**
     * Поиск пользователей по подстроке (username, email и т.п.)
     * Например, поиск "test" вернёт всех пользователей, у которых в имени/юзернейме/почте есть "test".
     *
     * @param searchString строка поиска
     */
    @Override
    public List<UserRepresentation> findUsers(String searchString) {
        return keycloak.realm(keycloakConsts.getRealm())
                .users()
                .search(searchString);
    }

    @Override
    public UserRepresentation getUserByEmail(String email) {

        // Общий список совпадений
        List<UserRepresentation> usersList = keycloak.realm(keycloakConsts.getRealm())
                .users()
                .search(email);

        // пользователя с таким email нет
        if (usersList.isEmpty()) {
            throw new CannotFindKeycloakUserException("No keycloak user found with email: " + email);
        }

        // Находим точное совпадение
        Optional<UserRepresentation> exactMatch = usersList.stream()
                .filter(u -> email.equalsIgnoreCase(u.getEmail()))
                .findFirst();

        if (exactMatch.isPresent()) {
            return exactMatch.get();
        } else {
            throw new CannotFindKeycloakUserException("No keycloak user found with email: " + email);
        }
    }

    /**
     * Обновить данные пользователя (firstName, lastName, email, enabled...)
     * Параметр user - это готовый UserRepresentation, у которого установлен ID (user.getId()) и новые поля.
     *
     * @param user данные юзера
     */
    @Transactional
    @Override
    public void updateUser(UserRepresentation user) {
        try {
            keycloak.realm(keycloakConsts.getRealm())
                    .users()
                    .get(user.getId())
                    .update(user);
        } catch (Exception e) {
            log.error("Error updating user (keycloak). Message: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Пример назначения realm-роли пользователю (по имени роли).
     * В данном случае ищем роль на уровне Realm, а не Client.
     *
     * @param keycloakUserId id пользователя
     * @param roleName       название роли
     */
    @Transactional
    @Override
    public void assignRealmRole(String keycloakUserId, String roleName) {
        try {
            RoleRepresentation role = keycloak.realm(keycloakConsts.getRealm())
                    .roles()
                    .get(roleName)
                    .toRepresentation();

            keycloak.realm(keycloakConsts.getRealm())
                    .users()
                    .get(keycloakUserId)
                    .roles()
                    .realmLevel()
                    .add(Collections.singletonList(role));
        } catch (Exception e) {
            log.error("Error assigning role (keycloak). Message: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Удалить (отобрать) realm-роль у пользователя.
     *
     * @param keycloakUserId id пользователя
     * @param roleName       название роли
     */
    @Transactional
    @Override
    public void removeRealmRole(String keycloakUserId, String roleName) {
        try {
            RoleRepresentation role = keycloak.realm(keycloakConsts.getRealm())
                    .roles()
                    .get(roleName)
                    .toRepresentation();

            keycloak.realm(keycloakConsts.getRealm())
                    .users()
                    .get(keycloakUserId)
                    .roles()
                    .realmLevel()
                    .remove(Collections.singletonList(role));
        } catch (Exception e) {
            log.error("Error removing role (keycloak). Message: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Получить все realm-роли, которые назначены пользователю.
     *
     * @param keycloakUserId id пользователя
     */
    @Override
    public List<RoleRepresentation> getUserRealmRoles(String keycloakUserId) {
        return keycloak.realm(keycloakConsts.getRealm())
                .users()
                .get(keycloakUserId)
                .roles()
                .realmLevel()
                .listAll();
    }

    /**
     * Выход пользователя из системы.
     *
     * @param keycloakUserId идентификатор пользователя
     */
    @Transactional
    @Override
    public void logoutUserById(String keycloakUserId) {
        try {
            keycloak.realm(keycloakConsts.getRealm())
                    .users()
                    .get(keycloakUserId)
                    .logout();
            log.info("User {} logged out", keycloakUserId);
        } catch (Exception e) {
            log.error("Error logging out user (keycloak). Message: {}", e.getMessage());
        }
    }

    @Override
    public AccessTokenResponse refreshToken(String refreshToken) {
        try {
            String tokenUrl = keycloakConsts.getAuthServerUrlAdmin() + "/realms/" + keycloakConsts.getRealm() + "/protocol/openid-connect/token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", OAuth2Constants.REFRESH_TOKEN);
            body.add("client_id", keycloakConsts.getResource());
            body.add("client_secret", keycloakConsts.getSecret());
            body.add("refresh_token", refreshToken);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            AccessTokenResponse accessTokenResponse = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, AccessTokenResponse.class).getBody();

            if (accessTokenResponse == null || accessTokenResponse.getToken() == null) {
                log.error("Failed to refresh token: empty response or no access token");
                throw new RuntimeException("Token refresh failed");
            }

            return accessTokenResponse;
        } catch (Exception e) {
            log.error("Failed to refresh token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to refresh token: " + e.getMessage(), e);
        }
    }

    @Override
    public AccessTokenResponse exchangeCodeForToken(String code) {
        try {
            String tokenUrl = keycloakConsts.getAuthServerUrlAdmin() + "/realms/" + keycloakConsts.getRealm() + "/protocol/openid-connect/token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("client_id", keycloakConsts.getResource());
            body.add("client_secret", keycloakConsts.getSecret());
            body.add("code", code);
            body.add("redirect_uri", keycloakConsts.getRedirectUri());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, AccessTokenResponse.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("Failed to exchange code: status {}, response: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Token exchange failed");
            }
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to exchange code for token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to exchange code: " + e.getMessage(), e);
        }
    }

    /**
     * Получает ID пользователя из JWT-токена с проверкой подписи.
     *
     * @param token Токен.
     * @return ID пользователя.
     */
    @Override
    public String getKeycloakUserIdFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            // Используем кэшированный JWKS через JwksService
            JWKSet jwkSet = jwksService.loadJwks();

            // Найти RSA-ключ по key ID
            RSAKey rsaKey = (RSAKey) jwkSet.getKeyByKeyId(signedJWT.getHeader().getKeyID());
            if (rsaKey == null) {
                log.error("RSA key not found in JWKS for kid: {}", signedJWT.getHeader().getKeyID());
                throw new RuntimeException("RSA key not found in JWKS");
            }

            // Проверить подпись
            RSASSAVerifier verifier = new RSASSAVerifier(rsaKey);
            if (!signedJWT.verify(verifier)) {
                log.error("Invalid JWT signature");
                throw new RuntimeException("Invalid JWT signature");
            }

            // Извлечь subject (user ID)
            String subject = signedJWT.getJWTClaimsSet().getSubject();
            if (subject == null || subject.isBlank()) {
                log.error("JWT token does not contain a valid subject");
                throw new RuntimeException("Invalid token: missing subject");
            }
            return subject;
        } catch (Exception e) {
            log.error("Failed to verify JWT: {}", e.getMessage(), e);
            throw new RuntimeException("Invalid token", e);
        }
    }
}
