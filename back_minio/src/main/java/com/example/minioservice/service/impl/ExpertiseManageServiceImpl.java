package com.example.minioservice.service.impl;

import com.example.minioservice.service.ExpertiseManageService;
import com.example.minioservice.util.FileNameBuilder;
import com.example.minioservice.util.MinioHelper;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Сервис управления файлами экспертизы.
 */
@Service
@Validated
public class ExpertiseManageServiceImpl implements ExpertiseManageService {

    private final MinioHelper minioHelper;

    private final FileNameBuilder fileNameBuilder;

    public ExpertiseManageServiceImpl(MinioHelper minioHelper,
                                      FileNameBuilder fileNameBuilder) {
        this.minioHelper = minioHelper;
        this.fileNameBuilder = fileNameBuilder;
    }

    /**
     * Получить фото по его названию
     *
     * @param pathFile название файла
     * @return ссылка на фото
     */
    @Override
    public String getAnswerPhotoByName(@NotNull(message = "Не указано название файла") String pathFile) {
        return minioHelper.getObjectUrl(minioHelper.bucketExpertiseAnswers, pathFile);
    }

    /**
     * Получить фото по его названию
     *
     * @param pathFile название файла
     * @return файл фото
     */
    @Override
    public byte[] getAnswerPhotoFileAsBytes(@NotNull(message = "Не указано название файла") String pathFile) {
        try (InputStream inputStream = minioHelper.getObject(minioHelper.bucketExpertiseAnswers, pathFile)) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении файла из MinIO", e);
        }
    }

    /**
     * Получить экспертизу по идентификатору профиля
     *
     * @param templateName имя файла
     * @return файл экспертизы
     */
    @Override
    public InputStreamResource getExpertise(@NotNull(message = "templateName null") String templateName) {
        return new InputStreamResource(minioHelper.getObject(minioHelper.bucketTemplates, fileNameBuilder.buildTemplateObjectName(templateName)));
    }

    /**
     * Загружает фото для ответа при создании экспертизы.
     *
     * @param photoName название файла
     * @param photo     файл
     */
    @Override
    public void uploadAnswerPhoto(@NotNull(message = "Не указано название файла") String photoName,
                                  @NotNull(message = "Не указан файл") MultipartFile photo) {
        minioHelper.upload(minioHelper.bucketExpertiseAnswers, photo, photoName);
    }

    /**
     * Удаляет все фотографии ответов экспертизы для указанной экспертизы
     *
     * @param expertiseId идентификатор экспертизы
     */
    @Override
    public void deleteAnswerPhotos(@NotNull(message = "Не указан идентификатор экспертизы") UUID expertiseId) {
        minioHelper.deleteByPrefix(minioHelper.bucketExpertiseAnswers, expertiseId.toString());
    }

    /**
     * Удаляет фотографию ответа в экспертизе по названию файла.
     *
     * @param pathFile название файла
     */
    @Override
    public void deleteAnswerPhoto(@NotNull(message = "Не указано название файла") String pathFile) {
        minioHelper.deleteByPrefix(minioHelper.bucketExpertiseAnswers, pathFile);
    }

}
