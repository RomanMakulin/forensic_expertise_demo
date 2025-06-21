package com.example.adminservice.dto.profile.original;

import com.example.adminservice.dto.profile.DirectionDto;
import com.example.adminservice.dto.profile.InstrumentDto;
import com.example.adminservice.dto.profile.LocationDto;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * DTO модели Profile
 * Принимаемый объект с мордуля "profile"
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OriginalProfileDto {

    /**
     * Идентификатор
     */
    private UUID id;

    /**
     * Пользователь
     */
    private OriginalUserDto appUser;

    /**
     * Телефон
     */
    private String phone;

    /**
     * Дата рождения
     */
    private LocalDate birthday;

    /**
     * Местоположение
     */
    private LocationDto location;

    /**
     * Статус
     */
    private OriginalStatusDto status;

    /**
     * Список направлений
     */
    private Set<DirectionDto> directions;

    /**
     * Список инструментов
     */
    private List<InstrumentDto> instruments;

    /**
     * Список дополнительных дипломов
     */
    private List<OriginalAdditionalDiplomaDto> additionalDiplomas;

    /**
     * Список сертификатов
     */
    private List<OriginalCertificateDto> certificates;

    /**
     * Список документов о переквалификации
     */
    private List<OriginalQualificationDto> qualifications;

}
