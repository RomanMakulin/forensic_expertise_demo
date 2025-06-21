package com.example.adminservice.dto.profile.original;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO: информация о пользователе (регистрационные данные) - эксперта
 * Принимаемый объект с мордуля "profile"
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OriginalUserDto {

    /**
     * ФИО пользователя
     */
    private String fullName;

    /**
     * Email пользователя
     */
    private String email;

    /**
     * Дата регистрации пользователя
     */
    private LocalDateTime registrationDate;

    /**
     * Роль пользователя
     */
    private OriginalRoleDto role;

}