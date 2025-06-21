package com.example.auth.service.user;

import com.example.auth.dto.auth.RegistrationRequest;
import com.example.auth.model.User;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс для работы с пользователем.
 */
public interface UserService {

    /**
     * Возвращает всех пользователей.
     *
     * @return список пользователей
     */
    List<User> getAllUsers();

    /**
     * Создание - регистрацию пользователя
     *
     * @param request запрос регистрации
     * @return пользователь
     */
    User createUser(RegistrationRequest request);

    /**
     * Создает пользователя в базе данных.
     *
     * @param request        запрос регистрации
     * @param keycloakUserId идентификатор пользователя в Keycloak
     * @return пользователь
     */
    User createUserMainTable(RegistrationRequest request, String keycloakUserId);

    /**
     * Возвращает пользователя по его идентификатору.
     *
     * @param id идентификатор пользователя
     * @return пользователь
     */
    User getUserById(UUID id);

    /**
     * Возвращает пользователя по его email.
     *
     * @param email email пользователя
     * @return пользователь
     */
    User getUserByEmail(String email);

    /**
     * Возвращает пользователя по его идентификатору в Keycloak.
     *
     * @param keycloakId идентификатор пользователя в Keycloak
     * @return пользователь
     */
    User getUserByKeycloakId(String keycloakId);

    /**
     * Получает список не подтвержденных пользователей.
     */
    List<User> getNotVerifiedUsers();

    /**
     * Удаляет пользователя по его идентификатору.
     *
     * @param id идентификатор пользователя
     */
    void deleteUserById(UUID id);

    /**
     * Удаляет пользователя по его email.
     *
     * @param email email пользователя
     */
    void deleteUserByEmail(String email);

    /**
     * Удаляет пользователя.
     *
     * @param user пользователь
     */
    void deleteUser(User user);

}

