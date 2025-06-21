package com.example.expertise.services.expertise.checklists.render.document.realisations;

import com.example.expertise.enums.checklists.ChecklistJsonKey;
import com.example.expertise.integration.minio.MinioIntegration;
import com.example.expertise.model.checklist.ChecklistInstance;
import com.example.expertise.repository.checklist.ChecklistInstanceRepository;
import com.example.expertise.services.cache.ChecklistTemplateCache;
import com.example.expertise.services.expertise.checklists.ChecklistTemplateService;
import com.example.expertise.services.expertise.checklists.render.annotation.RendererFor;
import com.example.expertise.services.expertise.checklists.render.document.AbstractChecklistRenderer;
import com.example.expertise.util.docs.DocumentUtil;
import com.example.expertise.util.docs.FormatConverterUtil;
import com.example.expertise.util.docs.TableUtil;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RendererFor("Дефекты объекта строительства")
public class DefectChecklistRenderer extends AbstractChecklistRenderer {

    private static final Logger log = LoggerFactory.getLogger(DefectChecklistRenderer.class);

    private final MinioIntegration minioIntegration;

    protected DefectChecklistRenderer(ChecklistTemplateCache templateCache,
                                      ChecklistTemplateService checklistTemplateService,
                                      ChecklistInstanceRepository checklistInstanceRepository,
                                      MinioIntegration minioIntegration) {
        super(templateCache, checklistTemplateService, checklistInstanceRepository);
        this.minioIntegration = minioIntegration;
    }

    @Override
    protected void insertChecklistContent(WordprocessingMLPackage wordPackage,
                                          MainDocumentPart mainPart,
                                          LinkedHashMap<String, Object> data,
                                          UUID templateId,
                                          ChecklistInstance checklist,
                                          P placeholderParagraph,
                                          Map<String, String> fieldNameMap) {

        Map<String, Object> typeParams = Optional.ofNullable(data.get(ChecklistJsonKey.TYPE_TEXT_PARAMS.getKey()))
                .filter(Map.class::isInstance)
                .map(obj -> (Map<String, Object>) obj)
                .orElse(Collections.emptyMap());

        P lastInserted = insertTypeText(mainPart, templateId, typeParams, fieldNameMap, placeholderParagraph, checklist);

        Object premisesObj = data.get(ChecklistJsonKey.PREMISES.getKey());
        if (!(premisesObj instanceof List<?> premises)) return;

        int insertIndex = mainPart.getContent().indexOf(lastInserted) + 1;

        List<Map<String, Object>> allPremises = new ArrayList<>();
        List<List<String>> allParameters = new ArrayList<>();
        List<String> allPremiseNames = new ArrayList<>();

        for (Object obj : premises) {
            if (!(obj instanceof Map premise)) continue;

            String premiseName = Objects.toString(premise.get(ChecklistJsonKey.PREMISE_NAME.getKey()), "Помещение");
            List<String> parameters = (List<String>) premise.getOrDefault(ChecklistJsonKey.PREMISE_PARAMETERS.getKey(), List.of());

            // Сохраняем для таблиц дефектов
            allPremises.add(premise);
            allParameters.add(parameters);
            allPremiseNames.add(premiseName);

            // Создаём таблицу
            Tbl table = TableUtil.createDefaultTable();
            TableUtil.fixTwoColumnWidths(table);
            addTitleRow(table, premiseName);

            // Вставка фото и схем помещения до параметров
            insertPremisePhotos(wordPackage, table, premise);
            insertPhotoBlock(wordPackage, table, premise, ChecklistJsonKey.DEFECT_SCHEMA_PDF.getKey(), templateId);

            for (String param : parameters) {
                Map<String, Object> fields = getMap(premise.get(param));
                if (fields == null) continue;

                insertTextRow(templateId, table, param, fields);
                insertPhotoBlock(wordPackage, table, fields, ChecklistJsonKey.DEFECT_PHOTOS.getKey(), templateId); // дефекты на уровне параметра
            }

            mainPart.getContent().add(insertIndex++, table);
            mainPart.getContent().add(insertIndex++, DocumentUtil.getFactory().createP()); // пустая строка
        }

        // Вставка таблиц дефектов
        for (int i = 0; i < allPremises.size(); i++) {
            Map<String, Object> premise = allPremises.get(i);
            List<String> parameters = allParameters.get(i);
            String premiseName = allPremiseNames.get(i);

            P header = DocumentUtil.createFormattedParagraph(premiseName);
            R run = (R) header.getContent().get(0);
            RPr rPr = DocumentUtil.getFactory().createRPr();
            rPr.setB(new BooleanDefaultTrue());
            run.setRPr(rPr);
            mainPart.getContent().add(insertIndex++, header);

            insertDefectsTable(mainPart, templateId, premise, parameters, insertIndex++);
            mainPart.getContent().add(insertIndex++, DocumentUtil.getFactory().createP());
        }

        insertDefectSummaryTables(mainPart, allPremises, allParameters, insertIndex);
    }

    private void insertDefectSummaryTables(MainDocumentPart mainPart,
                                           List<Map<String, Object>> premises,
                                           List<List<String>> allParams,
                                           int insertIndex) {
        for (int i = 0; i < premises.size(); i++) {
            Map<String, Object> premise = premises.get(i);
            List<String> parameters = allParams.get(i);
            String premiseName = Objects.toString(premise.get(ChecklistJsonKey.PREMISE_NAME.getKey()), "Помещение");

            // Создаём таблицу на 1 колонку, 3 строки
            Tbl table = TableUtil.createTable(
                    TableUtil.DEFAULT_TABLE_WIDTH_TWIPS,
                    TableUtil.DEFAULT_WIDTH_TYPE,
                    TableUtil.DEFAULT_BORDER_SIZE,
                    TableUtil.DEFAULT_BORDER_COLOR,
                    TableUtil.DEFAULT_BORDER_STYLE
            );
            TableUtil.fixColumnWidths(table, List.of(TableUtil.DEFAULT_TABLE_WIDTH_TWIPS)); // одна колонка

            // 1. Наименование помещения
            Tr row1 = TableUtil.createTableRow();
            row1.getContent().add(TableUtil.createStyledCell("Наименование помещения: " + premiseName));
            table.getContent().add(row1);

            // 2. Список дефектов (все defect_description из параметров)
            List<String> descriptions = new ArrayList<>();
            for (String param : parameters) {
                Map<String, Object> fields = getMap(premise.get(param));
                if (fields == null) continue;

                String desc = stringifyValue(fields.get(ChecklistJsonKey.DEFECT_DESCRIPTION.getKey()));
                if (!desc.isBlank()) {
                    descriptions.add(desc);
                }
            }
            String joinedDescriptions = String.join(", ", descriptions);


            Tr row2 = TableUtil.createTableRow();
            row2.getContent().add(TableUtil.createStyledCell("Список дефектов:\n" + joinedDescriptions));
            table.getContent().add(row2);

            // 3. Нормативы дефектов (пусто)
            Tr row3 = TableUtil.createTableRow();
            row3.getContent().add(TableUtil.createStyledCell("Нормативная документация дефектов:"));
            table.getContent().add(row3);

            // Вставка таблицы и отступ
            mainPart.getContent().add(insertIndex++, table);
            mainPart.getContent().add(insertIndex++, DocumentUtil.getFactory().createP());
        }
    }


    private void insertDefectsTable(MainDocumentPart mainPart,
                                    UUID templateId,
                                    Map<String, Object> premise,
                                    List<String> parameters,
                                    int insertIndex) {

        Tbl table = TableUtil.createTable(
                TableUtil.DEFAULT_TABLE_WIDTH_TWIPS,
                TableUtil.DEFAULT_WIDTH_TYPE,
                TableUtil.DEFAULT_BORDER_SIZE,
                TableUtil.DEFAULT_BORDER_COLOR,
                TableUtil.DEFAULT_BORDER_STYLE
        );
        TableUtil.fixColumnWidths(table, List.of(3000, 2000, 4000, 2000));

        Tr header = TableUtil.createTableRow();
        List<String> keys = List.of(ChecklistJsonKey.PARAM_LABEL.getKey(), ChecklistJsonKey.DEFECT_VOLUME.getKey(), ChecklistJsonKey.DEFECT_DESCRIPTION.getKey(), ChecklistJsonKey.DEFECT_NOTE.getKey());
        for (String key : keys) {
            String label = ChecklistJsonKey.PARAM_LABEL.getKey().equals(key) ? "Факт" :
                    templateCache.getFieldName(templateId, key).orElse(key);
            Tc cell = TableUtil.createStyledCell(label);
            header.getContent().add(cell);
        }
        table.getContent().add(header);

        for (String param : parameters) {
            Map<String, Object> fields = getMap(premise.get(param));
            if (fields == null) continue;

            Tr row = TableUtil.createTableRow();
            row.getContent().add(TableUtil.createStyledCell(param));
            row.getContent().add(TableUtil.createStyledCell(stringifyValue(fields.get(ChecklistJsonKey.DEFECT_VOLUME.getKey()))));
            row.getContent().add(TableUtil.createStyledCell(stringifyValue(fields.get(ChecklistJsonKey.DEFECT_DESCRIPTION.getKey()))));
            row.getContent().add(TableUtil.createStyledCell(stringifyValue(fields.get(ChecklistJsonKey.DEFECT_NOTE.getKey()))));
            table.getContent().add(row);
        }

        mainPart.getContent().add(insertIndex, table);
    }

    private void insertPremisePhotos(WordprocessingMLPackage wordPackage, Tbl table, Map<String, Object> fields) {
        List<?> files = getList(fields.get(ChecklistJsonKey.PREMISE_PHOTOS.getKey()));
        if (files != null) insertFileListAsRows(wordPackage, table, files, ChecklistJsonKey.PREMISE_PHOTOS.getKey());
    }

    private void insertTextRow(UUID templateId, Tbl table, String paramName, Map<String, Object> fields) {
        Tr row = TableUtil.createTableRow();
        Tc cell = TableUtil.createEmptyCell();

        // Ячейка на 2 колонки
        TcPr tcPr = cell.getTcPr();
        TcPrInner.GridSpan gridSpan = DocumentUtil.getFactory().createTcPrInnerGridSpan();
        gridSpan.setVal(BigInteger.valueOf(2));
        tcPr.setGridSpan(gridSpan);

        // Собираем текст
        StringBuilder sb = new StringBuilder();
        sb.append("\nПараметр: ").append(paramName).append("\n");

        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Пропустить списки map (например, массив файлов)
            if (value instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map) continue;

            String label = templateCache.getFieldName(templateId, key).orElse(key);

            if (value instanceof Map<?, ?> nested) {
                // Определяем значение "value" для основной строки
                Object mainVal = nested.get("value");
                String baseLabel = label + (mainVal != null ? ": " + mainVal : ":");

                sb.append(baseLabel).append("\n");

                // Остальные ключи добавим как подстроки
                nested.forEach((subKeyObj, subVal) -> {
                    String subKey = subKeyObj.toString();
                    if (ChecklistJsonKey.VALUE.getKey().equals(subKey)) return; // уже отображено
                    String subLabel = templateCache.getFieldName(templateId, subKey).orElse(subKey);
                    sb.append("  - ").append(subLabel).append(": ").append(Objects.toString(subVal, "")).append("\n");
                });
            } else if (value instanceof List<?>) {
                // Массив значений
                List<?> list = (List<?>) value;
                if (!list.isEmpty()) {
                    sb.append(label).append(":\n");
                    for (Object item : list) {
                        sb.append("  - ").append(item).append("\n");
                    }
                }
            } else {
                if (!Objects.toString(value, "").isBlank()) {
                    sb.append(label).append(": ").append(value).append("\n");
                }
            }

        }

        // Создание параграфа
        P paragraph = DocumentUtil.getFactory().createP();
        paragraph.setPPr(DocumentUtil.getFactory().createPPr());
        paragraph.getPPr().setJc(DocumentUtil.getFactory().createJc());
        paragraph.getPPr().getJc().setVal(JcEnumeration.LEFT);

        // Добавим жирную красную строку
        R warnRun = DocumentUtil.getFactory().createR();
        Text warnText = DocumentUtil.getFactory().createText();
        warnText.setValue("Необходимо назвать помещение!");
        warnText.setSpace("preserve");

        RPr warnStyle = DocumentUtil.getFactory().createRPr();
        BooleanDefaultTrue bold = new BooleanDefaultTrue();
        bold.setVal(true);
        warnStyle.setB(bold);

        Color color = new Color();
        color.setVal("FF0000");
        warnStyle.setColor(color);

        warnRun.setRPr(warnStyle);
        warnRun.getContent().add(warnText);
        paragraph.getContent().add(warnRun);

        // Перенос строки и основной текст
        paragraph.getContent().add(DocumentUtil.getFactory().createBr());
        paragraph.getContent().add(DocumentUtil.createMultilineRun(sb.toString()));

        cell.getContent().set(0, paragraph);
        row.getContent().add(cell);
        table.getContent().add(row);
    }

    private void insertPhotoBlock(WordprocessingMLPackage wordPackage,
                                  Tbl table,
                                  Map<String, Object> fields,
                                  String bucketKey,
                                  UUID templateId) {
        List<?> files = getList(fields.get(bucketKey));
        if (files == null || files.isEmpty()) return;

        String label = templateCache.getFieldName(templateId, bucketKey).orElse(bucketKey);
        Tr labelRow = TableUtil.createTableRow();
        Tc labelCell = TableUtil.createEmptyCell();

        TcPr tcPr = labelCell.getTcPr();
        TcPrInner.GridSpan gridSpan = DocumentUtil.getFactory().createTcPrInnerGridSpan();
        gridSpan.setVal(BigInteger.valueOf(2));
        tcPr.setGridSpan(gridSpan);

        addTextParagraph(labelCell, new StringBuilder(label + ":"));
        labelRow.getContent().add(labelCell);
        table.getContent().add(labelRow);

        insertFileListAsRows(wordPackage, table, files, bucketKey);
    }

    private void insertFileListAsRows(WordprocessingMLPackage wordPackage, Tbl table, List<?> fileList, String bucket) {
        try {
            List<byte[]> allImages = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            int counter = 1;

            for (Object fileObj : fileList) {
                if (!(fileObj instanceof Map<?, ?> map)) continue;

                String name = Objects.toString(map.get(ChecklistJsonKey.NAME.getKey()), "").trim();
                if (name.isBlank()) continue;

                String ext = getExtension(name);
                String baseName = name.substring(0, name.length() - ext.length() - 1);
                byte[] bytes = minioIntegration.getFileByParams(baseName, "." + ext, bucket);

                List<byte[]> images = ChecklistJsonKey.DEFECT_SCHEMA_PDF.getKey().equals(bucket)
                        ? FormatConverterUtil.convertPdfToImages(bytes)
                        : List.of(bytes);

                for (byte[] img : images) {
                    allImages.add(img);
                    String prefix = ChecklistJsonKey.DEFECT_SCHEMA_PDF.getKey().equals(bucket) ? "Рисунок" : "Фото";
                    labels.add(prefix + " " + counter++);
                }
            }

            for (int i = 0; i < allImages.size(); i += 2) {
                Tr row = TableUtil.createTableRow();

                // Первая картинка + подпись
                Tc cell1 = TableUtil.createImageCell(wordPackage, allImages.get(i), TableUtil.DEFAULT_IMAGE_WIDTH_TWIPS, null);
                DocumentUtil.addImageLabel(cell1, labels.get(i));
                row.getContent().add(cell1);

                // Вторая картинка + подпись
                if (i + 1 < allImages.size()) {
                    Tc cell2 = TableUtil.createImageCell(wordPackage, allImages.get(i + 1), TableUtil.DEFAULT_IMAGE_WIDTH_TWIPS, null);
                    DocumentUtil.addImageLabel(cell2, labels.get(i + 1));
                    row.getContent().add(cell2);
                } else {
                    row.getContent().add(TableUtil.createEmptyCell());
                }

                table.getContent().add(row);
            }
        } catch (Exception e) {
            log.warn("Ошибка при вставке изображений из bucket '{}': {}", bucket, e.getMessage());
        }
    }

    private void addTitleRow(Tbl table, String title) {
        Tr row = TableUtil.createTableRow();
        Tc cell = TableUtil.createEmptyCell();

        TcPr tcPr = cell.getTcPr();
        TcPrInner.GridSpan gridSpan = DocumentUtil.getFactory().createTcPrInnerGridSpan();
        gridSpan.setVal(BigInteger.valueOf(2));
        tcPr.setGridSpan(gridSpan);

        P paragraph = DocumentUtil.createFormattedParagraph(title);
        ((R) paragraph.getContent().get(0)).setRPr(bold());
        cell.getContent().set(0, paragraph);
        row.getContent().add(cell);
        table.getContent().add(row);
    }

    private void addTextParagraph(Tc cell, StringBuilder text) {
        P paragraph = DocumentUtil.getFactory().createP();
        paragraph.getContent().add(DocumentUtil.createMultilineRun(text.toString()));
        PPr pPr = DocumentUtil.getFactory().createPPr();
        Jc jc = DocumentUtil.getFactory().createJc();
        jc.setVal(JcEnumeration.LEFT);
        pPr.setJc(jc);
        paragraph.setPPr(pPr);
        cell.getContent().add(paragraph);
    }

    private RPr bold() {
        RPr rpr = new RPr();
        rpr.setB(new BooleanDefaultTrue());
        return rpr;
    }

    private String stringifyValue(Object value) {
        if (value instanceof List<?> list)
            return list.stream().map(Object::toString).filter(s -> !s.isBlank()).distinct().collect(Collectors.joining(",\n"));
        return Objects.toString(value, "").trim();
    }

    private Map<String, Object> getMap(Object obj) {
        return (obj instanceof Map<?, ?> map) ? (Map<String, Object>) map : null;
    }

    private List<?> getList(Object obj) {
        return (obj instanceof List<?>) ? (List<?>) obj : null;
    }
}
