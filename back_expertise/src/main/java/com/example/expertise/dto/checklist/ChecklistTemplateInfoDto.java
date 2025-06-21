package com.example.expertise.dto.checklist;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO представления информации о шаблоне экспертизы
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChecklistTemplateInfoDto {

    /**
     * Идентификатор шаблона экспертизы
     */
    @JsonProperty("checklist_template_id")
    private UUID checklistTemplateId;

    /**
     * Название шаблона чек-листа экспертизы
     */
    private String name;

}
