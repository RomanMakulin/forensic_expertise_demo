package com.example.expertise.exceptions;

/**
 * Ошибка получения фотографии ответа экспертизы.
 */
public class ExpertisePhotoNotFoundException extends IllegalArgumentException {
    public ExpertisePhotoNotFoundException() {
        super("Expertise photo Not Found");
    }
}
