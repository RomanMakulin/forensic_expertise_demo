package com.example.minioservice.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Класс FileNameBuilder предоставляет методы для построения имен файлов.
 */
@Component
public class FileNameBuilder {

    /**
     * Построение имени файла аватара.
     *
     * @param profileId идентификатор профиля
     * @return имя файла аватара
     */
    public String buildAvatarObjectName(UUID profileId) {
        return profileId + ".jpg";
    }

    /**
     * Построение имени файла шаблона.
     *
     * @param fileName название файла
     * @return имя файла шаблона
     */
    public String buildTemplateObjectName(String fileName) {
        return fileName + ".docx";
    }

    /**
     * Построение имени файла.
     *
     * @param profileId идентификатор профиля
     * @param fileId    идентификатор файла
     * @return имя файла
     */
    public String buildFileObjectName(UUID profileId, UUID fileId) {
        return profileId + "_" + fileId + ".pdf";
    }

    /**
     * Построение имени файла
     *
     * @param fileName имя файла
     * @return имя файла
     */
    public String buildFileObjectName(String fileName) {
        return fileName + ".pdf";
    }

    /**
     * Построение имени файла
     *
     * @param profileId идентификатор профиля
     * @return имя файла
     */
    public String buildFileObjectName(UUID profileId) {
        return profileId + ".pdf";
    }

}
