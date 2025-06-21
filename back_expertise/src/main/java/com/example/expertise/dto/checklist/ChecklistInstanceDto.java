package com.example.expertise.dto.checklist;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO представления информации об экземпляре чек-листа
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChecklistInstanceDto {

    /**
     * Идентификатор экземпляра чек-листа
     */
    private UUID id;

    /**
     * Идентификатор вопроса, к которому относится экземпляр чек-листа
     */
    @JsonProperty("question_id")
    private UUID questionId;

    /**
     * Идентификатор шаблона чек-листа, к которому относится экземпляр чек-листа
     */
    @JsonProperty("template_id")
    private UUID templateId;

    /**
     * Данные экземпляра чек-листа в формате JSON
     */
    private Map<String, Object> data;

    /**
     * Кэш имен полей экземпляра чек-листа для отображения в пользовательском интерфейсе
     */
    @JsonProperty("field_name_cache")
    private Map<String, String> fieldNameCache;

    /**
     * Дата и время создания экземпляра чек-листа
     */
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления экземпляра чек-листа
     */
    private LocalDateTime updatedAt;
}
