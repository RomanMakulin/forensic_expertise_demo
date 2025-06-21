package com.example.expertise.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Утилита для работы с файлами и их данными.
 * В основном для парсинга данных JSON и сбора файлов.
 */
public class FilesDataUtils {

    private static final Logger log = LoggerFactory.getLogger(FilesDataUtils.class);

    /**
     * Собирает все файлы из данных (JSON) в словарь, где ключ - название бакета, значение - список имен файлов
     *
     * @param node   - данные JSON
     * @param result - результирующий словарь с файлами, где ключ - название бакета, значение - список имен файлов
     */
    @SuppressWarnings("unchecked")
    public static void collectFilesFromData(Object node, Map<String, List<String>> result) {
        if (node instanceof Map<?, ?> mapNode) {
            // Если текущий объект — файл (имеет url + name)
            if (mapNode.containsKey("name") && mapNode.containsKey("url")) {
                addFile(result, mapNode);
            }

            // Рекурсивный обход всех значений
            for (Object value : mapNode.values()) {
                collectFilesFromData(value, result);
            }
        } else if (node instanceof List<?> listNode) {
            for (Object item : listNode) {
                collectFilesFromData(item, result);
            }
        }
    }

    /**
     * Добавляет файл в словарь
     *
     * @param result  - словарь с файлами
     * @param fileMap - словарь с файлами
     */
    private static void addFile(Map<String, List<String>> result, Map<?, ?> fileMap) {
        String name = String.valueOf(fileMap.get("name"));
        String url = String.valueOf(fileMap.get("url"));
        String bucket = extractBucketFromUrl(url);
        if (bucket != null && name != null) {
            result.computeIfAbsent(bucket, k -> new ArrayList<>()).add(name);
        }
    }

    /**
     * Извлекает название бакета из URL
     *
     * @param url URL в формате https://minio-server/bucket-name/file-name
     * @return Название бакета или null, если не удалось извлечь
     */
    public static String extractBucketFromUrl(String url) {
        try {
            URI uri = new URI(url);
            String path = uri.getPath(); // /bucket-name/file-name
            if (path != null && path.startsWith("/")) {
                String[] parts = path.substring(1).split("/", 2);
                return parts.length > 0 ? parts[0] : null;
            }
        } catch (Exception e) {
            log.warn("Не удалось извлечь bucket из URL: {}", url, e);
        }
        return null;
    }

}
