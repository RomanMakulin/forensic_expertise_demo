package com.example.expertise.services.expertise.document;

import com.example.expertise.model.expertise.ExpertiseQuestion;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import java.util.List;

/**
 * Процессор для вставки всех чек-листов по закладкам в Word-документ.
 */
public interface DocumentChecklistInserter {
    /**
     * Вставка чек-листов в документ по закладкам.
     *
     * @param wordPackage - докумет
     * @param questions   - вопросы, которые могут содержать чек-листы
     */
    void insertAnswerChecklist(WordprocessingMLPackage wordPackage, List<ExpertiseQuestion> questions);
}
