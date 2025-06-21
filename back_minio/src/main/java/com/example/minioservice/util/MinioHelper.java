package com.example.minioservice.util;

import com.example.minioservice.exception.MinioStorageException;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Класс MinioHelper предоставляет методы для работы с MinIO.
 */
@Component
public class MinioHelper {

    private static final Logger log = LoggerFactory.getLogger(MinioHelper.class);
    private final MinioClient minioClient;

    @Value("${minio.buckets.avatars}")
    public String bucketAvatars;

    @Value("${minio.buckets.passports}")
    public String bucketPassports;

    @Value("${minio.buckets.diploms}")
    public String bucketDiploms;

    @Value("${minio.buckets.files}")
    public String bucketFiles;

    @Value("${minio.buckets.templates}")
    public String bucketTemplates;

    @Value("${minio.buckets.expertise-answers}")
    public String bucketExpertiseAnswers;

    /**
     * Конструктор класса MinioHelper.
     *
     * @param minioClient клиент MinIO
     */
    public MinioHelper(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * Загружает файл в MinIO.
     *
     * @param bucket     имя бакета
     * @param file       файл для загрузки
     * @param objectName имя объекта в MinIO
     */
    public void upload(String bucket, MultipartFile file, String objectName) {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.info("Файл загружен: bucket={}, objectName={}", bucket, objectName);
        } catch (Exception e) {
            log.error("Ошибка загрузки в MinIO: bucket={}, objectName={}", bucket, objectName, e);
            throw new MinioStorageException("Ошибка загрузки файла в MinIO", e);
        }
    }

    /**
     * Получает файл из MinIO.
     *
     * @param bucket     имя бакета
     * @param objectName имя объекта в MinIO
     * @return поток данных файла
     */
    public InputStream getObject(String bucket, String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("Ошибка получения файла из MinIO: bucket={}, objectName={}", bucket, objectName, e);
            throw new MinioStorageException("Ошибка получения файла из MinIO", e);
        }
    }

    /**
     * Получает ссылку для скачивания файла из MinIO.
     *
     * @param bucket     имя бакета
     * @param objectName имя объекта в MinIO
     * @return ссылка для скачивания файла
     */
    public String getObjectUrl(String bucket, String objectName) {
        try {
            // Проверяем, существует ли объект в MinIO
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );

            // Если объект есть, генерируем ссылку
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectName)
                            .expiry(3, TimeUnit.DAYS) // Срок действия ссылки 1 час
                            .build()
            );
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                log.error("Файл не найден в MinIO: bucket={}, objectName={}", bucket, objectName);
                throw new MinioStorageException("Файл не найден в хранилище MinIO: " + objectName);
            }
            throw new MinioStorageException("Ошибка при проверке файла в MinIO", e);
        } catch (Exception e) {
            log.error("Ошибка генерации presigned URL: bucket={}, objectName={}", bucket, objectName, e);
            throw new MinioStorageException("Ошибка генерации presigned URL", e);
        }
    }


    /**
     * Удаляет файл из MinIO.
     *
     * @param bucket     имя бакета
     * @param objectName имя объекта в MinIO
     */
    public void delete(String bucket, String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
            log.info("Файл удален: bucket={}, objectName={}", bucket, objectName);
        } catch (Exception e) {
            log.error("Ошибка удаления файла из MinIO: bucket={}, objectName={}", bucket, objectName, e);
            throw new MinioStorageException("Ошибка удаления файла из MinIO", e);
        }
    }

    /**
     * Получает список файлов из MinIO.
     * (Не получает сами файлы, лишь сведения о них)
     *
     * @param bucket имя бакета
     * @param prefix префикс для фильтрации файлов
     * @return список имен файлов
     */
    public List<String> listObjects(String bucket, String prefix) {
        List<String> objectNames = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(prefix)
                            .build()
            );
            for (Result<Item> result : results) {
                objectNames.add(result.get().objectName());
            }
        } catch (Exception e) {
            log.error("Ошибка получения списка файлов из MinIO: bucket={}", bucket, e);
            throw new MinioStorageException("Ошибка получения списка файлов из MinIO", e);
        }
        return objectNames;
    }

    /**
     * Получает файлы из MinIO по заданному префиксу.
     *
     * @param bucket имя бакета
     * @param prefix префикс для фильтрации файлов
     * @return список потоков данных файлов
     * @throws MinioStorageException если произошла ошибка при получении файлов
     */
    public List<InputStream> getObjectsByPrefix(String bucket, String prefix) {
        List<InputStream> objectStreams = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(prefix)
                            .build()
            );
            for (Result<Item> result : results) {
                String objectName = result.get().objectName();
                InputStream inputStream = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucket)
                                .object(objectName)
                                .build()
                );
                objectStreams.add(inputStream);
                log.info("Файл добавлен в результат: bucket={}, objectName={}", bucket, objectName);
            }
            return objectStreams;
        } catch (Exception e) {
            log.error("Ошибка получения файлов по префиксу из MinIO: bucket={}, prefix={}", bucket, prefix, e);
            throw new MinioStorageException("Ошибка получения файлов по префиксу из MinIO", e);
        }
    }

    /**
     * Удаляет все файлы из MinIO, начинающиеся с заданного префикса в указанном бакете.
     *
     * @param bucket имя бакета
     * @param prefix префикс, по которому фильтруются файлы для удаления (например, "profileId_")
     */
    public void deleteByPrefix(String bucket, String prefix) {
        // Получаем список объектов с заданным префиксом
        List<String> objectsToDelete = listObjects(bucket, prefix);

        if (objectsToDelete.isEmpty()) {
            log.info("Файлы с префиксом '{}' в бакете '{}' не найдены", prefix, bucket);
            return;
        }

        // Удаляем каждый объект из списка
        for (String objectName : objectsToDelete) {
            delete(bucket, objectName);
        }
    }

}
