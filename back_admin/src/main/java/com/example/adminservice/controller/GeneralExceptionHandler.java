package com.example.adminservice.controller;

import com.example.adminservice.dto.ApiErrorResponse;
import com.example.adminservice.exceptions.IntegrationException;
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
     * Обрабатывает ошибки интеграции.
     */
    @ExceptionHandler(IntegrationException.class)
    public ResponseEntity<ApiErrorResponse> handleIntegrationException(IntegrationException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Ошибка интеграции: " + e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }


}
