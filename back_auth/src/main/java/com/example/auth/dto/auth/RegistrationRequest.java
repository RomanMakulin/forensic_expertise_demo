package com.example.auth.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO для регистрации пользователя
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    private String password;

    @NotBlank(message = "Имя не может быть пустым")
    @JsonProperty("first_name")
    private String firstName;

    @NotBlank(message = "Фамилия не может быть пустой")
    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("patronymic_name")
    private String patronymicName;
}
