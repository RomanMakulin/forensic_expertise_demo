package com.example.expertise.enums;

import lombok.Getter;

/**
 * Типы файлов профиля
 */
public enum FileType {
    ADDITIONAL_DIPLOMA("additionalDiplomas"),
    CERTIFICATE("certificates"),
    QUALIFICATION_CERTIFICATE("qualifications");

    private final String type;

    FileType(String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }
}
