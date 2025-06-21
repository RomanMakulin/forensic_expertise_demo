package com.example.expertise.enums;

/**
 * Список доступных расширений файлов.
 */
public enum FileExtension {
    PDF(".pdf"),
    JPG(".jpg"),
    DOCX(".docx");

    private final String extension;

    FileExtension(String extension) {
        this.extension = extension;
    }

    public String extension() {
        return extension;
    }
}
