package com.example.minioservice.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Сервис управления файлами экспертизы.
 */
public interface ExpertiseManageService {

    /**
     * Получить фото по его названию
     *
     * @param pathFile название файла
     * @return ссылка на фото
     */
    String getAnswerPhotoByName(@NotNull(message = "Не указано название файла") String pathFile);

    /**
     * Получить фото по его названию
     * @param pathFile название файла
     * @return ссылка на фото
     */
    byte[] getAnswerPhotoFileAsBytes(@NotNull(message = "Не указано название файла") String pathFile);

    /**
     * Получить экспертизу по идентификатору профиля
     *
     * @param templateName название файла
     * @return файл экспертизы
     */
    InputStreamResource getExpertise(@NotNull(message = "templateName null") String templateName);

    /**
     * Загружает фото для ответа при создании экспертизы.
     *
     * @param photoName название файла
     * @param photo     файл
     */
    void uploadAnswerPhoto(@NotNull(message = "Не указано название файла") String photoName,
                           @NotNull(message = "Не указан файл") MultipartFile photo);

    /**
     * Удаляет фотографии ответов в экспертизе для указанной экспертизы
     *
     * @param expertiseId идентификатор экспертизы
     */
    void deleteAnswerPhotos(@NotNull(message = "Не указан идентификатор экспертизы") UUID expertiseId);

    /**
     * Удаляет фотографию ответа в экспертизе по названию файла.
     *
     * @param pathFile название файла
     */
    void deleteAnswerPhoto(@NotNull(message = "Не указано название файла") String pathFile);

}
