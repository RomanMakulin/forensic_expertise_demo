package com.example.adminservice.service.impl;

import com.example.adminservice.integration.minio.MinioIntegration;
import com.example.adminservice.service.ProfileFileService;
import org.springframework.stereotype.Service;

/**
 * Сервис для работы с файлами профиля.
 */
@Service
public class ProfileFileServiceImpl implements ProfileFileService {

    private final MinioIntegration minioIntegration;

    public ProfileFileServiceImpl(MinioIntegration minioIntegration) {
        this.minioIntegration = minioIntegration;
    }

    /**
     * Получить ссылку на файл из Minio.
     *
     * @param fileName      имя файла
     * @param fileExtension расширение файла
     * @param fileBucket    название бакета
     * @return ссылка на файл
     */
    @Override
    public String getFileLink(String fileName, String fileExtension, String fileBucket) {
        return minioIntegration.getFileRequest(fileName, fileExtension, fileBucket, true, String.class);
    }

    /**
     * Получить байты файла из Minio.
     *
     * @param fileName      имя файла
     * @param fileExtension расширение файла
     * @param fileBucket    название бакета
     * @return массив байтов файла
     */
    @Override
    public byte[] getFileBytes(String fileName, String fileExtension, String fileBucket) {
        return minioIntegration.getFileRequest(fileName, fileExtension, fileBucket, false, byte[].class);
    }

    /**
     * Удалить файл из Minio.
     *
     * @param fileName      название файла
     * @param fileExtension расширение файла
     * @param fileBucket    название бакета
     */
    @Override
    public void deleteFile(String fileName, String fileExtension, String fileBucket) {
        minioIntegration.deleteFileRequest(fileName, fileExtension, fileBucket);
    }

}

