package com.example.adminservice.dto.profileCancel;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * DTO с фронта для отклонения верификации профиля с неподходящими данными
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ProfileCancelFromFront {

    /**
     * id профиля
     */
    @JsonProperty("profile_id")
    @NotNull(message = "profileId is required")
    private String profileId;

    /**
     * Если приходит с фронта true - удаляем из профиля паспорт
     */
    @JsonProperty("need_passport_delete")
    private Boolean needPassportDelete;

    /**
     * Если приходит с фронта true - удаляем из профиля диплом
     */
    @JsonProperty("need_diplom_delete")
    private Boolean needDiplomaDelete;

    /**
     * Направления работы. Приходит с фронта (если хотя бы один элемент есть - его нужно удалить из БД)
     * Содержит ID
     */
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
     * Почта пользователя (получается с фронта)
     */
    @JsonProperty("user_mail")
    private String userMail;

}
