package com.example.auth.controller.exception;

import exceptions.KeycloakUserCreatException;
import exceptions.UserAlreadyExistsException;
import exceptions.UserCreateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Обрабатывает ошибки с пользователем
 */
@ControllerAdvice
public class UserExceptionHandler {

    /**
     * Обрабатывает ошибку "Пользователь уже существует".
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExists(UserAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    /**
     * Обрабатывает ошибки создания пользователя в БД и Keycloak.
     */
    @ExceptionHandler({UserCreateException.class, KeycloakUserCreatException.class})
    public ResponseEntity<String> handleUserCreateException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка создания пользователя: " + e.getMessage());
    }

}
