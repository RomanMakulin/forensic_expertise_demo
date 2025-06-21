package com.example.expertise.services.expertise.document.impl;

import com.example.expertise.model.checklist.ChecklistInstance;
import com.example.expertise.model.expertise.ExpertiseQuestion;
import com.example.expertise.services.expertise.checklists.render.document.ChecklistRendererDispatcher;
import com.example.expertise.services.expertise.checklists.render.document.DocumentChecklistRenderer;
import com.example.expertise.services.expertise.document.DocumentChecklistInserter;
import com.example.expertise.util.docs.DocumentUtil;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Процессор для вставки всех чек-листов по закладкам в Word-документ.
 */
@Component
public class DocumentChecklistInserterImpl implements DocumentChecklistInserter {

    private static final Logger log = LoggerFactory.getLogger(DocumentChecklistInserterImpl.class);
    private final ChecklistRendererDispatcher dispatcher;

    public DocumentChecklistInserterImpl(ChecklistRendererDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * Вставка чек-листов в документ по закладкам.
     *
     * @param wordPackage - докумет
     * @param questions   - вопросы, которые могут содержать чек-листы
     */
    @Override
    public void insertAnswerChecklist(WordprocessingMLPackage wordPackage, List<ExpertiseQuestion> questions) {
        MainDocumentPart mainPart = wordPackage.getMainDocumentPart();
        List<Object> textElements = DocumentUtil.getAllElements(mainPart, Text.class);

        for (ExpertiseQuestion question : questions) {
            for (ChecklistInstance checklist : question.getChecklistInstances()) {
                String placeholder = "[CHECKLIST_" + checklist.getId() + "]";

                Text targetText = textElements.stream()
                        .filter(t -> t instanceof Text && placeholder.equals(((Text) t).getValue()))
                        .map(t -> (Text) t)
                        .findFirst()
                        .orElse(null);

                if (targetText == null) {
                    log.warn("Закладка {} не найдена", placeholder);
                    continue;
                }

                targetText.setValue("");
                P placeholderParagraph = DocumentUtil.findParentParagraph(targetText);
                if (placeholderParagraph == null) {
                    log.warn("Параграф для закладки {} не найден", placeholder);
                    continue;
                }

                String templateName = checklist.getChecklistTemplate().getName();
                DocumentChecklistRenderer renderer = dispatcher.getRenderer(templateName);
                log.info("Вставка чек-листа {} через {}", checklist.getId(), renderer.getClass().getSimpleName());

                renderer.insertAnswerChecklist(wordPackage, checklist, placeholderParagraph);
            }
        }
    }
}
