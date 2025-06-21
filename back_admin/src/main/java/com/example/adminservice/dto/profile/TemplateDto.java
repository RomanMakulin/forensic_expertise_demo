package com.example.adminservice.dto.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO: информация о шаблоне, который выбрал эксперт
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TemplateDto {

    /**
     * Идентификатор шаблона
     */
    @NotNull(message = "ID шаблона не может быть пустым")
    private String id;

    /**
     * Путь к шаблону
     */
    @NotNull(message = "Путь к шаблону не может быть пустым")
    private String path;

    /**
     * Дата создания шаблона
     */
    @JsonProperty("created_at")
    @NotNull(message = "Дата создания шаблона не может быть пустой")
    private String createdAt;

}
