package com.example.adminservice.dto.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
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
    @JsonProperty("issue_date")
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

    /**
     * Ссылка на скачивание
     */
    private String link;

}
