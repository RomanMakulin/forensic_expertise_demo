package com.example.expertise.util;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;

/**
 * Утилитный класс для подготовки файлов к отправке через RestTemplate в виде multipart/form-data
 */
public class FileUploadUtil {

    /**
     * Создаёт InputStreamResource из MultipartFile
     *
     * @param file файл, загруженный пользователем
     * @return ресурс, пригодный для отправки
     */
    public static Resource toInputStreamResource(MultipartFile file) {
        try {
            return new InputStreamResource(file.getInputStream()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }

                @Override
                public long contentLength() {
                    return file.getSize();
                }
            };
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать файл: " + file.getOriginalFilename(), e);
        }
    }

    /**
     * Вычисляет хэш файла
     *
     * @param file файл
     * @return хэш в виде строки
     */
    public static String computeSHA256(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) digest.update(buffer, 0, bytesRead);
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при вычислении хэша файла", e);
        }
    }

    /**
     * Получение всех хэшей из JSON представления
     *
     * @param dataMap данные в формате JSON
     * @return множество хэшей
     */
    public static Set<String> getAllExistingFileHashes(Map<String, Object> dataMap) {
        Set<String> hashes = new HashSet<>();
        collectHashesRecursive(dataMap, hashes);
        return hashes;
    }

    /**
     * Собирает хэши рекурсивно
     *
     * @param node   нода для обработки
     * @param hashes коллекция для хранения результатов
     */
    @SuppressWarnings("unchecked")
    private static void collectHashesRecursive(Object node, Set<String> hashes) {
        if (node instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if ("hash".equals(entry.getKey()) && entry.getValue() instanceof String s) {
                    hashes.add(s);
                } else {
                    collectHashesRecursive(entry.getValue(), hashes);
                }
            }
        } else if (node instanceof List<?> list) {
            for (Object item : list) {
                collectHashesRecursive(item, hashes);
            }
        }
    }


    /**
     * Получает расширение файла по имени
     *
     * @param filename имя файла
     * @return расширение файла. В виде "jpg" или "png". Если расширение не найдено, вернёт пустую строку.
     */
    public static String getExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return (lastDot != -1 && lastDot != filename.length() - 1) ? filename.substring(lastDot + 1) : "";
    }

    /**
     * Универсальный метод, собирающий JSON представление файла
     *
     * @param id   идентификатор файла в базе данных
     * @param name имя файла
     * @param url  адрес, по которому можно получить файл
     * @param hash хэш файла
     * @return JSON представление файла
     */
    public static Map<String, Object> buildFileEntry(String id, String name, String url, String hash) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("id", id);
        entry.put("name", name);
        entry.put("url", url);
        entry.put("hash", hash);
        return entry;
    }


}
