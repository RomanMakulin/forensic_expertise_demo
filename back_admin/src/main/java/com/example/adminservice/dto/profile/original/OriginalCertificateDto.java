package com.example.adminservice.dto.profile.original;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OriginalCertificateDto {

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
    @JsonProperty("issue_date")
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
