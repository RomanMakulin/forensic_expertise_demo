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
import com.example.expertise.util.docs.TableUtil;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RendererFor("Определение соответствия земельного участка")
public class DeterminationComplianceChecklistRender extends AbstractChecklistRenderer {

    private static final Logger log = LoggerFactory.getLogger(DeterminationComplianceChecklistRender.class);

    private static final String TITLE_TEXT = "Исследование в целях определения соответствия целевому назначению земельного участка";

    private static final String TEXT_INTERSECTION_NOTE = """
        Примечание:
        План установления местоположения границ исследуемого объекта относительно границ зон с особыми условиями см. в приложении к настоящему Заключению экспертов.
        """;

    private final MinioIntegration minioIntegration;

    protected DeterminationComplianceChecklistRender(ChecklistTemplateCache templateCache,
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

        Map<String, Object> typeParams = getMap(data.get(ChecklistJsonKey.TYPE_TEXT_PARAMS.getKey()));
        int insertIndex = mainPart.getContent().indexOf(placeholderParagraph);


        Tbl table = TableUtil.createDefaultTable();
        TableUtil.fixColumnWidths(table, List.of(TableUtil.DEFAULT_TABLE_WIDTH_TWIPS));
        DocumentUtil.addTextRow(table, TITLE_TEXT);

        renderAllBlocks(wordPackage, data, table, typeParams, checklist);

        mainPart.getContent().add(insertIndex, table);
    }

    /**
     * Рендеринг всех блоков для вставки в таблицу.
     */
    private void renderAllBlocks(WordprocessingMLPackage wordPackage, LinkedHashMap<String, Object> data, Tbl table, Map<String, Object> typeParams, ChecklistInstance checklist){
        // Получение связанных данных
        String address = defaultIfBlank(getFieldFromChecklist("Характеристики объекта строительства", checklist.getExpertiseQuestion().getId(), "address"));
        String cadastralNumber = defaultIfBlank(getFieldFromChecklist("Характеристики объекта строительства", checklist.getExpertiseQuestion().getId(), "cadastral_number"));
        String area = defaultIfBlank(getFieldFromChecklist("Площадь объекта", checklist.getExpertiseQuestion().getId(), "area"));

        String category = defaultIfBlank(typeParams.get(ChecklistJsonKey.CATEGORY.getKey()));
        String permittedUse = defaultIfBlank(typeParams.get(ChecklistJsonKey.PERMITTED_USE.getKey()));
        String vriCodes = defaultIfBlank(typeParams.get(ChecklistJsonKey.VRI_CODES.getKey()));
        String district = defaultIfBlank(typeParams.get(ChecklistJsonKey.DISTRICT.getKey()));
        String zoneText = defaultIfBlank(typeParams.get(ChecklistJsonKey.DISTINCT_MAP_TEXT.getKey()));

        // Блок 1
        addTextWithImagesBlockRow(wordPackage, table,
                generateBlock1Text(cadastralNumber, address, area, category, permittedUse),
                getList(data.get(ChecklistJsonKey.CADASTRE_IMAGES.getKey())), ChecklistJsonKey.CADASTRE_IMAGES.getKey(), false, null);

        // Блок 2
        String years = extractYears(getList(data.get(ChecklistJsonKey.OPEN_SOURCE_IMAGES.getKey())));
        addTextWithImagesBlockRow(wordPackage, table,
                generateBlock2Text(years),
                getList(data.get(ChecklistJsonKey.OPEN_SOURCE_IMAGES.getKey())), ChecklistJsonKey.OPEN_SOURCE_IMAGES.getKey(), true, null);

        // Блок 3
        addTextWithImagesBlockRow(wordPackage, table,
                generateBlock3Text(cadastralNumber, district, zoneText),
                getList(data.get(ChecklistJsonKey.DISTRICT_MAP_IMAGES.getKey())), ChecklistJsonKey.DISTRICT_MAP_IMAGES.getKey(), false, null);

        // Блок 4 — пересечения
        String intersectionsText = buildIntersectionsText(getList(data.get(ChecklistJsonKey.INTERSECTION.getKey())));
        addTextWithImagesBlockRow(wordPackage, table, intersectionsText,
                getList(data.get(ChecklistJsonKey.PZZ_SCREENSHOTS.getKey())), ChecklistJsonKey.PZZ_SCREENSHOTS.getKey(), false, null);

        // Финальный блок
        DocumentUtil.addTextRow(table,
                generateFinalBlockText(cadastralNumber, address, area, category, permittedUse, vriCodes));
    }

    private String generateBlock1Text(String cadastralNumber, String address, String area, String category, String permittedUse) {
        return String.format(
                "1. Согласно материалам отраженным в Публичной кадастровой карты портала Росреестр https://pkk.rosreestr.ru/ на земельный участок с кадастровым №%s, по адресу: %s, площадью %s., категория земель «%s» вид разрешенного использования «%s»",
                cadastralNumber, address, area, category, permittedUse);
    }

    private String generateBlock2Text(String years) {
        return String.format(
                "2. Исследование снимков исследуемого земельного участка, отраженных в открытом доступе в интернет-ресурсах по состоянию на (%s) годы, позволяющие отследить периоды строительства и эксплуатации исследуемого сооружения см. Рисунки.",
                years);
    }

    private String generateBlock3Text(String cadastralNumber, String district, String zoneText) {
        return String.format(
                "3. В результате исследований установлено, что земельный участок с кадастровым номером %s, согласно градостроительному зонированию утвержденных Правил Землепользования и Застройки (%s) находится в территориальной зоне %s",
                cadastralNumber, district, zoneText);
    }

    private String generateFinalBlockText(String cadastralNumber, String address, String area,
                                          String category, String permittedUse, String vriCodes) {
        return String.format("""
                Фактически Сооружение – (Наименование сооружения, будет тянуться из другого чек-листа - хардкод) расположенная на земельном участке с кадастровым номером %s по адресу: %s используется по числовому коду ВРИ %s тогда как согласно Публичной кадастровой карты портала Росреестр https://pkk.rosreestr.ru/ на земельный участок с кадастровым № %s, по адресу: %s площадью %s., категория земель «%s», вид разрешенного использования «%s», что соответствует коду ВРИ %s, размещение которых предусмотрено содержанием видов разрешенного использования с кодами %s.""",
                cadastralNumber, address, vriCodes, cadastralNumber, address, area, category, permittedUse, permittedUse, vriCodes);
    }

    private String extractYears(List<?> sources) {
        StringBuilder sb = new StringBuilder();
        for (Object o : sources) {
            if (o instanceof Map<?, ?> map) {
                Object desc = map.get("description");
                if (desc != null) sb.append(desc).append(", ");
            }
        }
        return sb.length() > 2 ? sb.substring(0, sb.length() - 2) : "Нет данных";
    }

    private String buildIntersectionsText(List<?> intersections) {
        StringBuilder sb = new StringBuilder("Пересечения с зонами особого использования территории:\n");
        for (Object o : intersections) {
            if (o instanceof Map<?, ?> map) {
                String name = defaultIfBlank(map.get("name"));
                String value = defaultIfBlank(map.get("value"));
                sb.append("- ").append(name).append(" Процент пересечения: ").append(value).append("\n");
            }
        }
        sb.append(TEXT_INTERSECTION_NOTE);
        return sb.toString();
    }

    private String defaultIfBlank(Object value) {
        String str = Objects.toString(value, "").trim();
        return str.isEmpty() ? "Нет значения" : str;
    }

    private void addTextWithImagesBlockRow(WordprocessingMLPackage wordPackage,
                                           Tbl table,
                                           String text,
                                           List<?> files,
                                           String bucket,
                                           boolean useDescriptions,
                                           String extraTextAfterImages) {
        Tr row = TableUtil.createTableRow();
        Tc cell = TableUtil.createEmptyCell();
        cell.getContent().add(DocumentUtil.createMultilineParagraph(text));

        if (files != null && !files.isEmpty()) {
            int counter = 1;
            for (Object o : files) {
                Map<?, ?> map = (Map<?, ?>) o;
                Map<?, ?> fileMap = ChecklistJsonKey.OPEN_SOURCE_IMAGES.getKey().equals(bucket) ? getMap(map.get("file")) : map;
                if (fileMap == null) continue;

                String name = defaultIfBlank(fileMap.get("name"));
                if ("Нет значения".equals(name)) continue;

                String ext = getExtension(name);
                String baseName = name.substring(0, name.length() - ext.length() - 1);
                try {
                    byte[] content = minioIntegration.getFileByParams(baseName, "." + ext, bucket);
                    P imageParagraph = DocumentUtil.createImageParagraph(wordPackage, content, null, false);
                    cell.getContent().add(imageParagraph);

                    String caption = "Рисунок " + counter++;
                    if (useDescriptions) {
                        caption += ". Иллюстрация с интернет-ресурса - " + defaultIfBlank(map.get("description"));
                    } else if (ChecklistJsonKey.CADASTRE_IMAGES.getKey().equals(bucket)) {
                        caption += " - иллюстрация с https://pkk.rosreestr.ru/";
                    }

                    cell.getContent().add(DocumentUtil.createCenteredParagraph(caption));
                } catch (Exception e) {
                    log.info("Error while adding image to checklist table", e);
                }
            }
        }

        if (extraTextAfterImages != null) {
            cell.getContent().add(DocumentUtil.createMultilineParagraph(extraTextAfterImages));
        }

        row.getContent().add(cell);
        table.getContent().add(row);
    }

    private Map<String, Object> getMap(Object obj) {
        return (obj instanceof Map<?, ?> map) ? (Map<String, Object>) map : Collections.emptyMap();
    }

    private List<?> getList(Object obj) {
        return (obj instanceof List<?>) ? (List<?>) obj : Collections.emptyList();
    }
}
