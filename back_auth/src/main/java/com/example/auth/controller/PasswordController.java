package com.example.auth.controller;

import com.example.auth.dto.auth.ResetPassword;
import com.example.auth.service.auth.PasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Восстановление пароля", description = "API для сброса и восстановления пароля пользователя")
@RestController
@RequestMapping("/api/auth")
public class PasswordController {

    private final PasswordService passwordService;

    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    /**
     * Запрос на восстановление пароля.
     *
     * @param email Адрес электронной почты пользователя
     * @return 200 OK, если запрос успешно обработан
     */
    @Operation(
            summary = "Запрос на восстановление пароля",
            description = "Отправляет ссылку для сброса пароля на указанный email.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Письмо с ссылкой для сброса отправлено"),
                    @ApiResponse(responseCode = "404", description = "Пользователь с таким email не найден")
            }
    )
    @PutMapping("/reset-password/{email}")
    public ResponseEntity<Void> resetPasswordRequest(
            @Parameter(description = "Email пользователя, для которого требуется сброс пароля")
            @PathVariable String email) {
        passwordService.resetPasswordRequest(email);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Сброс пароля пользователя.
     *
     * @param resetPassword Объект с новым паролем и токеном для восстановления
     * @return 200 OK, если пароль успешно сброшен
     */
    @Operation(
            summary = "Сброс пароля",
            description = "Позволяет пользователю установить новый пароль с помощью токена, полученного на email.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пароль успешно сброшен"),
                    @ApiResponse(responseCode = "400", description = "Некорректный токен или данные"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
            }
    )
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @Parameter(description = "Данные для сброса пароля (новый пароль и токен)")
            @RequestBody ResetPassword resetPassword) {
        passwordService.resetPassword(resetPassword);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
