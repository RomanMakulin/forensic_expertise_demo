package com.example.adminservice.dto.profile;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO: информация о пользователе (регистрационные данные) - эксперта
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    /**
     * Имя пользователя
     */
    @JsonProperty("full_name")
    @NotNull(message = "ФИО не может быть пустым")
    private String fullName;

    /**
     * Email пользователя
     */
    @NotNull(message = "email не может быть пустым")
    private String email;

    /**
     * Дата регистрации пользователя
     */
    @NotNull(message = "дата регистрации не может быть пустой")
    @JsonProperty("registration_date")
    private LocalDateTime registrationDate;

    /**
     * Роль пользователя
     */
    @NotNull(message = "роль не может быть пустой")
    private String role;

}

