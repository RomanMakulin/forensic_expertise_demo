package com.example.expertise.dto.checklist;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * DTO создания экземпляра чек-листа
 */
@Data
public class CreateChecklistInstanceDto {

    /**
     * Идентификатор вопроса, к которому относится экземпляр чек-листа
     */
    @JsonProperty("question_id")
    @NotNull(message = "Не указан идентификатор вопроса")
    private UUID questionId;

    /**
     * Идентификатор шаблона чек-листа
     */
    @JsonProperty("checklist_template_id")
    @NotNull(message = "Не указан идентификатор шаблона чек-листа")
    private UUID checklistTemplateId;

    /**
     * Данные экземпляра чек-листа в формате JSON
     */
    @JsonProperty("data")
    @NotEmpty(message = "Не указаны данные экземпляра чек-листа")
    private Map<String, Object> data;

}
