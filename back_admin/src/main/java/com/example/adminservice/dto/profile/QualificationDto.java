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
public class QualificationDto {

    /**
     * Уникальный идентификатор
     */
    private UUID id;

    /**
     * Наименование курса
     */
    @JsonProperty("course_name")
    private String courseName;

    /**
     * Дата получения
     */
    @JsonProperty("issue_date")
    private LocalDate issueDate;

    /**
     * Организация, выдавшая документ
     */
    private String institution;

    /**
     * Номер удостоверения
     */
    private String number;

    /**
     * Ссылка на скачивание
     */
    private String link;

}
