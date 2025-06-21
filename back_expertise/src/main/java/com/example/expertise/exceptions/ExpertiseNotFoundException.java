package com.example.expertise.exceptions;

/**
 * Ошибка в случае, если экспертиза не найдена
 */
public class ExpertiseNotFoundException extends IllegalArgumentException {
    public ExpertiseNotFoundException() {
        super("Expertise Not Found");
    }
}
