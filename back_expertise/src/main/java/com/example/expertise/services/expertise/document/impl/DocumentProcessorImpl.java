package com.example.expertise.services.expertise.document.impl;

import com.example.expertise.model.expertise.ExpertiseQuestion;
import com.example.expertise.services.expertise.document.DocumentChecklistInserter;
import com.example.expertise.services.expertise.document.DocumentPhotoInserter;
import com.example.expertise.services.expertise.document.DocumentProcessor;
import com.example.expertise.util.docs.DocumentUtil;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.model.fields.merge.MailMerger;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * Класс для обработки документов в формате DOCX.
 */
@Component
public class DocumentProcessorImpl implements DocumentProcessor {

    private static final Logger log = LoggerFactory.getLogger(DocumentProcessorImpl.class);

    private final DocumentPhotoInserter documentPhotoInserterImpl;
    private final DocumentChecklistInserter documentChecklistInserterImpl;

    public static final String QUESTION_SEPARATOR = "<!-- QUESTION_SEPARATOR -->";
    public static final String LINE_SEPARATOR = "<!-- LINE_SEPARATOR -->";
    private static final int PARAGRAPH_SPACING_TWIPS = 240;

    public DocumentProcessorImpl(@Lazy DocumentPhotoInserter documentPhotoInserterImpl,
                                 DocumentChecklistInserter documentChecklistInserterImpl) {
        this.documentPhotoInserterImpl = documentPhotoInserterImpl;
        this.documentChecklistInserterImpl = documentChecklistInserterImpl;
    }

    /**
     * Процесс проставления значений в документе по закладкам
     *
     * @param template  - шаблонный документ
     * @param mergeData - данные для заполнения
     * @param photoMap  - карта изображений
     * @param questions - список вопросов экспертизы
     * @return - обработанный документ в виде массива байтов
     */
    @Override
    public byte[] processDocument(WordprocessingMLPackage template,
                                  Map<DataFieldName, String> mergeData,
                                  Map<String, byte[]> photoMap,
                                  List<byte[]> photoDocs,
                                  List<ExpertiseQuestion> questions,
                                  MultipartFile screenMap) {
        try {
            MailMerger.setMERGEFIELDInOutput(MailMerger.OutputField.REMOVED);
            MailMerger.performMerge(template, mergeData, true);

            DocumentUtil.splitParagraphs(template, QUESTION_SEPARATOR, LINE_SEPARATOR);

            documentChecklistInserterImpl.insertAnswerChecklist(template, questions); // проставляем чек-листы по заданным ключам (по ответам на вопросы)
            documentPhotoInserterImpl.insertAnswerImages(template, photoMap); // вставляем изображения по заданным ключам (по ответам на вопросы)
            documentPhotoInserterImpl.insertPhotoDocs(template, photoDocs); // вставляем фото документов эксперта в приложении экспертизы
            documentPhotoInserterImpl.insertScreenMap(template, screenMap); // вставляем карту (скрин) объекта экспертизы

            // Сохранение итогового файла
            File outputFile = new File("output.docx");
            template.save(outputFile);
            log.info("Итоговый файл сохранён: {}", outputFile.getAbsolutePath());

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            template.save(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Ошибка при обработке документа", e);
            throw new RuntimeException("Не удалось обработать DOCX-документ", e);
        }
    }



}