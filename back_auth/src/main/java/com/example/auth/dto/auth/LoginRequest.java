package com.example.auth.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO для авторизации пользователя
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Почта пользователя не может быть пустой")
    private String email;

    @NotBlank(message = "Пароль не может быть пустым")
    private String password;

}
