package com.example.expertise.dto.profile;

import lombok.Data;

import java.util.List;

/**
 * DTO для получения данных профиля пользователя.
 * В основном для получения информации о файлах
 */
@Data
public class ProfileResponseDto {

    private DiplomaDto diploma;

    private List<AdditionalDiplomaDto> additionalDiplomas;

    private List<CertificateDto> certificates;

    private List<QualificationDto> qualifications;


}
