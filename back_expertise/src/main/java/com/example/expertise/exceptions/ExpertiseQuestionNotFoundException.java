package com.example.expertise.exceptions;

/**
 * Ошибка в случае, если экспертиза не найдена
 */
public class ExpertiseQuestionNotFoundException extends IllegalArgumentException {
    public ExpertiseQuestionNotFoundException() {
        super("Expertise Question Not Found");
    }
}
