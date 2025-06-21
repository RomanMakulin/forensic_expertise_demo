package com.example.adminservice.dto.profile;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO: информация о направлении работы эксперта
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DirectionDto {

    /**
     * id направления работы
     */
    @NotNull(message = "id направления работы не может быть null")
    private String id;

    /**
     * Название направления работы
     */
    @NotNull(message = "название направления работы не может быть null")
    private String name;

}
