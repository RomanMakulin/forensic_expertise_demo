package com.example.minioservice.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * Интерфейс для работы с файлами профиля.
 */
public interface ProfileManageService {

    /**
     * Загружает абстрактный файл по указанным параметрам.
     *
     * @param fileName      имя файла (profileId_fileId)
     * @param fileExtension расширение файла (.docx, .pdf)
     * @param fileBucket    название бакета - папки, в который сохраняется файл
     * @param file          файл
     * @return ссылка на загруженный файл
     */
    String uploadFileByParams(@NotNull(message = "filename null") String fileName,
                              @NotNull(message = "fileExtension null") String fileExtension,
                              @NotNull(message = "fileBucket null") String fileBucket,
                              @NotNull(message = "diploma null") MultipartFile file);

    /**
     * Получение разных типов файлов по разным параметрам (дипломы, шаблоны, сертификаты, переподготовка и т.д.)
     *
     * @param fileName      имя файла
     * @param fileExtension расширение файла
     * @param fileBucket    название бакета (папки)
     * @return ссылка на файл
     */
    String getFileLinkByParams(@NotNull(message = "fileName null") String fileName,
                               @NotNull(message = "fileExtension null") String fileExtension,
                               @NotNull(message = "fileBucket null") String fileBucket);

    /**
     * Получение разных типов файлов по разным параметрам (дипломы, шаблоны, сертификаты, переподготовка и т.д.).
     *
     * @param fileName      имя файла
     * @param fileExtension расширение файла
     * @param fileBucket    название бакета (папки)
     * @return файл в виде массива байт
     */
    byte[] getFileByteByParams(@NotNull(message = "fileName null") String fileName,
                               @NotNull(message = "fileExtension null") String fileExtension,
                               @NotNull(message = "fileBucket null") String fileBucket);

    /**
     * Удаление файла по параметрам.
     *
     * @param fileName      название файла
     * @param fileExtension расширение файла
     * @param fileBucket    название бакета (папка)
     */
    void deleteFileByParam(@NotNull(message = "fileName null") String fileName,
                           @NotNull(message = "fileExtension null") String fileExtension,
                           @NotNull(message = "fileBucket null") String fileBucket);

}
