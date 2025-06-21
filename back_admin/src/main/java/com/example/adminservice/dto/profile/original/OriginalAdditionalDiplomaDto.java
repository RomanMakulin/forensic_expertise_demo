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
public class OriginalAdditionalDiplomaDto {

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
