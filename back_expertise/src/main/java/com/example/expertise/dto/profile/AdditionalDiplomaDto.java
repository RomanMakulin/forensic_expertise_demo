package com.example.expertise.dto.profile;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO для представления дополнительного диплома
 */
@Data
public class AdditionalDiplomaDto {

    /**
     * Уникальный идентификатор
     */
    private UUID id;

    /**
     * Номер диплома
     */
    private String number;

    /**
     * Дата выдачи диплома
     */
    private LocalDate issueDate;

    /**
     * Наименование учебного заведения
     */
    private String institution;

    /**
     * Специальность
     */
    private String specialty;

    /**
     * Степень
     */
    private String degree;

}
