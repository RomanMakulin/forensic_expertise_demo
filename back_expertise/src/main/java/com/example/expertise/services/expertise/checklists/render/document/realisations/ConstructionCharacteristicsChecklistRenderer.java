package com.example.expertise.services.expertise.checklists.render.document.realisations;

import com.example.expertise.enums.checklists.ChecklistJsonKey;
import com.example.expertise.enums.checklists.ResourcesPaths;
import com.example.expertise.integration.gigachat.GigaChatIntegration;
import com.example.expertise.model.checklist.ChecklistInstance;
import com.example.expertise.repository.checklist.ChecklistInstanceRepository;
import com.example.expertise.services.cache.ChecklistTemplateCache;
import com.example.expertise.services.expertise.checklists.ChecklistTemplateService;
import com.example.expertise.services.expertise.checklists.render.annotation.RendererFor;
import com.example.expertise.services.expertise.checklists.render.document.AbstractChecklistRenderer;
import com.example.expertise.util.SSLUtils;
import com.example.expertise.util.docs.DocumentUtil;
import com.example.expertise.util.docs.TableUtil;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Реализация рендерера для чек-листа "Определение формата строения".
 */
@Component
@RendererFor("Определение формата строения")
public class ConstructionCharacteristicsChecklistRenderer extends AbstractChecklistRenderer {

    private static final Logger log = LoggerFactory.getLogger(ConstructionCharacteristicsChecklistRenderer.class);

    private static final String BLOCK_NAME_1 = "Несущие и ограждающие конструкции объекта, расположенные выше относительной отметки «0»";
    private static final String BLOCK_NAME_2 = "Конструкция фундаментов и оснований";
    private static final String BLOCK_NAME_3 = "Система инженерно-технического обеспечения";
    private static final String BLOCK_NAME_4 = "Период эксплуатации";
    private static final String BLOCK_NAME_5 = "Разрешительная документация";

    private final GigaChatIntegration gigaChatIntegration;

    public ConstructionCharacteristicsChecklistRenderer(ChecklistTemplateCache templateCache,
                                                        ChecklistTemplateService checklistTemplateService,
                                                        ChecklistInstanceRepository checklistInstanceRepository,
                                                        GigaChatIntegration gigaChatIntegration) {
        super(templateCache, checklistTemplateService, checklistInstanceRepository);
        this.gigaChatIntegration = gigaChatIntegration;
    }

    @Override
    protected void insertChecklistContent(WordprocessingMLPackage wordPackage,
                                          MainDocumentPart mainPart,
                                          LinkedHashMap<String, Object> data,
                                          UUID templateId,
                                          ChecklistInstance checklist,
                                          P placeholderParagraph,
                                          Map<String, String> fieldNameMap) {

        checklistInstanceThreadLocal.set(checklist); // Установка текущего чек-листа
        try {
            int insertIndex = mainPart.getContent().indexOf(placeholderParagraph);
            int tableInsertIndex = insertPreviewTypeText(mainPart, insertIndex); // теперь возвращает новый индекс

            Tbl table = generateTable(data);
            mainPart.getContent().add(tableInsertIndex++, table); // вставляем таблицу в документ

            insertOtherFields(data, mainPart, tableInsertIndex); // вставка примечания и финального текста
        } catch (Exception e) {
            log.error("Ошибка при формировании чек-листа", e);
            throw new RuntimeException("Ошибка при формировании чек-листа", e);
        } finally {
            checklistInstanceThreadLocal.remove();
        }
    }

    /**
     * Вставка дополнительного текста после таблицы чек-листа.
     * Примечание и финальный текст.
     *
     * @param data             - параметры чек-листа
     * @param mainPart         - основная часть документа
     * @param tableInsertIndex - индекс вставки таблицы
     */
    private void insertOtherFields(LinkedHashMap<String, Object> data, MainDocumentPart mainPart, int tableInsertIndex) {
        // Вставка примечания
        Object noteObj = data.get("global_note");
        if (noteObj instanceof String globalNote && !globalNote.isBlank()) {
            String noteText = "Примечание: " + globalNote;
            mainPart.getContent().add(tableInsertIndex++, DocumentUtil.createParagraph(noteText));
            mainPart.getContent().add(tableInsertIndex++, DocumentUtil.createEmptyParagraphWithSpacing());
        }

        // Финальный текст
        String conclusionText = "НАПИШИТЕ ВЫВОД ПО ПРОВЕДЕННОМУ ИССЛЕДОВАНИЮ КАПИТАЛЬНОСТИ СТРОЕНИЯ";
        mainPart.getContent().add(tableInsertIndex, DocumentUtil.createCenteredParagraph(conclusionText));
    }

    /**
     * Генерация таблицы с параметрами чек-листа
     *
     * @param data - параметры чек-листа
     * @return сформированная таблица с параметрами чек-листа
     */
    private Tbl generateTable(LinkedHashMap<String, Object> data) {
        Tbl table = TableUtil.createDefaultTable();
        TableUtil.fixTwoColumnWidths(table);
        DocumentUtil.addTwoCellRow(table, BLOCK_NAME_1, buildBlockText(data, getTextFromFileByPath(ResourcesPaths.BLOCK_1.getPath()), BLOCK_NAME_1));
        DocumentUtil.addTwoCellRow(table, BLOCK_NAME_2, buildBlockText(data, getTextFromFileByPath(ResourcesPaths.BLOCK_2.getPath()), BLOCK_NAME_2));
        DocumentUtil.addTwoCellRow(table, BLOCK_NAME_3, buildBlockText(data, getTextFromFileByPath(ResourcesPaths.BLOCK_3.getPath()), BLOCK_NAME_3));
        DocumentUtil.addTwoCellRow(table, BLOCK_NAME_4, buildBlockText(data, getTextFromFileByPath(ResourcesPaths.BLOCK_4.getPath()), BLOCK_NAME_4));
        DocumentUtil.addTwoCellRow(table, BLOCK_NAME_5, buildBlockText(data, getTextFromFileByPath(ResourcesPaths.BLOCK_5.getPath()), BLOCK_NAME_5));
        return table;
    }

    /**
     * Вставка типового текста перед таблицей с чек-листом
     *
     * @param mainPart    - основная часть документа
     * @param insertIndex - индекс для вставки
     * @return обновлённый индекс после вставки абзацев и отступа
     */
    private int insertPreviewTypeText(MainDocumentPart mainPart, int insertIndex) {
        P preSpacer = DocumentUtil.createEmptyParagraphWithSpacing();
        mainPart.getContent().add(insertIndex++, preSpacer);

        String introText = getTextFromFileByPath(ResourcesPaths.INTRO.getPath());
        List<P> introParagraphs = DocumentUtil.createParagraphsFromText(introText); // Преобразуем в параграфы

        // Вставляем параграфы перед таблицей
        for (P paragraph : introParagraphs) {
            mainPart.getContent().add(insertIndex++, paragraph);
        }
        return insertIndex;
    }

    /**
     * Формирование блока данных по ключу параметра
     *
     * @param data      - json с данными чек-листа
     * @param typeText  - текст для подстановки в запрос
     * @param blockName - название блока данных (парамерт чек-листа)
     * @return сформированный текст для конкретного блока данных (параметра чек-листа)
     */
    private String buildBlockText(Map<String, Object> data, String typeText, String blockName) {
        Map<String, Object> block = getMap(data.get(blockName));

        // Добавляем кадастр и адрес только для нужных блоков
        if (BLOCK_NAME_2.equals(blockName) || BLOCK_NAME_5.equals(blockName)) {
            UUID questionId = checklistInstanceThreadLocal.get().getExpertiseQuestion().getId();
            String cadastral = getFieldFromChecklist("Характеристики объекта строительства", questionId, ChecklistJsonKey.CADASTRAL_NUMBER.getKey());
            String address = getFieldFromChecklist("Характеристики объекта строительства", questionId, ChecklistJsonKey.ADDRESS.getKey());

            // Добавляем в мапу
            block.put(ChecklistJsonKey.CADASTRAL_NUMBER.getKey(), cadastral);
            block.put(ChecklistJsonKey.ADDRESS.getKey(), address);
        }

        try {
            SSLUtils.disableSSLVerification();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return gigaChatIntegration.sendMessage(buildGigaChatRequest(typeText, block));
    }

    /**
     * Получение из объекта JSON вложенного блока данных (map) если он присутствует
     *
     * @param obj - объект внутреннего представления блока данных
     * @return внутреннее представление блока данных в виде мапы
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Object obj) {
        return (obj instanceof Map<?, ?> map) ? (Map<String, Object>) map : new LinkedHashMap<>();
    }

    /**
     * Формирование запроса в гигачат для получения типаового текста блока данных.
     *
     * @param typeText      - текст для подстановки в запрос
     * @param parametersMap - параметры блока данных
     * @return запрос в гигачат для получения типаового текста блока данных
     */
    private String buildGigaChatRequest(String typeText, Map<String, Object> parametersMap) {
        return new StringBuilder()
                .append("Вот мой типовой текст: \n").append(typeText).append("\n\n")
                .append("А вот мои параметры под него которые ввел пользователь:\n")
                .append(parametersMap.toString()).append("\n\n")
                .append("Сформируй итоговый типовой текст в ответе на мое это сообщение. Не используй лишний слов, только типовой текст одним сообщением.")
                .toString();
    }

    /**
     * Получение текста для начала чек-листа.
     *
     * @return текст для начала чек-листа.
     */
    private String getTextFromFileByPath(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalStateException("Файл не найден: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка при чтении типового текста", e);
        }
    }

}
