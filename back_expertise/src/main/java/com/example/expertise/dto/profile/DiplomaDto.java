package com.example.expertise.dto.profile;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class DiplomaDto {

    /**
     * Уникальный идентификатор
     */
    private UUID id;

    /**
     * Серия диплома
     */
    private String serial;

    /**
     * Номер диплома
     */
    private String number;

    /**
     * Дата выдачи диплома
     */
    private LocalDate date;

    /**
     * Специализация
     */
    private String specialization;

    /**
     * Кем выдан
     */
    private String organization;

}
