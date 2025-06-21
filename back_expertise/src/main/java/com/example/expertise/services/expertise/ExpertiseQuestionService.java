package com.example.expertise.services.expertise;

import com.example.expertise.dto.expertise.AnswerDto;
import com.example.expertise.model.expertise.ExpertiseQuestion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Сервис для работы с вопросами экспертизы
 */
public interface ExpertiseQuestionService {

    /**
     * Получить вопрос экспертизы по id
     *
     * @param id идентификатор вопроса экспертизы
     * @return вопрос экспертизы
     */
    ExpertiseQuestion getExpertiseQuestionById(@NotNull(message = "ExpertiseQuestion ID cannot be null") UUID id);

    /**
     * Создать ответ на вопрос экспертизы
     *
     * @param answerDto данные ответа на вопрос экспертизы
     */
    @Transactional
    ExpertiseQuestion createAnswer(@Valid @NotNull(message = "AnswerData cannot be null") AnswerDto answerDto);

    /**
     * Обновить вывод ответа на вопрос экспертизы
     *
     * @param conclusion новый вывод ответа на вопрос экспертизы
     * @param questionId идентификатор вопроса экспертизы
     * @return обновленный вопрос экспертизы
     */
    ExpertiseQuestion updateAnswerConclusion(@NotNull String conclusion, @NotNull UUID questionId);

    /**
     * Удалить фото ответа на вопрос экспертизы
     *
     * @param filePath имя файла фото ответа на вопрос экспертизы
     * @return обновленный вопрос экспертизы
     */
    @Transactional
    ExpertiseQuestion deletePhotoByName(@NotNull(message = "Имя файла не должно быть пустым") String filePath);

}
