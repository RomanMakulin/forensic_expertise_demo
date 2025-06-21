package com.example.expertise.exceptions;

/**
 * Исключение, выбрасываемое при ошибке генерации файла экспертизы.
 */
public class ExpertiseFileGenerationException extends RuntimeException {
    public ExpertiseFileGenerationException() {
        super("Expertise file generation failed");
    }
}
