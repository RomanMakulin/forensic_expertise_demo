package com.example.expertise.services.expertise.document;

import com.example.expertise.model.expertise.Expertise;
import org.docx4j.model.fields.merge.DataFieldName;

import java.util.Map;

/**
 * Сервис для генерации данных для заполнения закладок в экспертизе.
 */
public interface BookmarkInserter {

    /**
     * Генерирует данные для заполнения закладок.
     *
     * @param expertise Экспертная экспертиза
     * @return Карта данных
     */
    Map<DataFieldName, String> generateVariables(Expertise expertise);

}
