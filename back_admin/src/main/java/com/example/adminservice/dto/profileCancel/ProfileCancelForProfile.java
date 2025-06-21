package com.example.adminservice.dto.profileCancel;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * DTO для запроса в модуль профиля для дальнейшего удаления содержимого
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ProfileCancelForProfile {

    /**
     * id профиля
     */
    @JsonProperty("profile_id")
    @NotNull(message = "profileId is required")
    private String profileId;

    /**
     * Направления работы. Приходит с фронта (если хотя бы один элемент есть - его нужно удалить из БД)
     * Содержит ID
     */
    @NotNull(message = "directions is required")
    private List<String> directions;

    /**
     * Инструменты. Приходит с фронта (если хотя бы один элемент есть - его нужно удалить из БД)
     * Содержит ID
     */
    private List<String> instruments;

    /**
     * Дополнительные дипломы. Приходит с фронта (если хотя бы один элемент есть - его нужно удалить из БД)
     */
    @JsonProperty("additional_diploms")
    private List<String> additionalDiplomas;

    /**
     * Сертификаты. Приходит с фронта (если хотя бы один элемент есть - его нужно удалить из БД)
     */
    private List<String> certificates;

    /**
     * Документы переквалификации. Приходит с фронта (если хотя бы один элемент есть - его нужно удалить из БД)
     */
    private List<String> qualifications;

    /**
     * Флаг, указывающий на то, что нужно удалить фото профиля
     */
    private Boolean needDiplomaDelete;

}
