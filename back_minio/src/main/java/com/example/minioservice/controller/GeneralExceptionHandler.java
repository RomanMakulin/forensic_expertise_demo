package com.example.minioservice.controller;

import com.example.minioservice.exception.MinioStorageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GeneralExceptionHandler {

    /**
     * Обрабатывает все остальные неожиданные ошибки.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
    }

    /**
     * Обрабатывает ошибки взаимодействия с Minio.
     */
    @ExceptionHandler(MinioStorageException.class)
    public ResponseEntity<String> handleMinioStorageException(MinioStorageException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка взаимодействия CRUD с Minio: " + e.getMessage());
    }


}
