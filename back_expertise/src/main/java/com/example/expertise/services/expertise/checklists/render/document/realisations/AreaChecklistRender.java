package com.example.expertise.services.expertise.checklists.render.document.realisations;

import com.example.expertise.enums.checklists.ChecklistJsonKey;
import com.example.expertise.model.checklist.ChecklistInstance;
import com.example.expertise.repository.checklist.ChecklistInstanceRepository;
import com.example.expertise.services.cache.ChecklistTemplateCache;
import com.example.expertise.services.expertise.checklists.ChecklistTemplateService;
import com.example.expertise.services.expertise.checklists.render.annotation.RendererFor;
import com.example.expertise.services.expertise.checklists.render.document.AbstractChecklistRenderer;
import com.example.expertise.util.docs.DocumentUtil;
import com.example.expertise.util.docs.TableUtil;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RendererFor("Площадь объекта")
public class AreaChecklistRender extends AbstractChecklistRenderer {

    private static final Logger log = LoggerFactory.getLogger(AreaChecklistRender.class);

    private static final String PLACEHOLDER_LOCATION = "location";

    public AreaChecklistRender(ChecklistTemplateCache templateCache, ChecklistTemplateService checklistTemplateService, ChecklistInstanceRepository checklistInstanceRepository) {
        super(templateCache, checklistTemplateService, checklistInstanceRepository);
    }

    @Override
    protected void insertChecklistContent(WordprocessingMLPackage wordPackage,
                                          MainDocumentPart mainPart,
                                          LinkedHashMap<String, Object> data,
                                          UUID templateId,
                                          ChecklistInstance checklist,
                                          P placeholderParagraph,
                                          Map<String, String> fieldNameMap) {

        // Извлекаем параметры type_text_params
        Map<String, Object> typeParams = Optional.ofNullable(data.get("type_text_params"))
                .filter(Map.class::isInstance)
                .map(obj -> (Map<String, Object>) obj)
                .orElse(Collections.emptyMap());

        P lastParagraph = insertTypeText(mainPart, templateId, typeParams, fieldNameMap, placeholderParagraph, checklist);

        // Вставка пустой строки после типового текста
        P emptyLineAfterTypeText = DocumentUtil.createFormattedParagraph("");
        int indexAfterType = mainPart.getContent().indexOf(lastParagraph);
        mainPart.getContent().add(indexAfterType + 1, emptyLineAfterTypeText);

        // Формируем и вставляем второй текст
        String area = Objects.toString(typeParams.get(ChecklistJsonKey.AREA.getKey()), "");
        String coordinates = Objects.toString(typeParams.get(ChecklistJsonKey.COORDINATES.getKey()), "");

        String coordinatePoints = formatPoints(coordinates);

        String resultText = String.format(
                "В результате проведения измерений, были установлены границы исследуемого объекта недвижимости – площадью %s, " +
                        "на дату проведения экспертного осмотра в точках %s, согласно следующего каталога координат",
                area,
                coordinatePoints
        );

        P resultParagraph = TableUtil.createLeftAlignedParagraph(resultText, false);
        mainPart.getContent().add(indexAfterType + 2, resultParagraph);

        // Абзац с красным текстом
        P redParagraph = DocumentUtil.createColoredParagraph("Необходимо вставить таблицу точек координат", "FF0000");
        mainPart.getContent().add(indexAfterType + 3, redParagraph);

        // Финальный отступ
        P emptyLineAfterResult = DocumentUtil.createFormattedParagraph("");
        mainPart.getContent().add(indexAfterType + 4, emptyLineAfterResult);
    }

    private String formatPoints(String coordinateCount) {
        try {
            int count = Integer.parseInt(coordinateCount.trim());
            if (count <= 0) return "(не указано)";
            return IntStream.rangeClosed(1, count)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.joining("-"));
        } catch (NumberFormatException e) {
            return "(неверный формат координат)";
        }
    }

    @Override
    protected P insertTypeText(MainDocumentPart mainPart,
                               UUID templateId,
                               Map<String, Object> params,
                               Map<String, String> fieldNameMap,
                               P placeholderParagraph,
                               ChecklistInstance checklist) {

        Optional<String> typeTextOpt = templateCache.getTypeText(templateId);
        if (typeTextOpt.isEmpty()) return placeholderParagraph;

        // Собираем итоговые параметры для подстановки
        Map<String, Object> mergedParams = buildMergedParams(params, checklist);

        // Подставляем значения в шаблон
        String resolved = applyPlaceholders(typeTextOpt.get(), mergedParams);

        // Вставляем абзац в документ
        P paragraph = TableUtil.createLeftAlignedParagraph(resolved, false);
        int index = mainPart.getContent().indexOf(placeholderParagraph);
        mainPart.getContent().add(index + 1, paragraph);
        return paragraph;
    }

    private Map<String, Object> buildMergedParams(Map<String, Object> currentParams, ChecklistInstance checklist) {
        Map<String, Object> merged = new LinkedHashMap<>();

        // Параметры из дополнительного чек-листа
        Map<String, Object> sourceParams = getSourceParamsFromAnotherChecklist(checklist);

        merged.putAll(sourceParams);

        // Формируем составной location
        String address = Objects.toString(sourceParams.get(ChecklistJsonKey.ADDRESS.getKey()), "").trim();
        String cadastral = Objects.toString(sourceParams.get(ChecklistJsonKey.CADASTRE_IMAGES.getKey()), "").trim();

        if (!address.isEmpty() || !cadastral.isEmpty()) {
            String location = String.format("%s%s",
                    !address.isEmpty() ? " по адресу " + address : "",
                    !cadastral.isEmpty() ? ", кадастровый номер " + cadastral : "");
            merged.put(PLACEHOLDER_LOCATION, location);
        }

        // Перезаписываем параметрами текущего чек-листа
        merged.putAll(currentParams);
        return merged;
    }

    /**
     * Извлекаем параметры type_text_params из дополнительного чек-листа (характеристики объекта строения)
     *
     * @param checklist текущий чек-лист
     * @return Параметры из дополнительного чек-листа (в виде мапы) или пустая мапа, если чек-лист не найден
     */
    private Map<String, Object> getSourceParamsFromAnotherChecklist(ChecklistInstance checklist) {
        ChecklistInstance sourceChecklist = getLinkedChecklistInstance(checklist.getExpertiseQuestion().getId(), "Характеристики объекта строительства");

        Map<String, Object> parsed = Optional.ofNullable(parseChecklistData(sourceChecklist.getData()).get("type_text_params"))
                .filter(Map.class::isInstance)
                .map(obj -> (Map<String, Object>) obj)
                .orElse(Collections.emptyMap());

        log.info("type_text_params из дополнительного чек-листа: {}", parsed);
        return parsed;
    }

    private String applyPlaceholders(String template, Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = Objects.toString(entry.getValue(), "").trim();

            if (!value.isEmpty()) {
                template = template.replace(placeholder, value);
                log.info("Подставлено значение для {}: {}", placeholder, value);
            } else {
                log.info("Значение для {} пустое или null — пропущено", placeholder);
            }
        }
        return template;
    }


}
