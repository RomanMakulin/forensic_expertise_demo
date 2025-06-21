package com.example.expertise.controller;

import com.example.expertise.exceptions.ExpertiseNotFoundException;
import com.example.expertise.exceptions.ExpertiseQuestionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Класс, который отлавливает ошибки и возвращает соответствующий ответ клиенту
 */
@RestControllerAdvice
public class GeneralExceptionHandler {

    /**
     * Обработка ошибки, если экспертиза не найдена
     *
     * @param e - ошибка
     * @return ответ с сообщением об ошибке
     */
    @ExceptionHandler(ExpertiseNotFoundException.class)
    public ResponseEntity<String> handleExpertiseNotFoundException(ExpertiseNotFoundException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ExpertiseNotFoundException: " + e.getMessage());
    }

    /**
     * Обработка ошибки, если вопрос экспертизы не найден
     *
     * @param e - ошибка
     * @return ответ с сообщением об ошибке
     */
    @ExceptionHandler(ExpertiseQuestionNotFoundException.class)
    public ResponseEntity<String> handleExpertiseQuestionNotFoundException(ExpertiseQuestionNotFoundException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ExpertiseQuestionNotFoundException: " + e.getMessage());
    }

    /**
     * Обработка общей ошибки
     *
     * @param e - ошибка
     * @return ответ с сообщением об ошибке
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
    }

}
