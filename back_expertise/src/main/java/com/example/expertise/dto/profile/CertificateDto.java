package com.example.expertise.dto.profile;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO для представления сертификата
 */
@Data
public class CertificateDto {

    /**
     * Уникальный идентификатор
     */
    private UUID id;

    /**
     * Наименование
     */
    private String name;

    /**
     * Дата выдачи
     */
    private LocalDate issueDate;

    /**
     * Организация выдавшая сертификат
     */
    private String organization;

    /**
     * Номер сертификата
     */
    private String number;

}
