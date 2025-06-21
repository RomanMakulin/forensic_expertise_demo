package com.example.adminservice.dto.profile.original;

import lombok.*;

/**
 * DTO: информация статуса профиля эксперта
 * Принимаемый объект с мордуля "profile"
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OriginalStatusDto {

    /**
     * Результат проверки профиля администратором
     */
    private String verificationResult;

    /**
     * Статус активности эксперта
     */
    private String activityStatus;
}
