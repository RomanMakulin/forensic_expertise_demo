package com.example.auth.service.sheduler;

import com.example.auth.model.User;
import com.example.auth.integrations.keycloak.KeycloakManager;
import com.example.auth.service.auth.PasswordTokenService;
import com.example.auth.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Служба для выполнения задач по расписанию.
 */
@Service
public class DailyTaskScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyTaskScheduler.class);

    /**
     * Сервис для работы с пользователями.
     */
    private final UserService userService;

    /**
     * Сервис для работы с админкой Keycloak.
     */
    private final KeycloakManager keycloakManager;

    private final PasswordTokenService passwordTokenService;

    public DailyTaskScheduler(UserService userService,
                              KeycloakManager keycloakManager,
                              PasswordTokenService passwordTokenService) {
        this.userService = userService;
        this.keycloakManager = keycloakManager;
        this.passwordTokenService = passwordTokenService;
    }

    /**
     * Каждые 4 часа выполняются запланированные задачи.
     */
    @Scheduled(cron = "0 0 0/4 * * ?") // Каждые 4 часа
    public void executeDailyTask() {
        deleteUnverifiedUsers(); // Удаление неподтвержденных пользователей
        deleteExpectedTokens(); // Удаление просроченных токенов
    }

    /**
     * Удаление всех просроченных токенов.
     */
    private void deleteExpectedTokens() {
        passwordTokenService.deleteAllExpiredTokens();
    }

    /**
     * Удаление неподтвержденных пользователей.
     */
    private void deleteUnverifiedUsers() {
        // Получение всех неподтвержденных пользователей
        List<User> users = userService.getNotVerifiedUsers();

        if (users.isEmpty()) {
            log.debug("Нет неподтвержденных пользователей");
            return;
        }

        // Удаление пользователей из Keycloak и локальной базы данных
        users.forEach(user -> {
            try {
                keycloakManager.deleteUserByEmail(user.getEmail()); // Удаление из Keycloak
                userService.deleteUser(user); // Удаление из локальной базы
                log.debug("Пользователь удален: {}", user);
            } catch (Exception e) {
                log.error("Ошибка при удалении пользователя: {}", user, e);
            }
        });

        log.info("Удалено неподтвержденных пользователей: {}", users.size());
    }

}
