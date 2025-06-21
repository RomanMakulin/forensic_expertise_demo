package com.example.auth.service.user;


import com.example.auth.dto.auth.RegistrationRequest;
import com.example.auth.integrations.keycloak.KeycloakManager;
import com.example.auth.model.Role;
import com.example.auth.model.User;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import exceptions.UserCreateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Реализация сервиса для работы с пользователем.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    /**
     * Репозиторий для работы с пользователями.
     */
    private final UserRepository userRepository;

    private final KeycloakManager keycloakManager;

    private final RoleRepository roleRepository;

    /**
     * Конструктор класса UserServiceImpl.
     *
     * @param userRepository репозиторий для работы с пользователями
     */
    public UserServiceImpl(UserRepository userRepository,
                           KeycloakManager keycloakManager,
                           RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.keycloakManager = keycloakManager;
        this.roleRepository = roleRepository;
    }

    /**
     * Возвращает всех пользователей.
     *
     * @return список пользователей
     */
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Регистрация пользователя.
     *
     * @param request запрос регистрации
     * @return пользователь
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createUser(RegistrationRequest request) {
        keycloakManager.createKeycloakUser(request);
        String keycloakUserId = keycloakManager.getUserByEmail(request.getEmail()).getId();
        return createUserMainTable(request, keycloakUserId);
    }

    /**
     * Создает пользователя в базе данных.
     *
     * @param request        запрос регистрации
     * @param keycloakUserId идентификатор пользователя в Keycloak
     * @return пользователь
     */
    @Transactional
    @Override
    public User createUserMainTable(RegistrationRequest request, String keycloakUserId) {
        try {
            User localUser = new User();
            Role role = roleRepository.findByName("Expert")
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            String fullName = Stream.of(request.getLastName(), request.getFirstName(), request.getPatronymicName())
                    .filter(Objects::nonNull).filter(s -> !s.isBlank()).collect(Collectors.joining(" "));

            if (fullName.isBlank()) fullName = "Unknown";

            localUser.setFullName(fullName);
            localUser.setEmail(request.getEmail());
            localUser.setRegistrationDate(LocalDateTime.now());
            localUser.setRole(role);
            localUser.setKeycloakId(keycloakUserId);
            localUser.setVerificationEmail(false);
            userRepository.save(localUser);
            log.info("User successfully created: {}", localUser);
            return localUser;
        } catch (Exception e) {
            keycloakManager.deleteUserByEmail(request.getEmail());
            log.error("User not created in local table", e);
            throw new UserCreateException();
        }
    }

    /**
     * Возвращает пользователя по его идентификатору.
     *
     * @param id идентификатор пользователя
     * @return пользователь
     */
    @Override
    public User getUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Возвращает пользователя по его идентификатору в Keycloak.
     *
     * @param keycloakId идентификатор пользователя в Keycloak
     * @return пользователь
     */
    @Override
    public User getUserByKeycloakId(String keycloakId) {
        return userRepository.findUserByKeycloakId(keycloakId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Получает список не прошедших проверку пользователей.
     */
    @Override
    public List<User> getNotVerifiedUsers() {
        return userRepository.findUnverifiedUsers();
    }

    /**
     * Удаляет пользователя по его идентификатору.
     *
     * @param id идентификатор пользователя
     */
    @Override
    public void deleteUserById(UUID id) {
        userRepository.deleteById(id);
    }

    /**
     * Удаляет пользователя по его email.
     *
     * @param email email пользователя
     */
    @Override
    public void deleteUserByEmail(String email) {
        userRepository.deleteByEmail(email);
    }

    /**
     * Удаляет пользователя.
     *
     * @param user пользователь
     */
    @Override
    public void deleteUser(User user) {
        userRepository.delete(user);
    }
}

