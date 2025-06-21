package com.example.expertise.dto.profile;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO для представления документа о квалификации
 */
@Data
public class QualificationDto {

    /**
     * Уникальный идентификатор
     */
    private UUID id;

    /**
     * Наименование курса
     */
    private String courseName;

    /**
     * Дата получения
     */
    private LocalDate issueDate;

    /**
     * Организация, выдавшая документ
     */
    private String institution;

    /**
     * Номер удостоверения
     */
    private String number;
}
