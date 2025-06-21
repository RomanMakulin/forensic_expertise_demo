package com.example.expertise.services.cache;

import com.example.expertise.model.checklist.ChecklistTemplate;
import com.example.expertise.repository.checklist.ChecklistTemplateRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Реализация кэша для шаблонов чек-листов
 */
@Component
public class ChecklistTemplateCache {

    private static final Logger log = LoggerFactory.getLogger(ChecklistTemplateCache.class);
    // Кэш: ID шаблона -> мапа ключей и "человеческих" названий полей
    private final Map<UUID, Map<String, String>> fieldNameCache = new HashMap<>();

    // Кэш: ID шаблона -> типовой текст
    private final Map<UUID, String> typeTextCache = new HashMap<>();

    private final ChecklistTemplateRepository checklistTemplateRepository;

    public ChecklistTemplateCache(ChecklistTemplateRepository checklistTemplateRepository) {
        this.checklistTemplateRepository = checklistTemplateRepository;
    }

    /**
     * Инициализация кэша при старте приложения
     */
    @PostConstruct
    public void init() {
        List<ChecklistTemplate> templates = checklistTemplateRepository.findAll();

        for (ChecklistTemplate template : templates) {
            UUID templateId = template.getId();

            // Заполнение кэша с именами полей
            fieldNameCache.put(templateId, extractFieldNameMapProcess(template.getStructure()));

            // Безопасное добавление типового текста
            Object typeTextRaw = template.getStructure().get("type_text");
            if (typeTextRaw instanceof String text && !text.isBlank()) typeTextCache.put(templateId, text);
            else log.info("Не указан type_text для шаблона {}", template.getName());
        }
    }


    /**
     * Возвращает типовой текст по ID шаблона
     *
     * @param templateId ID шаблона
     * @return Optional.empty() если шаблона нет или не указан type_text
     */
    public Optional<String> getTypeText(UUID templateId) {
        return Optional.ofNullable(typeTextCache.get(templateId));
    }

    /**
     * Возвращает мапу всех ключей -> названий по ID шаблона
     */
    public Map<String, String> getFieldNameMap(UUID templateId) {
        return fieldNameCache.getOrDefault(templateId, Map.of());
    }

    /**
     * Возвращает название поля по ключу и ID шаблона
     */
    public Optional<String> getFieldName(UUID templateId, String fieldKey) {
        return Optional.ofNullable(fieldNameCache.getOrDefault(templateId, Map.of()).get(fieldKey));
    }

    /**
     * Извлекает map ключ -> name из структуры JSON шаблона
     */
    private Map<String, String> extractFieldNameMapProcess(Map<String, Object> structure) {
        Map<String, String> result = new LinkedHashMap<>();

        extractMetadataRaw(structure, result); // metadata
        extractParamsRaw(structure, result); // parameters (и всё, что в них)

        result.putIfAbsent("gosts", "Нормативная документация");
        return result;
    }

    /**
     * Извлекает map ключ -> name из структуры JSON шаблона
     *
     * @param structure JSON шаблона
     */
    private void extractMetadataRaw(Map<String, Object> structure, Map<String, String> result) {
        Object metadataRaw = structure.get("metadata");
        if (metadataRaw instanceof Map<?, ?> metadata) {
            for (Map.Entry<?, ?> entry : metadata.entrySet()) {
                if (!(entry.getKey() instanceof String key)) continue;
                if (!(entry.getValue() instanceof Map<?, ?> value)) continue;
                Object name = value.get("name");
                if (name instanceof String n) {
                    result.put(key, n);
                }
            }
        }
    }

    /**
     * Извлекает map ключ -> name из структуры JSON шаблона
     *
     * @param structure JSON шаблона
     * @param result    Мапа ключей и названий
     */
    private void extractParamsRaw(Map<String, Object> structure, Map<String, String> result) {
        Object paramsRaw = structure.get("parameters");
        if (paramsRaw != null) {
            collectKeysRecursively(paramsRaw, result);
        }
    }

    /**
     * Собирает все ключи из структуры JSON шаблона
     *
     * @param node   Текущий узел в структуре JSON
     * @param result Мапа ключей и названий
     */
    @SuppressWarnings("unchecked")
    private void collectKeysRecursively(Object node, Map<String, String> result) {
        if (node instanceof Map<?, ?> map) {
            // Попробуем взять key и name прямо на этом уровне
            Object rawKey = map.get("key");
            Object rawName = map.get("name");

            if (rawKey instanceof String key && rawName instanceof String name) {
                result.put(key, name);
            }

            // Обходим все значения в этой мапе
            for (Object value : map.values()) {
                collectKeysRecursively(value, result);
            }

        } else if (node instanceof List<?> list) {
            // Обходим все элементы списка
            for (Object item : list) {
                collectKeysRecursively(item, result);
            }
        }
    }

}
