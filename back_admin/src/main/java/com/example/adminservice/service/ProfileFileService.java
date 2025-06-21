package com.example.adminservice.service;

/**
 * Сервис для работы с файлами профиля.
 */
public interface ProfileFileService {

    /**
     * Получить ссылку на файл.
     *
     * @param fileName      имя файла
     * @param fileExtension расширение файла
     * @param fileBucket    название бакета
     * @return ссылка на файл
     */
    String getFileLink(String fileName, String fileExtension, String fileBucket);

    /**
     * Получить байты файла.
     *
     * @param fileName      имя файла
     * @param fileExtension расширение файла
     * @param fileBucket    название бакета
     * @return массив байтов файла
     */
    byte[] getFileBytes(String fileName, String fileExtension, String fileBucket);

    /**
     * Удалить файл.
     *
     * @param fileName      название файла
     * @param fileExtension расширение файла
     * @param fileBucket    название бакета
     */
    void deleteFile(String fileName, String fileExtension, String fileBucket);
}