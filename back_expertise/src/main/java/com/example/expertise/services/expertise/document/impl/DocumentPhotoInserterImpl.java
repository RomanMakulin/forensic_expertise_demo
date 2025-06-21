package com.example.expertise.services.expertise.document.impl;

import com.example.expertise.enums.Bookmarks;
import com.example.expertise.services.expertise.document.DocumentPhotoInserter;
import com.example.expertise.services.expertise.document.Replacement;
import com.example.expertise.util.docs.DocumentUtil;
import com.example.expertise.util.docs.FormatConverterUtil;
import com.example.expertise.util.docs.TableUtil;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Класс для вставки фотографий в документ Word.
 */
@Component
public class DocumentPhotoInserterImpl implements DocumentPhotoInserter {

    private static final Logger log = LoggerFactory.getLogger(DocumentPhotoInserterImpl.class);

    /**
     * Вставляет скриншот карты экспертизы в документ Word по плейсхолдеру expertiseMapScreenshot.
     *
     * @param photoScreenMap Скриншот карты (MultipartFile)
     * @param wordPackage    Документ Word (WordprocessingMLPackage)
     * @throws Exception Если произошла ошибка при обработке изображения или документа
     */
    @Override
    public void insertScreenMap(WordprocessingMLPackage wordPackage, MultipartFile photoScreenMap) {
        try {
            MainDocumentPart mainDocumentPart = wordPackage.getMainDocumentPart();
            List<Object> textElements = DocumentUtil.getAllElements(mainDocumentPart, Text.class);

            for (Object obj : textElements) {
                Text text = (Text) obj;
                String textValue = text.getValue();

                if (textValue == null || !textValue.equals("expertiseMapScreenshot")) {
                    continue;
                }

                // Получаем родительский параграф
                P parentParagraph = DocumentUtil.findParentParagraph(text);
                if (parentParagraph == null) {
                    log.warn("Не удалось найти родительский параграф для плейсхолдера expertiseMapScreenshot");
                    continue;
                }

                // Очищаем текст плейсхолдера
                text.setValue("");

                // Получаем байты изображения
                byte[] imageData = photoScreenMap.getBytes();
                if (imageData.length == 0) {
                    log.warn("Файл изображения expertiseMapScreenshot пустой");
                    continue;
                }

                // Создаём Run с изображением
                R imageRun = TableUtil.createImageRun(wordPackage, imageData, "Скриншот карты экспертизы", true);

                // Добавляем изображение в параграф
                parentParagraph.getContent().add(imageRun);

                log.info("Скриншот карты экспертизы успешно вставлен");
                break;
            }
        } catch (Exception e) {
            log.error("Ошибка при вставке скриншота карты экспертизы: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Вставляет изображения документов (например, дипломов) в документ в раздел "Приложения".
     */
    @Override
    public void insertPhotoDocs(WordprocessingMLPackage wordPackage, List<byte[]> photoDocs) {
        try {
            MainDocumentPart mainDocumentPart = wordPackage.getMainDocumentPart();
            List<Object> textElements = DocumentUtil.getAllElements(mainDocumentPart, Text.class);

            for (Object obj : textElements) {
                Text text = (Text) obj;
                if (!isPhotoDocsBookmark(text)) {
                    continue;
                }

                replaceBookmarkText(text, Bookmarks.PHOTO_DOCS.getName());
                P parentParagraph = getParentParagraph(text, Bookmarks.PHOTO_DOCS.getName());
                if (parentParagraph == null) continue;

                // Индекс параграфа с плейсхолдером
                int insertIndex = mainDocumentPart.getContent().indexOf(parentParagraph);
                if (insertIndex == -1) {
                    log.warn("Не удалось найти индекс параграфа для вставки фото документов");
                    continue;
                }

                int docIndex = 1;
                int pageIndex = 1;
                for (byte[] docData : photoDocs) {
                    List<byte[]> imageDataList = FormatConverterUtil.convertPdfToImages(docData);
                    if (imageDataList.isEmpty()) {
                        log.warn("Не удалось преобразовать PDF в изображение для документа #{}, размер данных: {}", docIndex, docData.length);
                        docIndex++;
                        continue;
                    }

                    for (byte[] imageData : imageDataList) {
                        // Создаём run с изображением
                        R imageRun = TableUtil.createImageRun(
                                wordPackage,
                                imageData,
                                "Документ " + docIndex + ", страница " + pageIndex,
                                false
                        );

                        // Создаём параграф с этим run
                        P imageParagraph = new P();
                        imageParagraph.getContent().add(imageRun);

                        // Вставляем параграф с изображением после плейсхолдера
                        mainDocumentPart.getContent().add(++insertIndex, imageParagraph);
                        pageIndex++;
                    }
                    docIndex++;
                    pageIndex = 1;
                }

                // Удаляем исходный параграф с плейсхолдером
                mainDocumentPart.getContent().remove(parentParagraph);
                break;
            }
        } catch (Exception e) {
            log.error("Ошибка при вставке изображений документов: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Вставляет изображения по плейсхолдерам в таблицу с двумя колонками.
     */
    @Override
    public void insertAnswerImages(WordprocessingMLPackage wordPackage, Map<String, byte[]> photoMap) {
        try {
            MainDocumentPart mainDocumentPart = wordPackage.getMainDocumentPart();
            List<Object> textElements = DocumentUtil.getAllElements(mainDocumentPart, Text.class);

            List<Replacement> replacements = new ArrayList<>();
            int imageCounter = 1;

            for (int i = 0; i < textElements.size(); i++) {
                Text text = (Text) textElements.get(i);
                if (!hasPlaceholder(text, photoMap)) {
                    continue;
                }

                P parentParagraph = getParentParagraph(text, text.getValue());
                if (parentParagraph == null) {
                    continue;
                }

                int paragraphIndex = mainDocumentPart.getContent().indexOf(parentParagraph);
                if (paragraphIndex == -1) {
                    log.warn("Не удалось найти индекс параграфа для текста: {}", text.getValue());
                    continue;
                }

                // Создаём пустой параграф (пустая строка)
                P spaceParagraph = DocumentUtil.createFormattedParagraph("");

                // Создаём таблицу
                Tbl table = TableUtil.createDefaultTable();
                List<P> paragraphsToRemove = new ArrayList<>();
                paragraphsToRemove.add(parentParagraph);

                while (i < textElements.size()) {
                    Text currentText = (Text) textElements.get(i);
                    Optional<Map.Entry<String, byte[]>> currentEntryOpt = processPlaceholder(currentText, photoMap);
                    if (currentEntryOpt.isEmpty()) {
                        break;
                    }

                    Map.Entry<String, byte[]> currentEntry = currentEntryOpt.get();
                    Tr tableRow = TableUtil.createTableRow();
                    table.getContent().add(tableRow);

                    // Первая ячейка с текущим изображением
                    Tc tableCell1 = TableUtil.createImageCell(
                            wordPackage,
                            currentEntry.getValue(),
                            TableUtil.DEFAULT_IMAGE_WIDTH_TWIPS,
                            "Рисунок " + imageCounter++
                    );
                    tableRow.getContent().add(tableCell1);

                    // Вторая ячейка - либо следующее изображение, либо пустая
                    Tc tableCell2 = TableUtil.createEmptyCell();
                    if (i + 1 < textElements.size()) {
                        Text nextText = (Text) textElements.get(i + 1);
                        Optional<Map.Entry<String, byte[]>> nextEntryOpt = processPlaceholder(nextText, photoMap);
                        if (nextEntryOpt.isPresent()) {
                            tableCell2 = TableUtil.createImageCell(
                                    wordPackage,
                                    nextEntryOpt.get().getValue(),
                                    TableUtil.DEFAULT_IMAGE_WIDTH_TWIPS,
                                    "Рисунок " + imageCounter++
                            );
                            P nextParagraph = DocumentUtil.findParentParagraph(nextText);
                            if (nextParagraph != null) {
                                paragraphsToRemove.add(nextParagraph);
                            }
                            i++; // Пропускаем следующий элемент, так как мы его уже обработали
                        }
                    }
                    tableRow.getContent().add(tableCell2);
                    i++; // Переходим к следующему элементу
                }

                // Добавляем замены
                replacements.add(new Replacement(paragraphIndex, spaceParagraph));
                replacements.add(new Replacement(paragraphIndex + 1, table));

                // Удаляем параграфы
                for (P paragraph : paragraphsToRemove) {
                    mainDocumentPart.getContent().remove(paragraph);
                }
            }

            // Применяем замены
            replacements.sort((r1, r2) -> Integer.compare(r2.index, r1.index));
            for (Replacement r : replacements) {
                mainDocumentPart.getContent().add(r.index, r.content);
            }

            log.info("Вставлено {} изображений", imageCounter - 1);
        } catch (Exception e) {
            log.error("Ошибка при вставке изображений (ответы на вопросы): {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Вспомогательные методы

    private boolean isPhotoDocsBookmark(Text text) {
        String textValue = text.getValue();
        return textValue != null && textValue.contains(Bookmarks.PHOTO_DOCS.getName());
    }

    private boolean hasPlaceholder(Text text, Map<String, byte[]> photoMap) {
        String textValue = text.getValue();
        return textValue != null && DocumentUtil.containsPlaceholder(textValue, photoMap);
    }

    private void replaceBookmarkText(Text text, String bookmark) {
        text.setValue(text.getValue().replace(bookmark, ""));
    }

    private P getParentParagraph(Text text, String logContext) {
        P parentParagraph = DocumentUtil.findParentParagraph(text);
        if (parentParagraph == null) {
            log.warn("Не удалось найти родительский параграф для текста: {}", logContext);
        }
        return parentParagraph;
    }

    private Optional<Map.Entry<String, byte[]>> processPlaceholder(Text text, Map<String, byte[]> photoMap) {
        String textValue = text.getValue();
        if (textValue == null || !DocumentUtil.containsPlaceholder(textValue, photoMap)) {
            return Optional.empty();
        }

        Optional<Map.Entry<String, byte[]>> entryOpt = DocumentUtil.findPlaceholderEntry(textValue, photoMap);
        if (entryOpt.isPresent()) {
            Map.Entry<String, byte[]> entry = entryOpt.get();
            log.info("Найден плейсхолдер '{}'", entry.getKey());
            text.setValue(textValue.replace(entry.getKey(), ""));
        }
        return entryOpt;
    }

    private Tc processNextPlaceholder(WordprocessingMLPackage wordPackage, List<Object> textElements, int index,
                                      Map<String, byte[]> photoMap, String currentKey, List<P> paragraphsToRemove, int imageCounter) throws Exception {
        if (index >= textElements.size()) {
            return TableUtil.createEmptyCell();
        }

        Text nextText = (Text) textElements.get(index);
        String nextTextValue = nextText.getValue();
        if (nextTextValue == null || !DocumentUtil.containsPlaceholder(nextTextValue, photoMap)) {
            return TableUtil.createEmptyCell();
        }

        Optional<Map.Entry<String, byte[]>> nextEntryOpt = DocumentUtil.findPlaceholderEntry(nextTextValue, photoMap, currentKey);
        if (nextEntryOpt.isEmpty()) {
            return TableUtil.createEmptyCell();
        }

        Map.Entry<String, byte[]> nextEntry = nextEntryOpt.get();
        log.info("Найден соседний плейсхолдер '{}'", nextEntry.getKey());
        nextText.setValue(nextTextValue.replace(nextEntry.getKey(), ""));

        Tc tableCell = TableUtil.createImageCell(
                wordPackage,
                nextEntry.getValue(),
                TableUtil.DEFAULT_IMAGE_WIDTH_TWIPS,
                "Рисунок " + imageCounter
        );
        P nextParagraph = DocumentUtil.findParentParagraph(nextText);
        if (nextParagraph != null) {
            paragraphsToRemove.add(nextParagraph);
        }
        textElements.set(index, nextText); // Обновляем элемент в списке
        return tableCell;
    }

    private void removeParagraphs(MainDocumentPart mainDocumentPart, List<P> paragraphsToRemove, int paragraphIndex) {
        for (P paragraph : paragraphsToRemove) {
            int index = mainDocumentPart.getContent().indexOf(paragraph);
            if (index != -1) {
                mainDocumentPart.getContent().remove(index);
                if (index <= paragraphIndex) {
                    paragraphIndex--;
                }
            }
        }
    }
}