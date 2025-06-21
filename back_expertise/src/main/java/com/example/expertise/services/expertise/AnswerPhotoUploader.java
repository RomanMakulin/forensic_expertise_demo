package com.example.expertise.services.expertise;

import com.example.expertise.dto.expertise.AnswerDto;
import com.example.expertise.model.expertise.ExpertiseQuestion;

/**
 * Вспомогательный класс для управления фотографиями ответов на вопросы.
 */
public interface AnswerPhotoUploader {

    /**
     * Загружает фотографии, если они присутствуют в данных ответа.
     */
    void uploadPhotosIfPresent(ExpertiseQuestion expertiseQuestion, AnswerDto answerDto);
}
