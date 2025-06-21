package com.example.expertise.services.expertise.checklists.render.document;

import com.example.expertise.model.checklist.ChecklistInstance;
import com.example.expertise.repository.checklist.ChecklistInstanceRepository;
import com.example.expertise.services.cache.ChecklistTemplateCache;
import com.example.expertise.services.expertise.checklists.ChecklistTemplateService;
import com.example.expertise.util.docs.TableUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Абстрактный класс для рендеринга чек-листов.
 */
@AllArgsConstructor
public abstract class AbstractChecklistRenderer implements DocumentChecklistRenderer {

    private static final String TYPE_TEXT_PARAMS = "type_text_params";
    private static final Logger log = LoggerFactory.getLogger(AbstractChecklistRenderer.class);

    protected final ChecklistTemplateCache templateCache;
    protected final ObjectMapper objectMapper = new ObjectMapper();
    private final ChecklistTemplateService checklistTemplateService;
    private final ChecklistInstanceRepository checklistInstanceRepository;

    // Для того, что бы не пробрасывать объект checklistInstance каждый раз по цепочке, используем
    protected static final ThreadLocal<ChecklistInstance> checklistInstanceThreadLocal = new ThreadLocal<>();

    @Override
    public void insertAnswerChecklist(WordprocessingMLPackage wordPackage, ChecklistInstance checklist, P placeholderParagraph) {
        MainDocumentPart mainPart = wordPackage.getMainDocumentPart();
        LinkedHashMap<String, Object> data = parseChecklistData(checklist.getData());
        UUID templateId = checklist.getChecklistTemplate().getId();
        Map<String, String> fieldNameMap = templateCache.getFieldNameMap(templateId);

        Map<String, Object> typeParams = Optional.ofNullable(data.get("type_text_params"))
                .filter(Map.class::isInstance)
                .map(obj -> (Map<String, Object>) obj)
                .orElse(Collections.emptyMap());

        // Вызов insertChecklistContent отвечает и за вставку типового текста
        insertChecklistContent(wordPackage, mainPart, data, templateId,
                checklist, placeholderParagraph, fieldNameMap);
    }


    protected LinkedHashMap<String, Object> parseChecklistData(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Ошибка парсинга JSON чек-листа", e);
        }
    }

    protected P insertTypeText(MainDocumentPart mainPart, UUID templateId, Map<String, Object> params,
                               Map<String, String> fieldNameMap, P placeholderParagraph, ChecklistInstance checklist) {
        return templateCache.getTypeText(templateId).map(template -> {
            String resolved = resolveTypeText(template, params, fieldNameMap);
            P paragraph = TableUtil.createLeftAlignedParagraph(resolved, false);
            int index = mainPart.getContent().indexOf(placeholderParagraph);
            mainPart.getContent().add(index + 1, paragraph);
            return paragraph;
        }).orElse(placeholderParagraph); // если текста нет — вставлять после плейсхолдера
    }

    protected String resolveTypeText(String template, Map<String, Object> params, Map<String, String> keyToNameMap) {
        String location = keyToNameMap.entrySet().stream()
                .map(e -> {
                    Object val = params.get(e.getKey());
                    return val != null && !val.toString().isBlank() ? e.getValue() + ": " + val : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));

        Map<String, Object> allParams = new LinkedHashMap<>(params);
        allParams.put("location", location);

        Map<String, String> nameToKey = keyToNameMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (a, b) -> a));

        for (Map.Entry<String, String> entry : nameToKey.entrySet()) {
            Object val = allParams.get(entry.getValue());
            if (val != null) {
                template = template.replace("{{" + entry.getKey() + "}}", val.toString());
            }
        }

        for (Map.Entry<String, Object> entry : allParams.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue().toString());
        }

        return template;
    }

    /**
     * Абстрактный метод для вставки содержимого чек-листа в документ.
     * <p>
     * Теперь принимает LinkedHashMap<String, Object> вместо вложенной структуры.
     */
    protected abstract void insertChecklistContent(WordprocessingMLPackage wordPackage,
                                                   MainDocumentPart mainPart,
                                                   LinkedHashMap<String, Object> data,
                                                   UUID templateId,
                                                   ChecklistInstance checklist,
                                                   P placeholderParagraph,
                                                   Map<String, String> fieldNameMap);

    /**
     * Получить связанный экземпляр чек-листа "Характеристики объекта строительства".
     *
     * @param questionId ID вопроса, для которого нужно получить связанный экземпляр чек-листа.
     * @return Связанный экземпляр чек-листа "Характеристики объекта строительства" или null, если нет связей.
     */
    protected ChecklistInstance getLinkedChecklistInstance(UUID questionId, String name) {
        UUID templateId = checklistTemplateService.getTemplateIdByName(name);
        return checklistInstanceRepository.findByExpertiseQuestionIdAndChecklistTemplateId(questionId, templateId)
                .orElseThrow(() -> new IllegalStateException("Не найден связанный чек-лист для вопроса с ID " + questionId + " и шаблона " + name));
    }

    /**
     * Получить данные связного чек-листа по его ID вопроса и имени шаблона.
     *
     * @param questionId ID вопроса, для которого нужно получить данные связного чек-листа.
     * @param name       Имя шаблона связного чек-листа.
     * @return Данные связного чек-листа в виде строки.
     */
    protected String getLinkedChecklistData(UUID questionId, String name) {
        return getLinkedChecklistInstance(questionId, name).getData();
    }

    /**
     * Получить расширение файла по его имени.
     *
     * @param name Имя файла.
     * @return Расширение файла.
     */
    protected String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        return (dot >= 0 && dot < name.length() - 1) ? name.substring(dot + 1) : "";
    }

    /**
     * Получить значение поля из связанного чек-листа по его имени, ID вопроса и ключу.
     *
     * @param checklistName Имя связанного чек-листа.
     * @param questionId    ID вопроса.
     * @param key           Ключ поля.
     * @return Значение поля или пустая строка, если не удалось получить значение.
     */
    public String getFieldFromChecklist(String checklistName, UUID questionId, String key) {
        try {
            String json = getLinkedChecklistData(questionId, checklistName);
            Map<String, Object> dataMap = objectMapper.readValue(json, Map.class);
            Object nested = dataMap.get(TYPE_TEXT_PARAMS);
            if (nested instanceof Map<?, ?> typeTextParams) {
                return Objects.toString(typeTextParams.get(key), "");
            }
        } catch (Exception e) {
            log.warn("Cannot extract field '{}' from checklist '{}'", key, checklistName, e);
        }
        return "";
    }

}
