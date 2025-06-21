package com.example.expertise.services.expertise.document;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Класс для вставки фотографий в документ Word.
 */
public interface DocumentPhotoInserter {
    /**
     * Вставляет скриншот карты экспертизы в документ Word по плейсхолдеру expertiseMapScreenshot.
     *
     * @param photoScreenMap Скриншот карты (MultipartFile)
     * @param wordPackage    Документ Word (WordprocessingMLPackage)
     * @throws Exception Если произошла ошибка при обработке изображения или документа
     */
    void insertScreenMap(WordprocessingMLPackage wordPackage, MultipartFile photoScreenMap);

    /**
     * Вставляет изображения документов (например, дипломов) в документ в раздел "Приложения".
     */
    void insertPhotoDocs(WordprocessingMLPackage wordPackage, List<byte[]> photoDocs);

    /**
     * Вставляет изображения по плейсхолдерам в таблицу с двумя колонками.
     */
    void insertAnswerImages(WordprocessingMLPackage wordPackage, Map<String, byte[]> photoMap);
}
