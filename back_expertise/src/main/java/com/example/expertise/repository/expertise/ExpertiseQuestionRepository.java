package com.example.expertise.repository.expertise;

import com.example.expertise.model.expertise.ExpertiseQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью "Вопрос экспертизы".
 */
public interface ExpertiseQuestionRepository extends JpaRepository<ExpertiseQuestion, UUID> {

    /**
     * Поиск вопроса экспертизы по идентификатору.
     *
     * @param id Идентификатор вопроса экспертизы.
     * @return Вопрос экспертизы с указанным идентификатором, если найден; иначе null.
     */
    Optional<ExpertiseQuestion> findById(UUID id);

}
