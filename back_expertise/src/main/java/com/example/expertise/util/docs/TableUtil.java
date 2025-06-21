package com.example.expertise.util.docs;

import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.*;

import java.math.BigInteger;
import java.util.List;

/**
 * Утилитный класс для работы с таблицами в документах Word.
 */
public final class TableUtil {

    private static final ObjectFactory factory = new ObjectFactory();

    private TableUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    // Константы для таблиц и изображений
    public static final int DEFAULT_TABLE_WIDTH_TWIPS = 9000;
    public static final int DEFAULT_SCREEN_MAP_WIDTH = 9750;
    public static final String DEFAULT_WIDTH_TYPE = "dxa";
    public static final int DEFAULT_BORDER_SIZE = 4; // 0.5 pt
    public static final String DEFAULT_BORDER_COLOR = "000000";
    public static final STBorder DEFAULT_BORDER_STYLE = STBorder.SINGLE;
    public static final int DEFAULT_IMAGE_WIDTH_TWIPS = 4500;
    private static final int SPACING_TOP_IMAGE = 120; // 6 pt в TWIPS
    private static final int SPACING_BOTTOM_IMAGE = 120; // 6 pt в TWIPS
    private static final int SPACING_TOP_CAPTION = 60; // 3 pt в TWIPS
    private static final int SPACING_BOTTOM_CAPTION = 120; // 6 pt в TWIPS

    /**
     * Создает таблицу с заданными параметрами.
     */
    public static Tbl createTable(int widthTwips, String widthType, int borderSize, String borderColor, STBorder borderStyle) {
        Tbl table = factory.createTbl();
        TblPr tblPr = factory.createTblPr();
        setTableWidth(tblPr, widthTwips, widthType);
        setTableBorders(tblPr, borderSize, borderColor, borderStyle);
        table.setTblPr(tblPr);
        return table;
    }

    /**
     * Создает таблицу с параметрами по умолчанию.
     *
     * @return Таблица с параметрами по умолчанию.
     */
    public static Tbl createDefaultTable() {
        return createTable(
                TableUtil.DEFAULT_TABLE_WIDTH_TWIPS,
                TableUtil.DEFAULT_WIDTH_TYPE,
                TableUtil.DEFAULT_BORDER_SIZE,
                TableUtil.DEFAULT_BORDER_COLOR,
                TableUtil.DEFAULT_BORDER_STYLE
        );
    }

    /**
     * Создает строку таблицы.
     */
    public static Tr createTableRow() {
        return factory.createTr();
    }

    /**
     * Создает ячейку таблицы с изображением и подписью, центрированную по горизонтали и вертикали с отступами.
     */
    public static Tc createImageCell(WordprocessingMLPackage wordPackage, byte[] imageData, int imageWidth, String caption) {
        try {
            Tc tableCell = factory.createTc();

            // Вертикальное выравнивание ячейки по центру
            TcPr tableCellProperties = factory.createTcPr();
            CTVerticalJc verticalAlignment = factory.createCTVerticalJc();
            verticalAlignment.setVal(STVerticalJc.CENTER);
            tableCellProperties.setVAlign(verticalAlignment);
            tableCell.setTcPr(tableCellProperties);

            // Параграф с изображением
            P imageParagraph = createCenteredParagraphWithSpacing(
                    createImageRun(wordPackage, imageData, null, true),
                    SPACING_TOP_IMAGE, SPACING_BOTTOM_IMAGE
            );
            tableCell.getContent().add(imageParagraph);

            // Подпись, если указана
            if (caption != null && !caption.isEmpty()) {
                P captionParagraph = createCenteredParagraphWithSpacing(
                        createTextRun(caption),
                        SPACING_TOP_CAPTION, SPACING_BOTTOM_CAPTION
                );
                tableCell.getContent().add(captionParagraph);
            }

            return tableCell;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании ячейки таблицы с изображением и подписью.", e);
        }
    }

    /**
     * Создает пустую ячейку таблицы с вертикальным выравниванием по центру.
     */
    public static Tc createEmptyCell() {
        Tc emptyCell = factory.createTc();
        TcPr tableCellProperties = factory.createTcPr();
        CTVerticalJc verticalAlignment = factory.createCTVerticalJc();
        verticalAlignment.setVal(STVerticalJc.CENTER);
        tableCellProperties.setVAlign(verticalAlignment);
        emptyCell.setTcPr(tableCellProperties);
        emptyCell.getContent().add(factory.createP());
        return emptyCell;
    }

    public static void fixTwoColumnWidths(Tbl table) {
        TblGrid tblGrid = factory.createTblGrid();
        TblGridCol col1 = factory.createTblGridCol();
        col1.setW(BigInteger.valueOf(DEFAULT_TABLE_WIDTH_TWIPS / 2));
        TblGridCol col2 = factory.createTblGridCol();
        col2.setW(BigInteger.valueOf(DEFAULT_TABLE_WIDTH_TWIPS / 2));
        tblGrid.getGridCol().add(col1);
        tblGrid.getGridCol().add(col2);
        table.setTblGrid(tblGrid);
    }

    /**
     * Создает run с изображением.
     */
    public static R createImageRun(WordprocessingMLPackage wordPackage, byte[] imageData, String altText, boolean defaultImageWidth) throws Exception {
        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordPackage, imageData);

        Inline inline;
        if (defaultImageWidth)
            inline = imagePart.createImageInline(null, altText, 0, 1, false, DEFAULT_IMAGE_WIDTH_TWIPS);
        else inline = imagePart.createImageInline(null, altText, 0, 1, false);

        R imageRun = factory.createR();
        Drawing drawing = factory

                .createDrawing();
        drawing.getAnchorOrInline().add(inline);
        imageRun.getContent().add(drawing);
        return imageRun;
    }

    public static P createLeftAlignedParagraph(String text, boolean bold) {
        P paragraph = factory.createP();
        PPr pPr = factory.createPPr();

        // Выравнивание по левому краю
        Jc jc = factory.createJc();
        jc.setVal(JcEnumeration.LEFT);
        pPr.setJc(jc);
        paragraph.setPPr(pPr);

        // Обработка каждой строки отдельно
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            R run = factory.createR();
            Text t = factory.createText();
            t.setValue(lines[i]);

            // Включить xml:space="preserve" для сохранения пробелов
            t.setSpace("preserve");
            run.getContent().add(t);

            // Установим жирность, если нужно
            if (bold) {
                RPr rPr = factory.createRPr();
                BooleanDefaultTrue b = new BooleanDefaultTrue();
                b.setVal(true);
                rPr.setB(b);
                run.setRPr(rPr);
            }

            paragraph.getContent().add(run);

            // Добавляем <w:br/> если это не последняя строка
            if (i < lines.length - 1) {
                Br br = factory.createBr();
                paragraph.getContent().add(br);
            }
        }

        return paragraph;
    }

    /**
     * Устанавливает ширины колонок по списку.
     */
    public static void fixColumnWidths(Tbl table, List<Integer> widths) {
        TblGrid tblGrid = factory.createTblGrid();
        for (int width : widths) {
            TblGridCol col = factory.createTblGridCol();
            col.setW(BigInteger.valueOf(width));
            tblGrid.getGridCol().add(col);
        }
        table.setTblGrid(tblGrid);
    }

    /**
     * Добавляет строку с текстом в таблицу.
     */
    public static void addTableRow(Tbl table, String text) {
        Tr row = createTableRow();
        Tc cell = createEmptyCell();
        P paragraph = factory.createP();
        R run = factory.createR();
        Text textObj = factory.createText();
        textObj.setValue(text);
        run.getContent().add(textObj);
        paragraph.getContent().add(run);
        cell.getContent().set(0, paragraph); // Заменяем пустой параграф
        row.getContent().add(cell);
        table.getContent().add(row);
    }

    public static P createCenteredParagraphWithSpacing(R content, int spacingTop, int spacingBottom) {
        P paragraph = factory.createP();
        paragraph.getContent().add(content);

        PPr pPr = factory.createPPr();
        Jc justification = factory.createJc();
        justification.setVal(JcEnumeration.CENTER);
        pPr.setJc(justification);

        PPrBase.Spacing spacing = factory.createPPrBaseSpacing();
        spacing.setBefore(BigInteger.valueOf(spacingTop));
        spacing.setAfter(BigInteger.valueOf(spacingBottom));
        pPr.setSpacing(spacing);

        paragraph.setPPr(pPr);
        return paragraph;
    }

    // Вспомогательные методы

    private static void setTableWidth(TblPr tblPr, int widthTwips, String widthType) {
        TblWidth tblWidth = factory.createTblWidth();
        tblWidth.setW(BigInteger.valueOf(widthTwips));
        tblWidth.setType(widthType);
        tblPr.setTblW(tblWidth);
    }

    private static void setTableBorders(TblPr tblPr, int borderSize, String borderColor, STBorder borderStyle) {
        TblBorders borders = factory.createTblBorders();
        CTBorder border = factory.createCTBorder();
        border.setVal(borderStyle);
        border.setSz(BigInteger.valueOf(borderSize));
        border.setSpace(BigInteger.ZERO);
        border.setColor(borderColor);
        borders.setTop(border);
        borders.setBottom(border);
        borders.setLeft(border);
        borders.setRight(border);
        borders.setInsideH(border);
        borders.setInsideV(border);
        tblPr.setTblBorders(borders);
    }

    private static R createTextRun(String text) {
        R run = factory.createR();
        Text textObj = factory.createText();
        textObj.setValue(text);
        run.getContent().add(textObj);
        return run;
    }

    /**
     * Создаёт ячейку с текстом, выровненным по левому краю и с сохранением переносов строк.
     */
    public static Tc createStyledCell(String text) {
        Tc cell = createEmptyCell();
        P paragraph = createLeftAlignedParagraph(text != null ? text : "", false);
        cell.getContent().set(0, paragraph); // заменяем пустой параграф
        return cell;
    }
}