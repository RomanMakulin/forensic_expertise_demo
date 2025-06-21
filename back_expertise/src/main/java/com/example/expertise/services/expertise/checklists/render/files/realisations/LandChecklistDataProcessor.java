package com.example.expertise.services.expertise.checklists.render.files.realisations;

import com.example.expertise.dto.checklist.CreateChecklistInstanceDto;
import com.example.expertise.integration.minio.MinioIntegration;
import com.example.expertise.model.checklist.ChecklistInstance;
import com.example.expertise.services.expertise.checklists.render.annotation.FileProcessorFor;
import com.example.expertise.services.expertise.checklists.render.files.ChecklistDataRender;
import com.example.expertise.util.FileUploadUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@FileProcessorFor("Определение соответствия земельного участка")
public class LandChecklistDataProcessor implements ChecklistDataRender {

    private final MinioIntegration minioIntegration;
    private final ObjectMapper objectMapper;

    public LandChecklistDataProcessor(MinioIntegration minioIntegration, ObjectMapper objectMapper) {
        this.minioIntegration = minioIntegration;
        this.objectMapper = objectMapper;
    }

    @Override
    public void updateInstanceWithFilesAndFields(ChecklistInstance instance,
                                                 MultipartHttpServletRequest request,
                                                 String token,
                                                 CreateChecklistInstanceDto dto) {
        try {
            // 1. Загружаем старые данные
            Map<String, Object> oldDataMap = saveOldDataMap(instance);

            // 2. Копируем новые поля
            Map<String, Object> newDataMap = new LinkedHashMap<>(dto.getData());

            // 3. Восстанавливаем старые файлы
            restoreOldFiles(oldDataMap, newDataMap);

            // 4. Собираем хэши из newDataMap
            Map<String, Set<String>> existingHashes = collectHashesFromOld(newDataMap);

            // 5. Загрузка новых файлов
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            List<Callable<Void>> tasks = new ArrayList<>();

            request.getMultiFileMap().forEach((key, files) -> {
                if ("payload".equals(key)) return;

                String bucket = key.startsWith("open-source-images__") ? "open-source-images" : key;
                String description = key.startsWith("open-source-images__") ? key.substring("open-source-images__".length()).trim() : null;

                for (MultipartFile file : files) {
                    tasks.add(() -> {
                        uploadFileIfNew(file, instance, token, bucket, description, newDataMap, existingHashes);
                        return null;
                    });
                }
            });

            executor.invokeAll(tasks);
            executor.shutdown();

            // 6. Сохраняем итог
            instance.setData(objectMapper.writeValueAsString(newDataMap));
        } catch (Exception e) {
            log.error("Ошибка при обработке чек-листа с файлами", e);
            throw new RuntimeException("Ошибка при обработке чек-листа", e);
        }
    }

    /**
     * Сохраняет старые данные чек-листа
     *
     * @param instance чек-лист
     * @return карта данных
     */
    private Map<String, Object> saveOldDataMap(ChecklistInstance instance) {
        try {
            return instance.getData() != null
                    ? objectMapper.readValue(instance.getData(), new TypeReference<Map<String, Object>>() {
            })
                    : new HashMap<>();
        } catch (Exception e) {
            log.warn("Не удалось распарсить старые данные чек-листа", e);
            return new HashMap<>();
        }
    }

    /**
     * Загружает файл, если он новый
     *
     * @param file           файл
     * @param instance       чек-лист
     * @param token          токен
     * @param bucket         bucket
     * @param description    описание
     * @param dataMap        карта данных
     * @param existingHashes список хэшей
     */
    private void uploadFileIfNew(MultipartFile file,
                                 ChecklistInstance instance,
                                 String token,
                                 String bucket,
                                 String description,
                                 Map<String, Object> dataMap,
                                 Map<String, Set<String>> existingHashes) {
        try {
            String hash = FileUploadUtil.computeSHA256(file);
            Set<String> hashes = existingHashes.computeIfAbsent(bucket, k -> new HashSet<>());

            if (hashes.contains(hash)) return;

            String ext = FileUploadUtil.getExtension(file.getOriginalFilename());
            String fileId = UUID.randomUUID().toString();
            String fileName = instance.getId() + "_" + bucket + "_" + fileId;
            String url = minioIntegration.uploadFileByParams(fileName, "." + ext, bucket, file, token);

            Map<String, Object> entry = FileUploadUtil.buildFileEntry(fileId, fileName + "." + ext, url, hash);
            List<Map<String, Object>> fileList = (List<Map<String, Object>>) dataMap.computeIfAbsent(bucket, k -> new ArrayList<>());

            if ("open-source-images".equals(bucket)) {
                Map<String, Object> wrapper = new HashMap<>();
                wrapper.put("file", entry);
                wrapper.put("description", description);
                fileList.add(wrapper);
            } else {
                fileList.add(entry);
            }

            hashes.add(hash);
        } catch (Exception e) {
            log.error("Ошибка при загрузке файла в bucket '{}'", bucket, e);
            throw new RuntimeException("Ошибка загрузки файла", e);
        }
    }

    /**
     * Восстанавливает старые файлы в новом чек-листе
     *
     * @param oldMap карта старых данных чек-листа
     * @param newMap карта новых данных чек-листа
     */
    private void restoreOldFiles(Map<String, Object> oldMap, Map<String, Object> newMap) {
        oldMap.forEach((bucket, oldListObj) -> {
            if (!(oldListObj instanceof List<?> oldList)) return;

            List<Map<String, Object>> newList = (List<Map<String, Object>>) newMap.computeIfAbsent(bucket, k -> new ArrayList<>());
            Set<String> newHashes = newList.stream()
                    .map(item -> extractHash(bucket, item))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            for (Object item : oldList) {
                if (!(item instanceof Map<?, ?> map)) continue;
                String hash = extractHash(bucket, map);
                if (hash != null && !newHashes.contains(hash)) {
                    newList.add((Map<String, Object>) map);
                }
            }
        });
    }

    /**
     * Извлекает хэш файла из элемента списка
     *
     * @param bucket название бакета
     * @param item   элемент списка
     * @return хэш или null, если не найден
     */
    private String extractHash(String bucket, Map<?, ?> item) {
        if ("open-source-images".equals(bucket)) {
            return Optional.ofNullable(item.get("file"))
                    .filter(f -> f instanceof Map)
                    .map(f -> ((Map<?, ?>) f).get("hash"))
                    .map(Object::toString)
                    .orElse(null);
        } else {
            return Optional.ofNullable(item.get("hash")).map(Object::toString).orElse(null);
        }
    }

    /**
     * Собирает хэши из старых данных чек-листа
     *
     * @param oldDataMap карта старых данных
     * @return карта хэшей по бакетам
     */
    private Map<String, Set<String>> collectHashesFromOld(Map<String, Object> oldDataMap) {
        Map<String, Set<String>> result = new HashMap<>();

        oldDataMap.forEach((bucket, listObj) -> {
            if (!(listObj instanceof List<?> list)) return;

            Set<String> hashes = list.stream()
                    .filter(item -> item instanceof Map)
                    .map(item -> extractHash(bucket, (Map<?, ?>) item))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (!hashes.isEmpty()) {
                result.put(bucket, hashes);
            }
        });

        return result;
    }

    @Override
    public void processDeletedFiles(String fileName, String bucket, ChecklistInstance instance) {
        try {
            String extension = FileUploadUtil.getExtension(fileName);
            String baseName = fileName.replace("." + extension, "");
            minioIntegration.deleteFileByParams(baseName, "." + extension, bucket);
            log.info("Удаление из MinIO: bucket='{}', object='{}'", bucket, fileName);

            Map<String, Object> dataMap = objectMapper.readValue(instance.getData(), new TypeReference<>() {
            });
            Object listObj = dataMap.get(bucket);
            if (!(listObj instanceof List<?> list)) return;

            List<Map<String, Object>> updated = list.stream()
                    .filter(item -> {
                        if (!(item instanceof Map<?, ?> map)) return true;
                        if ("open-source-images".equals(bucket)) {
                            return Optional.ofNullable(map.get("file"))
                                    .filter(f -> f instanceof Map)
                                    .map(f -> !fileName.equals(((Map<?, ?>) f).get("name")))
                                    .orElse(true);
                        } else {
                            return !fileName.equals(map.get("name"));
                        }
                    })
                    .map(item -> (Map<String, Object>) item)
                    .toList();

            dataMap.put(bucket, updated);
            instance.setData(objectMapper.writeValueAsString(dataMap));
        } catch (Exception e) {
            log.error("Ошибка при удалении файла '{}' из экземпляра чек-листа", fileName, e);
            throw new RuntimeException("Ошибка удаления файла", e);
        }
    }
}
