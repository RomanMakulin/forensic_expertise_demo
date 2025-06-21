package com.example.expertise.services.expertise.checklists.render.document;

import com.example.expertise.model.checklist.ChecklistInstance;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;

/**
 * Интерфейс DocumentChecklistRenderer определяет методы для рендеринга чек-листов в документ.
 */
public interface DocumentChecklistRenderer {

    /**
     * Вставка ответов на чек-лист в документ.
     *
     * @param wordPackage        пакет WordprocessingMLPackage
     * @param checklist          экземпляр чек-листа
     * @param placeholderParagraph абзац-заполнитель
     */
    void insertAnswerChecklist(WordprocessingMLPackage wordPackage, ChecklistInstance checklist, P placeholderParagraph);
}

