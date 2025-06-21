package com.example.expertise.services.expertise.checklists.render.document.realisations;

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
import org.docx4j.wml.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Класс DefaultChecklistRenderer используется для рендеринга чек-листов по умолчанию.
 */
@Component
@RendererFor("Характеристики объекта строительства")
public class DefaultChecklistRenderer extends AbstractChecklistRenderer {

    protected DefaultChecklistRenderer(ChecklistTemplateCache templateCache, ChecklistTemplateService checklistTemplateService, ChecklistInstanceRepository checklistInstanceRepository) {
        super(templateCache, checklistTemplateService, checklistInstanceRepository);
    }

    /**
     * Вставка содержимого чек-листа в документ.
     *
     * @param wordPackage        пакет WordprocessingMLPackage
     * @param mainPart           основная часть документа
     * @param data               данные чек-листа
     * @param templateId         идентификатор шаблона
     * @param checklist          объект ChecklistInstance
     * @param placeholderParagraph абзац-заполнитель
     */
    @Override
    protected void insertChecklistContent(WordprocessingMLPackage wordPackage,
                                          MainDocumentPart mainPart,
                                          LinkedHashMap<String, Object> data,
                                          UUID templateId,
                                          ChecklistInstance checklist,
                                          P placeholderParagraph,
                                          Map<String, String> fieldNameMap) {

        Map<String, Object> typeParams = Optional.ofNullable(data.get("type_text_params"))
                .filter(Map.class::isInstance)
                .map(obj -> (Map<String, Object>) obj)
                .orElse(Collections.emptyMap());

        P lastInserted = insertTypeText(mainPart, templateId, typeParams, fieldNameMap, placeholderParagraph, checklist);

        Tbl table = TableUtil.createTable(
                TableUtil.DEFAULT_TABLE_WIDTH_TWIPS,
                TableUtil.DEFAULT_WIDTH_TYPE,
                TableUtil.DEFAULT_BORDER_SIZE,
                TableUtil.DEFAULT_BORDER_COLOR,
                TableUtil.DEFAULT_BORDER_STYLE
        );

        // Заголовок таблицы
        Tr titleRow = TableUtil.createTableRow();
        Tc titleCell = TableUtil.createEmptyCell();
        P titleParagraph = DocumentUtil.createFormattedParagraph(checklist.getChecklistTemplate().getName());
        RPr bold = new RPr();
        bold.setB(new BooleanDefaultTrue());
        ((R) titleParagraph.getContent().get(0)).setRPr(bold);
        titleCell.getContent().set(0, titleParagraph);
        titleRow.getContent().add(titleCell);
        table.getContent().add(titleRow);

        // Обработка параметров
        data.forEach((paramName, rawFields) -> {
            if ("type_text_params".equals(paramName) || !(rawFields instanceof Map<?, ?> fieldsRaw)) return;

            @SuppressWarnings("unchecked")
            Map<String, Object> fields = (Map<String, Object>) fieldsRaw;

            StringBuilder content = new StringBuilder("Параметр: ").append(paramName).append("\n");

            fields.forEach((key, value) -> {
                String subTitle = templateCache.getFieldName(templateId, key).orElse(key);

                // Обработка списка ГОСТов
                if ("gosts".equalsIgnoreCase(key) && value instanceof List<?> list) {
                    String gosts = list.stream().map(Object::toString).distinct().collect(Collectors.joining(",\n"));
                    content.append(subTitle).append(":\n").append(gosts).append("\n");
                    return;
                }

                // Обработка вложенных specs (Map с value и характеристиками)
                if (value instanceof Map<?, ?> map && map.containsKey("value")) {
                    // Основное значение
                    content.append(subTitle).append(": ").append(Objects.toString(map.get("value"), "")).append("\n");

                    // Обработка вложенных характеристик (все ключи кроме value)
                    map.forEach((specKey, specValue) -> {
                        if ("value".equals(specKey)) return;
                        String specTitle = templateCache.getFieldName(templateId, specKey.toString()).orElse(specKey.toString());
                        content.append("  • ").append(specTitle).append(": ").append(Objects.toString(specValue, "")).append("\n");
                    });
                    return;
                }

                // Обработка списков
                if (value instanceof List<?> list) {
                    String listStr = list.stream().map(Object::toString).distinct().collect(Collectors.joining(",\n"));
                    content.append(subTitle).append(": ").append(listStr).append("\n");
                    return;
                }

                // Обычное поле
                content.append(subTitle).append(": ").append(Objects.toString(value, "")).append("\n");
            });


            Tr row = TableUtil.createTableRow();
            Tc cell = TableUtil.createEmptyCell();

            P paragraph = DocumentUtil.getFactory().createP();
            paragraph.getContent().add(DocumentUtil.createMultilineRun(content.toString()));

            PPr pPr = DocumentUtil.getFactory().createPPr();
            Jc jc = DocumentUtil.getFactory().createJc();
            jc.setVal(JcEnumeration.LEFT);
            pPr.setJc(jc);
            paragraph.setPPr(pPr);

            cell.getContent().set(0, paragraph);
            row.getContent().add(cell);
            table.getContent().add(row);
        });

        int index = mainPart.getContent().indexOf(placeholderParagraph);
        mainPart.getContent().add(index + 2, table);
    }

}
