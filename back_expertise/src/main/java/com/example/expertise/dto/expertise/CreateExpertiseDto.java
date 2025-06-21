package com.example.expertise.dto.expertise;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO с вводными данными для создания экспертизы.
 * Приходит с клиента на создание экспертизы.
 */
@Data
public class CreateExpertiseDto {

    /**
     * ID Профиля пользователя, которому принадлежит экспертиза.
     */
    @NotNull
    @JsonProperty("profile_id")
    private UUID profileId;

    /**
     * Имя шаблона экспертизы, без расширения (без .docx)
     */
    @JsonProperty("template_name")
    @NotNull(message = "ID шаблона не может быть пустым")
    private String templateName;

    /**
     * Специализация экспертизы
     */
    @NotNull
    private String speciality;

    /**
     * Дата окончания экспертизы
     */
    @JsonProperty("end_date")
    @NotNull
    private LocalDate endDate;

    /**
     * Название экспертизы
     */
    @NotNull
    private String name;

    /**
     * Дата судебного определения о назначении экспертизы (ОТ)
     */
    @NotNull
    @JsonProperty("ruling_date")
    private LocalDate rulingDate;

    /**
     * Название суда, которое назначило экспертизу
     */
    @NotNull
    @JsonProperty("court_name")
    private String courtName;

    /**
     * Номер дела
     */
    @NotNull
    @JsonProperty("case_number")
    private String caseNumber;

    /**
     * ФИО председателя суда
     */
    @NotNull
    @JsonProperty("presiding_judge")
    private String presidingJudge;

    /**
     * Список ФИО судей
     */
    @NotNull
    @JsonProperty("expertise_judges")
    private List<String> expertiseJudges;

    /**
     * Иск
     */
    @NotNull
    private String plaintiff;

    /**
     * Дата начала экспертизы
     */
    @NotNull
    @JsonProperty("start_date")
    private LocalDate startDate;

    /**
     * Месторасположение объекта экспертизы
     */
    @NotNull
    private String location;

    /**
     * Список вопросов экспертизы
     */
    @NotNull
    private List<String> questions;

    /**
     * Сколько томов
     */
    @NotNull
    @JsonProperty("volume_count")
    private String volumeCount;

    /**
     * Список присутствующих при осмотре в одну строку через запятую
     */
    @NotNull
    private String participants;

    /**
     * Дата и время осмотра
     */
    @NotNull
    @JsonProperty("inspection_date_time")
    private LocalDateTime inspectionDateTime;

}
