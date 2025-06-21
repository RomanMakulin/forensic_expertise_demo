package com.example.expertise.repository.checklist;

import com.example.expertise.model.checklist.ChecklistInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с экземплярами чек-листов
 */
@Repository
public interface ChecklistInstanceRepository extends JpaRepository<ChecklistInstance, UUID> {

    /**
     * Проверка существования экземпляра чек-листа по идентификатору вопроса экспертизы
     *
     * @param expertiseQuestionId идентификатор вопроса экспертизы
     * @return результат проверки
     */
    boolean existsChecklistInstanceByExpertiseQuestionId(UUID expertiseQuestionId);

    /**
     * Поиск экземпляра чек-листа по идентификатору вопроса экспертизы и шаблона экспертизы
     * Так как у каждого вопроса может быть только один вид чек-листа (по шаблону)
     *
     * @param questionId          идентификатор вопроса экспертизы
     * @param checklistTemplateId идентификатор шаблона экспертизы
     * @return экземпляр чек-листа, если он найден; иначе null
     */
    Optional<ChecklistInstance> findByExpertiseQuestionIdAndChecklistTemplateId(UUID questionId, UUID checklistTemplateId);
}
