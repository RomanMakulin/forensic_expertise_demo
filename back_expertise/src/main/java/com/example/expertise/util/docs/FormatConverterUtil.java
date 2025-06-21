package com.example.expertise.util.docs;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

/**
 * Конвертор форматов файлов
 */
public final class FormatConverterUtil {

    private static final Logger log = LoggerFactory.getLogger(FormatConverterUtil.class);

    private static final int DPI = 96;
    private static final String OUTPUT_FORMAT = "jpeg";
    private static final float JPEG_QUALITY = 0.5f;
    private static final int PARALLELISM = 10;

    private FormatConverterUtil() {
        throw new UnsupportedOperationException("FormatConverter is not intended to be instantiated");
    }

    /**
     * Конвертирует PDF в изображение (несколько страниц).
     *
     * @param pdfData байты PDF-документа
     * @return список байтов изображений (JPEG) для каждой страницы
     */
    public static List<byte[]> convertPdfToImages(byte[] pdfData) {
        List<byte[]> images = new ArrayList<>();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(pdfData);
             PDDocument document = PDDocument.load(bais)) {
            if (document.getNumberOfPages() == 0) {
                log.warn("PDF-документ пустой (0 страниц)");
                return images;
            }

            PDFRenderer pdfRenderer = new PDFRenderer(document);

            ForkJoinPool customThreadPool = new ForkJoinPool(PARALLELISM);

            try {
                List<byte[]> pageImages = new ArrayList<>(Collections.nCopies(document.getNumberOfPages(), null));

                // Параллельно обрабатываем страницы
                customThreadPool.submit(() -> {
                    IntStream.range(0, document.getNumberOfPages())
                            .parallel()
                            .forEach(page -> {
                                long startTime = System.currentTimeMillis();
                                try {
                                    // Синхронизируем доступ к pdfRenderer
                                    BufferedImage image;
                                    synchronized (pdfRenderer) {
                                        image = pdfRenderer.renderImageWithDPI(page, DPI);
                                    }
                                    if (image == null) {
                                        log.warn("Не удалось отрендерить страницу {} PDF-документа", page + 1);
                                        return;
                                    }

                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    if ("jpeg".equals(OUTPUT_FORMAT)) {
                                        // Настраиваем сжатие для JPEG
                                        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
                                        ImageWriteParam param = writer.getDefaultWriteParam();
                                        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                                        param.setCompressionQuality(JPEG_QUALITY);

                                        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                                            writer.setOutput(ios);
                                            writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
                                        } finally {
                                            writer.dispose();
                                        }
                                    } else {
                                        ImageIO.write(image, OUTPUT_FORMAT, baos);
                                    }

                                    byte[] imageData = baos.toByteArray();
                                    if (imageData.length == 0) {
                                        log.warn("Изображение для страницы {} PDF-документа пустое", page + 1);
                                        return;
                                    }

                                    pageImages.set(page, imageData);
                                    long endTime = System.currentTimeMillis();
                                    log.info("Страница {} PDF-документа преобразована в изображение, размер: {} байт, время: {} мс",
                                            page + 1, imageData.length, (endTime - startTime));
                                } catch (Exception e) {
                                    log.error("Ошибка рендеринга страницы {}: {}", page + 1, e.getMessage());
                                }
                            });
                }).get(); // Ожидаем завершения всех задач

                // Фильтруем null-значения (страницы с ошибками)
                images.addAll(pageImages.stream().filter(data -> data != null).toList());
            } finally {
                customThreadPool.shutdown();
            }

            return images;
        } catch (Exception e) {
            log.error("Ошибка преобразования PDF в изображения", e);
            return Collections.emptyList();
        }
    }
}