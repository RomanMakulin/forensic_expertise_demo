package com.example.adminservice.integration.minio;

import jakarta.validation.constraints.NotNull;

/**
 * Интерфейс для работы с Minio
 */
public interface MinioIntegration {

    /**
     * Получить файл из микросервиса Minio.
     *
     * @param fileName      имя файла
     * @param fileExtension расширение файла
     * @param fileBucket    название бакета
     * @param link          флаг возвращаемого типа (true - ссылка, false - массив байтов)
     * @return ссылка на файл / массив байтов
     */
    <T> T getFileRequest(@NotNull(message = "fileName null") String fileName,
                         @NotNull(message = "fileExtension null") String fileExtension,
                         @NotNull(message = "fileBucket null") String fileBucket,
                         @NotNull(message = "link null") boolean link,
                         Class<T> responseType);

    /**
     * Удалить файл из микросервиса Minio.
     *
     * @param fileName      название файла
     * @param fileExtension расширение файла
     * @param fileBucket    название бакета
     */
    void deleteFileRequest(@NotNull(message = "fileName null") String fileName,
                           @NotNull(message = "fileExtension null") String fileExtension,
                           @NotNull(message = "fileBucket null") String fileBucket);

}
