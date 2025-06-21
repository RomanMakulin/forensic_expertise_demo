package com.example.expertise.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;

/**
 * Утилитный компонент для работы с MinIO.
 */
public class MinioUtils {

    private static final Logger log = LoggerFactory.getLogger(MinioUtils.class);

    /**
     * Десериализует ответ в список файлов в виде байтов.
     *
     * @param jsonResponse ответ в формате JSON
     * @return список файлов в виде байтов
     */
    public static List<byte[]> deserializeJsonToListBytes(String jsonResponse) {
        try {
            // Десериализуем JSON в List<String>
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> base64Files = objectMapper.readValue(jsonResponse,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));

            // Декодируем Base64 в byte[]
            return decodeBase64Files(base64Files);
        } catch (Exception e) {
            log.error("Ошибка десериализации ответа: {}", e);
            throw new RuntimeException("Ошибка обработки ответа от первого бэкенда", e);
        }
    }

    /**
     * Декодирует список Base64-строк в список байтов.
     *
     * @param base64Files список Base64-строк
     * @return список байтов
     */
    private static List<byte[]> decodeBase64Files(List<String> base64Files) {
        return base64Files.stream()
                .map(base64 -> {
                    try {
                        return Base64.getDecoder().decode(base64);
                    } catch (IllegalArgumentException e) {
                        log.error("Ошибка декодирования Base64", e);
                        throw new RuntimeException("Ошибка при декодировании файла из Base64", e);
                    }
                })
                .toList();
    }

}
