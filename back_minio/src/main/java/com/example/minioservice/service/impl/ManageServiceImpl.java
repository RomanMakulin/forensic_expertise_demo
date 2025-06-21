package com.example.minioservice.service.impl;

import com.example.minioservice.service.ProfileManageService;
import com.example.minioservice.util.MinioHelper;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Реализация сервиса для управления файлами профиля.
 */
@Service
@Validated
public class ManageServiceImpl implements ProfileManageService {

    private static final Logger log = LoggerFactory.getLogger(ManageServiceImpl.class);
    private final MinioHelper minioHelper;

    public ManageServiceImpl(MinioHelper minioHelper) {
        this.minioHelper = minioHelper;
    }

    /**
     * Загружает абстрактный файл по указанным параметрам.
     *
     * @param fileName      имя файла (profileId_fileId)
     * @param fileExtension расширение файла (.docx, .pdf)
     * @param fileBucket    название бакета для загрузки
     * @param file          файл
     * @return ссылка на загруженный файл
     */
    @Override
    public String uploadFileByParams(@NotNull(message = "filename null") String fileName,
                                     @NotNull(message = "fileExtension null") String fileExtension,
                                     @NotNull(message = "fileBucket null") String fileBucket,
                                     @NotNull(message = "diploma null") MultipartFile file) {
        fileName = fileName + fileExtension; // например: profileId_fileId.docx / profileId.pdf

        minioHelper.upload(fileBucket, file, fileName);
        return minioHelper.getObjectUrl(fileBucket, fileName);
    }

    /**
     * Получение разных типов файла по заданным параметрам. Например, диплом, сертификат и т.д.
     *
     * @param fileName      имя файла
     * @param fileExtension расширение файла
     * @param fileBucket    название бакета (папки)
     * @return ссылка на файл
     */
    @Override
    public String getFileLinkByParams(@NotNull(message = "fileName null") String fileName,
                                      @NotNull(message = "fileExtension null") String fileExtension,
                                      @NotNull(message = "fileBucket null") String fileBucket) {
        return minioHelper.getObjectUrl(fileBucket, fileName + fileExtension);
    }

    /**
     * Получение разных типов файла по заданным параметрам. Например, диплом, сертификат и т.д.
     *
     * @param fileName      имя файла
     * @param fileExtension расширение файла
     * @param fileBucket    название бакета (папки)
     * @return массив байт с файлом
     */
    @Override
    public byte[] getFileByteByParams(@NotNull(message = "fileName null") String fileName,
                                      @NotNull(message = "fileExtension null") String fileExtension,
                                      @NotNull(message = "fileBucket null") String fileBucket) {
        try (InputStream inputStream = minioHelper.getObject(fileBucket, fileName + fileExtension)) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении файла из MinIO", e);
        }
    }

    /**
     * Удаляет файл по параметрам.
     *
     * @param fileName      название файла
     * @param fileExtension расширение файла
     * @param fileBucket    название бакета (папка)
     */
    @Override
    public void deleteFileByParam(@NotNull(message = "fileName null") String fileName,
                                  @NotNull(message = "fileExtension null") String fileExtension,
                                  @NotNull(message = "fileBucket null") String fileBucket) {
        minioHelper.delete(fileBucket, fileName + fileExtension);

    }
}
