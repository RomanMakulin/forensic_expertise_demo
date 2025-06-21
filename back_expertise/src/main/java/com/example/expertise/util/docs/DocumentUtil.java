package com.example.expertise.util.docs;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Утилитный класс для работы с документами Word (параграфы, текст, форматирование).
 */
@Slf4j
public final class DocumentUtil {

    private static final int PARAGRAPH_SPACING_TWIPS = 240;

    @Getter
    private static final ObjectFactory factory = new ObjectFactory();

    private DocumentUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Получает текст параграфа.
     *
     * @param paragraph Параграф, из которого нужно извлечь текст
     * @return Текст параграфа
     */
    public static String getParagraphText(P paragraph) {
        StringBuilder text = new StringBuilder();
        for (Object content : paragraph.getContent()) {
            if (content instanceof R) {
                R run = (R) content;
                for (Object runContent : run.getContent()) {
                    if (runContent instanceof Text) {
                        text.append(((Text) runContent).getValue());
                    }
                }
            }
        }
        return text.toString();
    }

    /**
     * Создает параграф с заданным текстом и форматированием.
     *
     * @param text Текст параграфа
     * @return Созданный параграф
     */
    public static P createFormattedParagraph(String text) {
        P paragraph = factory.createP();
        R run = factory.createR();
        Text runText = factory.createText();
        runText.setValue(text.trim());
        run.getContent().add(runText);

        if (text.trim().startsWith("Вопрос №")) {
            RPr runProperties = factory.createRPr();
            BooleanDefaultTrue bold = factory.createBooleanDefaultTrue();
            bold.setVal(true);
            runProperties.setB(bold);
            run.setRPr(runProperties);
        }

        PPr paragraphProperties = factory.createPPr();
        PPrBase.Spacing spacing = factory.createPPrBaseSpacing();
        spacing.setAfter(BigInteger.valueOf(PARAGRAPH_SPACING_TWIPS));
        paragraphProperties.setSpacing(spacing);
        paragraph.setPPr(paragraphProperties);

        paragraph.getContent().add(run);
        return paragraph;
    }

    /**
     * Разбивает на параграфы по заданным разделителям
     *
     * @param wordPackage - обрабатываемый документ
     */
    public static void splitParagraphs(WordprocessingMLPackage wordPackage, String questionSeparator, String lineSeparator) {
        MainDocumentPart mainDocumentPart = wordPackage.getMainDocumentPart();
        List<Object> content = mainDocumentPart.getContent();

        for (int i = 0; i < content.size(); i++) {
            if (content.get(i) instanceof P) {
                P paragraph = (P) content.get(i);
                String text = getParagraphText(paragraph);
                if (text.contains(questionSeparator) || text.contains(lineSeparator)) {
                    i = replaceParagraphWithSplitContent(content, i, text, questionSeparator, lineSeparator);
                }
            }
        }
    }

    /**
     * Разбивает текст параграфа на части по разделителю и заменяет параграф на новые.
     *
     * @param content       Список объектов документа
     * @param index         Индекс текущего параграфа
     * @param text          Текст параграфа
     * @param separator     Разделитель для разбиения текста
     * @param lineSeparator Разделитель для разбиения строк внутри частей
     * @return Новый индекс после вставки
     */
    public static int replaceParagraphWithSplitContent(List<Object> content, int index, String text, String separator, String lineSeparator) {
        String[] parts = text.split(separator);
        content.remove(index);

        int insertIndex = index;
        for (String part : parts) {
            if (!part.trim().isEmpty()) {
                insertIndex = addLinesToContent(content, insertIndex, part, lineSeparator);
            }
        }
        return insertIndex - 1;
    }

    public static P createEmptyParagraphWithSpacing() {
        P paragraph = factory.createP();

        PPr paragraphProperties = factory.createPPr();
        PPrBase.Spacing spacing = factory.createPPrBaseSpacing();
        spacing.setAfter(BigInteger.valueOf(PARAGRAPH_SPACING_TWIPS)); // 240 twips = ~12pt
        paragraphProperties.setSpacing(spacing);

        paragraph.setPPr(paragraphProperties);
        return paragraph;
    }

    /**
     * Добавляет строки в список объектов документа.
     *
     * @param content       Список объектов документа
     * @param insertIndex   Индекс для вставки
     * @param part          Часть текста для добавления
     * @param lineSeparator Разделитель строк
     * @return Новый индекс после вставки
     */
    private static int addLinesToContent(List<Object> content, int insertIndex, String part, String lineSeparator) {
        String[] lines = part.split(lineSeparator);
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                content.add(insertIndex++, createFormattedParagraph(line));
            }
        }
        return insertIndex;
    }

    /**
     * Получает все элементы заданного типа в документе.
     *
     * @param obj      Объект, в котором нужно найти элементы
     * @param toSearch Тип элементов для поиска
     * @return Список найденных элементов
     */
    public static List<Object> getAllElements(Object obj, Class<?> toSearch) {
        List<Object> result = new ArrayList<>();
        if (obj instanceof jakarta.xml.bind.JAXBElement) {
            obj = ((jakarta.xml.bind.JAXBElement<?>) obj).getValue();
        }
        if (toSearch.isInstance(obj)) {
            result.add(obj);
        } else if (obj instanceof ContentAccessor) {
            for (Object child : ((ContentAccessor) obj).getContent()) {
                result.addAll(getAllElements(child, toSearch));
            }
        }
        return result;
    }

    public static void addImageLabel(Tc cell, String label) {
        // Создаём параграф
        P labelParagraph = getFactory().createP();

        // Настройка выравнивания по центру
        PPr pPr = getFactory().createPPr();
        Jc jc = getFactory().createJc();
        jc.setVal(JcEnumeration.CENTER);
        pPr.setJc(jc);
        labelParagraph.setPPr(pPr);

        // Текст подписи
        Text text = getFactory().createText();
        text.setValue(label);
        text.setSpace("preserve");

        R run = getFactory().createR();
        run.getContent().add(text);
        labelParagraph.getContent().add(run);

        // Добавляем подпись внутрь ячейки (вниз под изображением)
        cell.getContent().add(labelParagraph);
    }

    public static P createColoredParagraph(String text, String hexColor) {
        P paragraph = factory.createP();
        R run = factory.createR();
        Text runText = factory.createText();
        runText.setValue(text);
        run.getContent().add(runText);

        RPr runProps = factory.createRPr();
        Color color = factory.createColor();
        color.setVal(hexColor); // например, "FF0000" — красный
        runProps.setColor(color);
        run.setRPr(runProps);

        paragraph.getContent().add(run);

        // Добавим отступ после абзаца
        PPr pPr = factory.createPPr();
        PPrBase.Spacing spacing = factory.createPPrBaseSpacing();
        spacing.setAfter(BigInteger.valueOf(PARAGRAPH_SPACING_TWIPS));
        pPr.setSpacing(spacing);
        paragraph.setPPr(pPr);

        return paragraph;
    }


    /**
     * Находит родительский параграф для текста.
     *
     * @param text Текст
     * @return Родительский параграф или null
     */
    public static P findParentParagraph(Text text) {
        return (P) getParentOfType(text, P.class);
    }

    /**
     * Получает родительский объект заданного типа.
     *
     * @param obj  Объект, для которого нужно найти родителя
     * @param type Тип родительского объекта
     * @return Родительский объект или null
     */
    public static Object getParentOfType(Object obj, Class<?> type) {
        Object current = obj;
        while (current != null) {
            if (type.isAssignableFrom(current.getClass())) {
                return current;
            }
            if (current instanceof org.jvnet.jaxb2_commons.ppp.Child) {
                current = ((org.jvnet.jaxb2_commons.ppp.Child) current).getParent();
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * Проверяет, содержит ли текст плейсхолдер из photoMap.
     *
     * @param textValue Текст для проверки
     * @param photoMap  Карта изображений
     * @return true, если текст содержит плейсхолдер
     */
    public static boolean containsPlaceholder(String textValue, Map<String, byte[]> photoMap) {
        return photoMap.keySet().stream().anyMatch(textValue::contains);
    }

    /**
     * Находит запись в photoMap, соответствующую плейсхолдеру в тексте.
     *
     * @param textValue Текст для проверки
     * @param photoMap  Карта изображений
     * @return Optional с найденной записью
     */
    public static Optional<Map.Entry<String, byte[]>> findPlaceholderEntry(String textValue, Map<String, byte[]> photoMap) {
        return findPlaceholderEntry(textValue, photoMap, null);
    }

    /**
     * Находит запись в photoMap, соответствующую плейсхолдеру в тексте, исключая указанный плейсхолдер.
     *
     * @param textValue          Текст для проверки
     * @param photoMap           Карта изображений
     * @param excludePlaceholder Плейсхолдер для исключения
     * @return Optional с найденной записью
     */
    public static Optional<Map.Entry<String, byte[]>> findPlaceholderEntry(String textValue, Map<String, byte[]> photoMap, String excludePlaceholder) {
        return photoMap.entrySet().stream()
                .filter(entry -> (excludePlaceholder == null || !entry.getKey().equals(excludePlaceholder))
                        && textValue.contains(entry.getKey()))
                .findFirst();
    }

    /**
     * Создает параграф с отформатированным текстом.
     *
     * @param multilineText Текст с переносами строк
     */
    public static R createMultilineRun(String multilineText) {
        R run = factory.createR();
        String[] lines = multilineText.split("\n");
        for (int i = 0; i < lines.length; i++) {
            Text text = factory.createText();
            text.setValue(lines[i]);
            run.getContent().add(text);

            // Добавляем перенос строки после каждой строки, кроме последней
            if (i < lines.length - 1) {
                run.getContent().add(factory.createBr());
            }
        }
        return run;
    }

    /**
     * Создает параграф с многострочным текстом и отступами.
     */
    public static P createMultilineParagraph(String text) {
        P paragraph = factory.createP();
        paragraph.getContent().add(createMultilineRun(text));

        PPr pPr = factory.createPPr();
        Jc jc = factory.createJc();
        jc.setVal(JcEnumeration.LEFT);
        pPr.setJc(jc);

        PPrBase.Spacing spacing = factory.createPPrBaseSpacing();
        spacing.setAfter(BigInteger.valueOf(240)); // отступ после абзаца
        pPr.setSpacing(spacing);

        paragraph.setPPr(pPr);
        return paragraph;
    }

    /**
     * Быстрый параграф без форматирования, с выравниванием влево.
     */
    public static P createParagraph(String text) {
        P paragraph = factory.createP();
        R run = factory.createR();
        Text textObj = factory.createText();
        textObj.setValue(text);
        textObj.setSpace("preserve");
        run.getContent().add(textObj);
        paragraph.getContent().add(run);

        PPr pPr = factory.createPPr();
        Jc jc = factory.createJc();
        jc.setVal(JcEnumeration.LEFT);
        pPr.setJc(jc);
        paragraph.setPPr(pPr);

        return paragraph;
    }

    /**
     * Параграф с изображением по центру.
     */
    public static P createImageParagraph(WordprocessingMLPackage wordPackage, byte[] imageBytes, String altText, boolean defaultImageWidth) throws Exception {
        R imageRun = TableUtil.createImageRun(wordPackage, imageBytes, altText, defaultImageWidth);
        return TableUtil.createCenteredParagraphWithSpacing(imageRun, 120, 120);
    }

    public static P createCenteredParagraph(String text) {
        P paragraph = factory.createP();
        R run = factory.createR();
        Text textObj = factory.createText();
        textObj.setValue(text);
        run.getContent().add(textObj);
        paragraph.getContent().add(run);

        PPr pPr = factory.createPPr();
        Jc jc = factory.createJc();
        jc.setVal(JcEnumeration.CENTER);
        pPr.setJc(jc);
        paragraph.setPPr(pPr);

        return paragraph;
    }

    public static void addTextRow(Tbl table, String text) {
        Tr row = TableUtil.createTableRow();
        Tc cell = TableUtil.createStyledCell(text);
        row.getContent().add(cell);
        table.getContent().add(row);
    }

    public static void addTwoCellRow(Tbl table, String leftText, String rightText) {
        Tr row = TableUtil.createTableRow();
        Tc left = TableUtil.createStyledCell(leftText);
        Tc right = TableUtil.createStyledCell(rightText);
        row.getContent().add(left);
        row.getContent().add(right);
        table.getContent().add(row);
    }

    public static List<P> createParagraphsFromText(String fullText) {
        List<P> paragraphs = new ArrayList<>();
        String[] blocks = fullText.split("\\n\\s*\\n"); // разбиваем по двойным переносам (абзацы)

        for (String block : blocks) {
            if (!block.trim().isEmpty()) {
                P paragraph = createMultilineParagraph(block.trim()); // сохраняем переносы внутри блока
                paragraphs.add(paragraph);
            }
        }

        return paragraphs;
    }

}