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

/**
 * Реализация процессора проставления данных чек-листа по умолчанию.
 */
@Slf4j
@Component
@FileProcessorFor("Характеристики объекта строительства")
@FileProcessorFor("Дефекты объекта строительства")
public class DefaultChecklistDataProcessor implements ChecklistDataRender {

    private final MinioIntegration minioIntegration;
    private final ObjectMapper objectMapper;

    public DefaultChecklistDataProcessor(MinioIntegration minioIntegration, ObjectMapper objectMapper) {
        this.minioIntegration = minioIntegration;
        this.objectMapper = objectMapper;
    }

    @Override
    public void updateInstanceWithFilesAndFields(ChecklistInstance instance,
                                                 MultipartHttpServletRequest request,
                                                 String token,
                                                 CreateChecklistInstanceDto dto) {
        try {
            Map<String, Object> oldDataMap = new HashMap<>();
            if (instance.getData() != null) {
                try {
                    oldDataMap = objectMapper.readValue(instance.getData(), new TypeReference<>() {
                    });
                } catch (Exception e) {
                    log.warn("Не удалось распарсить старые данные чек-листа", e);
                }
            }

            // Установка новых данных (без файлов)
            instance.setData(objectMapper.writeValueAsString(dto.getData()));
            Map<String, Object> dataMap = objectMapper.readValue(instance.getData(), new TypeReference<>() {
            });
            Map<String, Set<String>> existingHashes = new HashMap<>();

            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            List<Callable<Void>> tasks = new ArrayList<>();

            Map<String, Object> finalOldDataMap = oldDataMap;
            request.getMultiFileMap().forEach((fullKey, files) -> {
                if ("payload".equals(fullKey)) return;
                ParsedKey key = parseKey(fullKey);
                if (key == null) return;

                List<Map<String, Object>> fileList = locateFileList(dataMap, key);
                List<Map<String, Object>> oldFileList = locateFileList(finalOldDataMap, key);
                if (fileList == null) return;

                String bucket = key.field();
                Set<String> bucketHashes = existingHashes.computeIfAbsent(bucket, k -> new HashSet<>());

                // Восстанавливаем старые файлы
                if (oldFileList != null) {
                    for (Map<String, Object> oldFile : oldFileList) {
                        String hash = (String) oldFile.get("hash");
                        if (hash != null && fileList.stream().noneMatch(f -> hash.equals(f.get("hash")))) {
                            fileList.add(oldFile);
                            bucketHashes.add(hash);
                        }
                    }
                }

                for (MultipartFile file : files) {
                    tasks.add(() -> {
                        String hash = FileUploadUtil.computeSHA256(file);
                        synchronized (bucketHashes) {
                            if (bucketHashes.contains(hash) || fileList.stream().anyMatch(f -> hash.equals(f.get("hash")))) {
                                return null;
                            }
                        }

                        String ext = FileUploadUtil.getExtension(file.getOriginalFilename());
                        String fileId = UUID.randomUUID().toString();
                        String fileName = instance.getId() + "_" + (key.parameter() != null ? key.parameter() : key.field()) + "_" + fileId;
                        String url = minioIntegration.uploadFileByParams(fileName, "." + ext, bucket, file, token);
                        Map<String, Object> entry = FileUploadUtil.buildFileEntry(fileId, fileName + "." + ext, url, hash);

                        synchronized (fileList) {
                            fileList.add(entry);
                        }
                        synchronized (bucketHashes) {
                            bucketHashes.add(hash);
                        }
                        return null;
                    });
                }
            });

            executor.invokeAll(tasks);
            executor.shutdown();

            instance.setData(objectMapper.writeValueAsString(dataMap));
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            log.error("Ошибка при обновлении чек-листа с файлами", e);
            throw new RuntimeException("Ошибка обработки чек-листа", e);
        }
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
            boolean modified = deleteFileFromJson(fileName, bucket, dataMap);

            if (modified) {
                instance.setData(objectMapper.writeValueAsString(dataMap));
                log.info("ChecklistInstance обновлён: файл '{}' удалён из bucket '{}'", fileName, bucket);
            } else {
                log.warn("Файл '{}' не найден в JSON по bucket '{}'", fileName, bucket);
            }
        } catch (Exception e) {
            log.error("Ошибка при удалении файла '{}' из экземпляра чек-листа", fileName, e);
            throw new RuntimeException("Ошибка при обновлении checklist instance после удаления файла", e);
        }
    }

    /**
     * Удаление файла из JSON по имени и bucket
     *
     * @param fileName - имя файла
     * @param bucket   - имя бакета
     * @param dataMap  - данные чек-листа
     * @return true, если файл был найден и удален
     */
    @SuppressWarnings("unchecked")
    private boolean deleteFileFromJson(String fileName, String bucket, Map<String, Object> dataMap) {
        boolean modified = false;
        Object premisesObj = dataMap.get("premises");
        if (!(premisesObj instanceof List<?> premisesList)) return false;

        for (Object premiseObj : premisesList) {
            if (!(premiseObj instanceof Map<?, ?> premise)) continue;

            for (Map.Entry<?, ?> entry : premise.entrySet()) {
                String key = Objects.toString(entry.getKey(), "");
                if ("premise_name".equals(key) || "premise_parameters".equals(key)) continue;

                Object value = entry.getValue();
                if (value instanceof Map<?, ?> paramBlock) {
                    Object fileListObj = paramBlock.get(bucket);
                    if (fileListObj instanceof List<?> fileList) {
                        List<Map<String, Object>> updated = fileList.stream()
                                .filter(f -> !(f instanceof Map<?, ?> map) || !fileName.equals(map.get("name")))
                                .map(f -> (Map<String, Object>) f)
                                .toList();
                        if (updated.size() != fileList.size()) {
                            ((Map<String, Object>) paramBlock).put(bucket, updated);
                            modified = true;
                        }
                    }
                } else if (value instanceof List<?> fileList && bucket.equals(key)) {
                    List<Map<String, Object>> updated = fileList.stream()
                            .filter(f -> !(f instanceof Map<?, ?> map) || !fileName.equals(map.get("name")))
                            .map(f -> (Map<String, Object>) f)
                            .toList();
                    if (updated.size() != fileList.size()) {
                        ((Map<String, Object>) premise).put(bucket, updated);
                        modified = true;
                    }
                }
            }
        }

        return modified;
    }

    /**
     * Поиск файла в списке файлов в JSON
     *
     * @param dataMap - данные чек-листа
     * @param key     - ключ поиска
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> locateFileList(Map<String, Object> dataMap, ParsedKey key) {
        Object premisesObj = dataMap.get("premises");
        if (!(premisesObj instanceof List<?> premises)) return null;

        Optional<Map<String, Object>> matchedPremiseOpt = premises.stream()
                .filter(p -> {
                    Map<String, Object> map = (Map<String, Object>) p;
                    if (key.premiseName() != null) return key.premiseName().equals(map.get("premise_name"));
                    if (key.parameter() != null) {
                        Object paramList = map.get("premise_parameters");
                        return paramList instanceof List<?> list && list.contains(key.parameter());
                    }
                    return false;
                })
                .map(p -> (Map<String, Object>) p)
                .findFirst();

        if (matchedPremiseOpt.isEmpty()) return null;
        Map<String, Object> premise = matchedPremiseOpt.get();

        if (key.parameter() == null) {
            return (List<Map<String, Object>>) premise.computeIfAbsent(key.field(), f -> new ArrayList<>());
        } else {
            Map<String, Object> paramBlock = (Map<String, Object>) premise.computeIfAbsent(key.parameter(), k -> new LinkedHashMap<>());
            return (List<Map<String, Object>>) paramBlock.computeIfAbsent(key.field(), f -> new ArrayList<>());
        }
    }

    private record ParsedKey(String parameter, String field, String premiseName) {
    }

    /**
     * Парсинг ключей файлов в чек-листе
     *
     * @param fullKey - полный ключ, содержащий: помещение, параметр, бакет в minIO
     * @return парсинг результата
     */
    private ParsedKey parseKey(String fullKey) {
        String[] parts = fullKey.split("__");
        return switch (parts.length) {
            case 3 -> new ParsedKey(parts[1], parts[2], parts[0]);
            case 2 -> {
                String left = parts[0];
                String right = parts[1];
                if (Set.of("defect-photos", "defect_volume", "defect_note", "defect_description").contains(right)) {
                    yield new ParsedKey(left, right, null);
                } else {
                    yield new ParsedKey(null, right, left);
                }
            }
            default -> {
                log.warn("Некорректный ключ файла: {}", fullKey);
                yield null;
            }
        };
    }
}
