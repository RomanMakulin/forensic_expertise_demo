package com.example.adminservice.dto.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * DTO: Все данные профиля для фронта
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDto {

    /**
     * id профиля
     */
    @NotNull(message = "идентификатор не может быть пустым")
    private UUID id;

    /**
     * Данные пользователя (регистрационные)
     */
    @NotNull(message = "пользователь не может быть пустым")
    private UserDto user;

    /**
     * Дата рождения профиля
     */
    @NotNull(message = "дата рождения не может быть пустой")
    private LocalDate birthday;

    /**
     * Фото профиля (ссылка на файл)
     */
    @NotNull(message = "фото не может быть пустым")
    private String photo;

    /**
     * Паспорт профиля (ссылка на файл)
     */
    @NotNull(message = "паспорт не может быть пустым")
    private String passport;

    /**
     * Диплом профиля (ссылка на файл)
     */
    @NotNull(message = "диплом не может быть пустым")
    private String diplom;

    /**
     * Телефон профиля
     */
    @NotNull(message = "номер телефона не может быть пустым")
    private String phone;

    /**
     * Локация профиля
     */
    @NotNull(message = "локация не может быть пустой")
    private LocationDto location;

    /**
     * Статус профиля
     */
    @JsonProperty("profile_status")
    @NotNull(message = "статус пользователя не может быть пустым")
    private StatusDto profileStatus;

    /**
     * Направления работы профиля
     */
    @NotEmpty(message = "направления работы не может быть пустым")
    private Set<DirectionDto> directions;

    /**
     * Инструменты профиля
     */
    private List<InstrumentDto> instruments;

    /**
     * Список дополнительных дипломов
     */
    @JsonProperty("additional_diplomas")
    private List<AdditionalDiplomaDto> additionalDiplomas;

    /**
     * Список сертификатов
     */
    private List<CertificateDto> certificates;

    /**
     * Список документов о переквалификации
     */
    private List<QualificationDto> qualifications;

}
