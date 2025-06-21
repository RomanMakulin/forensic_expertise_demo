package com.example.auth.controller.exception;

import exceptions.PasswordResetTokenException;
import exceptions.ResetPasswordException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Обработчик ошибок с токенами восстановления пароля
 */
@ControllerAdvice
public class TokenExceptionHandler {

    /**
     * Обрабатывает ошибки работы с токеном восстановления пароля.
     */
    @ExceptionHandler({PasswordResetTokenException.class, ResetPasswordException.class})
    public ResponseEntity<String> handlePasswordResetTokenException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка работы с токеном: " + e.getMessage());
    }
}

