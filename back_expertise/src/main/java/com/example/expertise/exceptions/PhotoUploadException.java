package com.example.expertise.exceptions;


/**
 * Исключение, выбрасываемое при ошибке загрузки фотографий.
 */
public class PhotoUploadException extends RuntimeException {

    public PhotoUploadException(String message) {
        super(message);
    }

    public PhotoUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}