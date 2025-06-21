package com.example.adminservice.dto.profile.original;

import lombok.*;

import java.util.UUID;

/**
 * Роль пользователя
 * Принимаемый объект с мордуля "profile"
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OriginalRoleDto {

    /**
     * Идентификатор роли
     */
    private UUID id;

    /**
     * Название роли
     */
    private String name;

}
