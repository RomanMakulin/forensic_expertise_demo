package com.example.expertise.services.expertise.document;

import com.example.expertise.model.expertise.ExpertiseQuestion;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Класс для обработки документов в формате DOCX.
 */
public interface DocumentProcessor {
    /**
     * Процесс проставления значений в документе по закладкам
     *
     * @param template  - шаблонный документ
     * @param mergeData - данные для заполнения
     * @param photoMap  - карта изображений
     * @param questions - список вопросов экспертизы
     * @return - обработанный документ в виде массива байтов
     */
    byte[] processDocument(WordprocessingMLPackage template,
                                  Map<DataFieldName, String> mergeData,
                                  Map<String, byte[]> photoMap,
                                  List<byte[]> photoDocs,
                                  List<ExpertiseQuestion> questions,
                                  MultipartFile screenMap);
}
