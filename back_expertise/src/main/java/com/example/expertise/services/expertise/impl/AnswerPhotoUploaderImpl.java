package com.example.expertise.services.expertise.impl;

import com.example.expertise.dto.expertise.AnswerDto;
import com.example.expertise.exceptions.PhotoUploadException;
import com.example.expertise.integration.minio.MinioIntegration;
import com.example.expertise.model.expertise.ExpertisePhoto;
import com.example.expertise.model.expertise.ExpertiseQuestion;
import com.example.expertise.services.expertise.AnswerPhotoUploader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

/**
 * Класс для управления фотографиями в ответах на экспертные вопросы.
 */
@Component
public class AnswerPhotoUploaderImpl implements AnswerPhotoUploader {

    private static final Logger log = LoggerFactory.getLogger(AnswerPhotoUploaderImpl.class);

    private static final int MAX_CONCURRENT_UPLOADS = 5; // Ограничение на количество параллельных загрузок
    private final Semaphore uploadSemaphore = new Semaphore(MAX_CONCURRENT_UPLOADS);

    private final Executor taskExecutor;
    private final MinioIntegration minioIntegration;

    public AnswerPhotoUploaderImpl(@Qualifier("taskExecutor") Executor taskExecutor,
                                   MinioIntegration minioIntegration) {
        this.taskExecutor = taskExecutor;
        this.minioIntegration = minioIntegration;
    }

    /**
     * Загружает фотографии, если они присутствуют в данных ответа.
     */
    @Override
    public void uploadPhotosIfPresent(ExpertiseQuestion expertiseQuestion, AnswerDto answerDto) {
        if (answerDto.getPhotos() != null && !answerDto.getPhotos().isEmpty()) {
            uploadPhotos(expertiseQuestion, answerDto.getPhotos(),
                    expertiseQuestion.getExpertise().getId(), expertiseQuestion.getId());
        }
    }

    /**
     * Асинхронно загружает фотографии в MinIO и сохраняет ссылки на них в базе данных.
     *
     * @param expertiseQuestion экспертный вопрос, к которому относятся фотографии
     * @param photos            список фотографий для загрузки
     * @param expertiseId       идентификатор экспертизы
     * @param questionId        идентификатор вопроса экспертизы
     * @throws PhotoUploadException если загрузка одной или нескольких фотографий не удалась
     */
    private void uploadPhotos(ExpertiseQuestion expertiseQuestion, List<MultipartFile> photos, UUID expertiseId, UUID questionId) {
        List<ExpertisePhoto> photoRecords = new ArrayList<>();

        // Сохраняем текущий контекст запроса для передачи в асинхронные потоки
        ServletRequestAttributes requestContext = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestContext == null) {
            throw new IllegalStateException("Request context not found in main thread");
        }

        try {
            List<CompletableFuture<Void>> uploadTasks = createUploadTasks(expertiseQuestion, photoRecords, photos, expertiseId, questionId, requestContext);
            CompletableFuture.allOf(uploadTasks.toArray(new CompletableFuture[0])).join();
            expertiseQuestion.setPhotos(photoRecords);
        } catch (Exception e) {
            log.error("Failed to upload photos for question ID: {} and expertiseId: {}", questionId, expertiseId, e);
            throw new PhotoUploadException("Failed to upload one or more photos for question ID: " + questionId, e);
        }
    }

    /**
     * Создаёт список асинхронных задач для загрузки фотографий.
     *
     * @param expertiseQuestion экспертный вопрос
     * @param photoRecords      список записей фотографий для сохранения
     * @param photos            список фотографий для загрузки
     * @param expertiseId       идентификатор экспертизы
     * @param questionId        идентификатор вопроса
     * @param requestContext    контекст текущего HTTP-запроса
     * @return список асинхронных задач
     */
    private List<CompletableFuture<Void>> createUploadTasks(ExpertiseQuestion expertiseQuestion,
                                                            List<ExpertisePhoto> photoRecords,
                                                            List<MultipartFile> photos,
                                                            UUID expertiseId,
                                                            UUID questionId,
                                                            ServletRequestAttributes requestContext) {
        return photos.stream()
                .map(photo -> createUploadTask(expertiseQuestion, photoRecords, photo, expertiseId, questionId, requestContext))
                .toList();
    }

    /**
     * Создаёт и возвращает асинхронную задачу для загрузки одной фотографии.
     *
     * @param expertiseQuestion экспертный вопрос
     * @param photoRecords      список записей фотографий
     * @param photo             фотография для загрузки
     * @param expertiseId       идентификатор экспертизы
     * @param questionId        идентификатор вопроса
     * @param requestContext    контекст текущего HTTP-запроса
     * @return асинхронная задача
     */
    private CompletableFuture<Void> createUploadTask(ExpertiseQuestion expertiseQuestion,
                                                     List<ExpertisePhoto> photoRecords,
                                                     MultipartFile photo,
                                                     UUID expertiseId,
                                                     UUID questionId,
                                                     ServletRequestAttributes requestContext) {
        UUID photoId = UUID.randomUUID();
        String photoName = String.format("%s_%s_%s.jpg", expertiseId, questionId, photoId);
        ExpertisePhoto photoRecord = new ExpertisePhoto(photoId, photoName, expertiseQuestion);
        photoRecords.add(photoRecord);

        return CompletableFuture.runAsync(() -> {
            try {
                uploadSemaphore.acquire(); // Ограничиваем параллелизм
                RequestContextHolder.setRequestAttributes(requestContext);
                minioIntegration.uploadAnswerPhoto(photoName, photo);
                log.info("Successfully uploaded photo: {}", photoName);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Upload interrupted for photo: " + photoName, e);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload photo: " + photoName, e);
            } finally {
                uploadSemaphore.release();
                RequestContextHolder.resetRequestAttributes();
            }
        }, taskExecutor).exceptionally(throwable -> {
            log.error("Upload failed for photo: {}", photoName, throwable);
            throw new RuntimeException("Failed to upload photo: " + photoName, throwable);
        });
    }

}
