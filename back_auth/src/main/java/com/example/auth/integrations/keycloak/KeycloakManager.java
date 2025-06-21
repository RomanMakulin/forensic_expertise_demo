package com.example.auth.integrations.keycloak;

import com.example.auth.dto.auth.RegistrationRequest;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

/**
 * Интерфейс для работы с админкой Keycloak.
 */
public interface KeycloakManager {

    /**
     * Создает пользователя Keycloak.
     *
     * @param request данные пользователя для регистрации (DTO)
     * @return представление пользователя
     */
    void createKeycloakUser(RegistrationRequest request);

    /**
     * Удаляет пользователя по его идентификатору.
     *
     * @param keycloakUserId идентификатор пользователя
     */
    void deleteUserById(String keycloakUserId);

    /**
     * Удаляет пользователя по его email.
     *
     * @param email email пользователя
     */
    void deleteUserByEmail(String email);

    /**
     * Возвращает пользователя по его идентификатору.
     *
     * @param keycloakUserId идентификатор пользователя
     * @return представление пользователя
     */
    UserRepresentation getUserById(String keycloakUserId);

    /**
     * Изменить пароль пользователя
     *
     * @param keycloakId  идентификатор пользователя
     * @param newPassword новый пароль
     */
    void resetPassword(String keycloakId, String newPassword);

    /**
     * Возвращает список пользователей, соответствующих поисковому запросу.
     *
     * @param searchString поисковый запрос
     * @return список представлений пользователей
     */
    List<UserRepresentation> findUsers(String searchString);

    /**
     * Возвращает пользователя keycloak по его email.
     *
     * @param email email пользователя
     * @return представление пользователя
     */
    UserRepresentation getUserByEmail(String email);

    /**
     * Обновляет информацию о пользователе.
     *
     * @param user представление пользователя
     */
    void updateUser(UserRepresentation user);

    /**
     * Назначает роль пользователя в рамках реалма.
     *
     * @param keycloakUserId идентификатор пользователя
     * @param roleName       имя роли
     */
    void assignRealmRole(String keycloakUserId, String roleName);

    /**
     * Удаляет роль пользователя в рамках реалма.
     *
     * @param keycloakUserId идентификатор пользователя
     * @param roleName       имя роли
     */
    void removeRealmRole(String keycloakUserId, String roleName);

    /**
     * Возвращает список ролей пользователя в рамках реалма.
     *
     * @param keycloakUserId идентификатор пользователя
     * @return список представлений ролей
     */
    List<RoleRepresentation> getUserRealmRoles(String keycloakUserId);

    /**
     * Выход пользователя из системы.
     *
     * @param keycloakUserId идентификатор пользователя
     */
    void logoutUserById(String keycloakUserId);

    /**
     * Обновить access токен на основании refresh
     *
     * @param refreshToken токен для обновления
     * @return обновленные данные авторизации
     */
    AccessTokenResponse refreshToken(String refreshToken);

    /**
     * Обменять код авторизации на токены
     *
     * @param code код
     * @return токены
     */
    AccessTokenResponse exchangeCodeForToken(String code);

    /**
     * Получает ID пользователя из JWT-токена.
     *
     * @param token Токен.
     * @return ID пользователя.
     */
    String getKeycloakUserIdFromToken(String token);
}
