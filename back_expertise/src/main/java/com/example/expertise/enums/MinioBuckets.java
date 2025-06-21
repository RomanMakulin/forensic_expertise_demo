package com.example.expertise.enums;

import lombok.Getter;

/**
 * Список доступных бакетов для хранения файлов в Minio.
 */
@Getter
public enum MinioBuckets {
    USER_AVATARS("user-avatars"),
    USER_DIPLOMS("user-diploms"),
    USER_ADDITIONAL_DIPLOMS("user-additional-diploms"),
    USER_QUALIFICATION("user-qualification"),
    USER_PASSPORTS("user-passports"),
    USER_TEMPLATES("user-templates"),
    USER_CERTS("user-certs"),
    EXPERTISE_ANSWERS("expertise-answers");

    private final String bucket;

    MinioBuckets(String bucket) {
        this.bucket = bucket;
    }

    public String bucket() {
        return bucket;
    }
}
