package com.example.auth.controller.exception;

import exceptions.VkAuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.naming.AuthenticationException;

/**
 * Обрабатывает ошибки аутентификации.
 */
@ControllerAdvice
public class AuthExceptionHandler {

    /**
     * Обрабатывает ошибки аутентификации.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationException(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: " + e.getMessage());
    }

    /**
     * Обрабатывает ошибки аутентификации с помощью ВКонтакте.
     *
     * @param e ошибка
     * @return ответ
     */
    @ExceptionHandler(VkAuthException.class)
    public ResponseEntity<String> handleVkAuthException(VkAuthException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
