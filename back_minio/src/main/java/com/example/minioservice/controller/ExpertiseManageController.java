package com.example.minioservice.controller;

import com.example.minioservice.service.ExpertiseManageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Контроллер работы с фото экспертиз
 */
@RestController
@RequestMapping("/api/files")
public class ExpertiseManageController {

    private final ExpertiseManageService expertiseManageService;

    public ExpertiseManageController(ExpertiseManageService expertiseManageService) {
        this.expertiseManageService = expertiseManageService;
    }

    /**
     * API для получения фото ответа экспертизы.
     *
     * @param photoName название фото
     * @return название фото
     */
    @GetMapping("/get-expertise-answer-photo/{photoName}")
    public ResponseEntity<String> getExpertiseAnswerPhoto(@PathVariable String photoName) {
        return ResponseEntity.ok(expertiseManageService.getAnswerPhotoByName(photoName));
    }

    /**
     * API для получения фото ответа экспертизы.
     *
     * @param photoName название фото
     * @return фото ответа экспертизы
     */
    @GetMapping("/get-expertise-answer-photo-file/{photoName}")
    public ResponseEntity<byte[]> getExpertiseAnswerPhotoFile(@PathVariable String photoName) {
        try {
            byte[] fileData = expertiseManageService.getAnswerPhotoFileAsBytes(photoName);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expertise-" + photoName);
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(fileData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }


    /**
     * API для получения файла экспертизы пользователя (шаблон для дальнейшего заполнения)
     *
     * @param templateName название шаблона
     * @return файл экспертизы
     */
    @GetMapping("/get-expertise-file/{templateName}")
    public ResponseEntity<InputStreamResource> getExpertiseFile(@PathVariable String templateName) {
        try {
            InputStreamResource resource = expertiseManageService.getExpertise(templateName);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expertise-" + templateName + ".docx");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * API для загрузки фото ответа при формировании экспертизы.
     *
     * @param photoName сформированное имя файла фотографии
     * @param photo     фотография ответа
     * @return ответ сервера
     */
    @PostMapping("/upload-expertise-answer-photo")
    public ResponseEntity<Void> uploadExpertiseAnswerPhoto(@RequestParam("photoName") String photoName,
                                                           @RequestParam("photo") MultipartFile photo) {
        expertiseManageService.uploadAnswerPhoto(photoName, photo);
        return ResponseEntity.ok().build();
    }

    /**
     * API для удаления фото всех ответов по экспертизе.
     *
     * @param expertiseId ID экспертизы
     * @return статус операции
     */
    @DeleteMapping("/delete-expertise-answer-photos/{expertiseId}")
    public ResponseEntity<Void> deleteExpertiseAnswerPhotos(@PathVariable UUID expertiseId) {
        expertiseManageService.deleteAnswerPhotos(expertiseId);
        return ResponseEntity.ok().build();
    }

    /**
     * API для удаления фото ответа в экспертизе.
     *
     * @param photoName название файла
     * @return статус операции
     */
    @DeleteMapping("/delete-expertise-answer-photo/{photoName}")
    public ResponseEntity<Void> deleteExpertiseAnswerPhoto(@PathVariable String photoName) {
        expertiseManageService.deleteAnswerPhoto(photoName);
        return ResponseEntity.ok().build();
    }

}
