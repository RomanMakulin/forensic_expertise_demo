package com.example.expertise.services.expertise;

import com.example.expertise.model.expertise.ExpertiseQuestion;

/**
 * Интерфейс для генерации выводов
 */
public interface ConclusionGenerator {
    /**
     * Добавляет заключение к вопросу, если оно существует и не пустое.
     */
    void addConclusionIfPresent(ExpertiseQuestion expertiseQuestion, String answer);
}
