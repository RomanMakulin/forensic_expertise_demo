package com.example.adminservice.dto.profile;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * DTO: информация о локации работы эксперта
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class LocationDto {

    /**
     * Страна
     */
    @NotNull(message = "Страна не может быть null")
    private String country;

    /**
     * Регион
     */
    @NotNull(message = "Регион не может быть null")
    private String region;

    /**
     * Город
     */
    @NotNull(message = "Город не может быть null")
    private String city;

    /**
     * Адрес
     */
    @NotNull(message = "Адрес не может быть null")
    private String address;

}
