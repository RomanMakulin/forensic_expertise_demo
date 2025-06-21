package com.example.expertise.services.expertise;

import com.example.expertise.dto.expertise.CreateExpertiseDto;
import com.example.expertise.dto.expertise.ExpertiseResponseDto;
import com.example.expertise.model.expertise.Expertise;
import jakarta.validation.constraints.NotNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с сущностью Expertise.
 */
public interface ExpertiseService {

    /**
     * Получить все текущие экспертизы.
     *
     * @return список всех экспертиз
     */
    List<Expertise> getAllExpertise();

    /**
     * Получить все экспертизы, которые были созданы более чем 2 дня назад.
     *
     * @return список всех экспертиз, которые были созданы более чем 2 дня назад
     */
    List<Expertise> getAllExpertiseOlderThanTwoDays();

    /**
     * Создает новую экспертизу для заданного уникального идентификатора эксперта.
     *
     * @param createExpertiseDto данные для создания экспертизы
     */
    @Transactional
    ExpertiseResponseDto createNewExpertise(CreateExpertiseDto createExpertiseDto);

    /**
     * Находит экспертизу по уникальному идентификатору эксперта.
     *
     * @param expertiseId уникальный идентификатор экспертизы
     * @return найденная экспертиза или null, если экспертиза не найдена
     */
    Expertise getExpertiseById(@NotNull(message = "expertiseId cannot be null") UUID expertiseId);

    /**
     * Получает экспертизу по id и возвращает ее в виде ExpertiseResponseDto.
     * @param expertiseId ID экспертизы
     * @return экспертиза в виде ExpertiseResponseDto
     */
    ExpertiseResponseDto getExpertiseResponseDtoById(@NotNull(message = "expertiseId cannot be null") UUID expertiseId);

    /**
     * Удаляет экспертизу по уникальному идентификатору эксперта.
     *
     * @param expertiseId уникальный идентификатор
     */
    @Transactional
    void deleteExpertise(@NotNull(message = "expertiseId cannot be null") UUID expertiseId);

    /**
     * Генерирует файл экспертной экспертизы по заданному ID экспертизы
     *
     * @param expertiseId ID экспертизы
     * @return Файл экспертной экспертизы
     */
    byte[] generateExpertiseFile(UUID expertiseId, MultipartFile mapScreen);

    /**
     * Получить список всех экспертиз по заданному ID профиля
     *
     * @param profileId id профиля
     * @return список экспертиз по заданному ID профиля
     */
    List<Expertise> getExpertiseByProfileId(@NotNull(message = "profileId cannot be null") UUID profileId);

    /**
     * Получить список всех экспертиз по заданному ID профиля в формате ExpertiseResponseDto
     * @param profileId id профиля
     * @return список экспертиз по заданному ID профиля в формате ExpertiseResponseDto
     */
    List<ExpertiseResponseDto> getExpertiseResponseDtoByProfileId(@NotNull(message = "profileId cannot be null") UUID profileId);
}
