package com.example.expertise.services.expertise.impl;

import com.example.expertise.dto.expertise.AnswerDto;
import com.example.expertise.exceptions.ExpertisePhotoNotFoundException;
import com.example.expertise.exceptions.ExpertiseQuestionNotFoundException;
import com.example.expertise.integration.minio.MinioIntegration;
import com.example.expertise.model.expertise.ExpertisePhoto;
import com.example.expertise.model.expertise.ExpertiseQuestion;
import com.example.expertise.repository.expertise.ExpertisePhotoRepository;
import com.example.expertise.repository.expertise.ExpertiseQuestionRepository;
import com.example.expertise.services.expertise.AnswerPhotoUploader;
import com.example.expertise.services.expertise.ExpertiseQuestionService;
import com.example.expertise.services.expertise.ConclusionGenerator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

/**
 * Сервис для управления вопросами экспертизы, включая создание ответов и загрузку связанных фотографий.
 */
@Service
@Validated
public class ExpertiseQuestionServiceImpl implements ExpertiseQuestionService {

    private static final Logger log = LoggerFactory.getLogger(ExpertiseQuestionServiceImpl.class);

    private final ExpertisePhotoRepository expertisePhotoRepository;
    private final ExpertiseQuestionRepository expertiseQuestionRepository;
    private final MinioIntegration minioIntegration;
    private final AnswerPhotoUploader answerPhotoUploader;
    private final ConclusionGenerator conclusionGenerator;

    @Autowired
    public ExpertiseQuestionServiceImpl(ExpertisePhotoRepository expertisePhotoRepository,
                                        ExpertiseQuestionRepository expertiseQuestionRepository,
                                        MinioIntegration minioIntegration,
                                        AnswerPhotoUploader answerPhotoUploader,
                                        ConclusionGenerator conclusionGenerator) {
        this.expertisePhotoRepository = expertisePhotoRepository;
        this.expertiseQuestionRepository = expertiseQuestionRepository;
        this.minioIntegration = minioIntegration;
        this.answerPhotoUploader = answerPhotoUploader;
        this.conclusionGenerator = conclusionGenerator;
    }

    /**
     * Получает вопрос экспертизы по его идентификатору.
     *
     * @param id идентификатор вопроса экспертизы
     * @return объект {@link ExpertiseQuestion}
     * @throws ExpertiseQuestionNotFoundException если вопрос с указанным ID не найден
     */
    @Override
    public ExpertiseQuestion getExpertiseQuestionById(@NotNull(message = "ExpertiseQuestion ID cannot be null") UUID id) {
        return expertiseQuestionRepository.findById(id)
                .orElseThrow(ExpertiseQuestionNotFoundException::new);
    }

    /**
     * Создаёт или обновляет ответ на вопрос экспертизы, включая загрузку связанных фотографий.
     *
     * @param answerDto данные ответа на вопрос экспертизы
     * @return обновлённый объект {@link ExpertiseQuestion}
     * @throws IllegalArgumentException если входные данные некорректны
     */
    @Transactional
    @Override
    public ExpertiseQuestion createAnswer(@Valid @NotNull(message = "AnswerData cannot be null") AnswerDto answerDto) {
        ExpertiseQuestion expertiseQuestion = getExpertiseQuestionById(answerDto.getQuestionId());
        expertiseQuestion.setAnswer(answerDto.getAnswer());
        conclusionGenerator.addConclusionIfPresent(expertiseQuestion, answerDto.getAnswer());
        answerPhotoUploader.uploadPhotosIfPresent(expertiseQuestion, answerDto);
        return expertiseQuestionRepository.save(expertiseQuestion);
    }

    /**
     * Обновляет вывод ответа на вопрос экспертизы.
     * @param conclusion новый вывод ответа на вопрос экспертизы
     * @param questionId идентификатор вопроса экспертизы
     * @return обновлённый объект {@link ExpertiseQuestion} с обновлённым выводом ответа на вопрос экспертизы
     */
    @Transactional
    @Override
    public ExpertiseQuestion updateAnswerConclusion(@NotNull String conclusion, @NotNull UUID questionId) {
        ExpertiseQuestion expertiseQuestion = getExpertiseQuestionById(questionId);
        expertiseQuestion.setAnswerConclusion(conclusion);
        return expertiseQuestionRepository.save(expertiseQuestion);
    }

    /**
     * Удаляет фотографию по её имени.
     *
     * @param filePath имя файла фото ответа на вопрос экспертизы
     * @return Обновлённый объект {@link ExpertiseQuestion} с удалённой фотографией.
     */
    @Transactional
    @Override
    public ExpertiseQuestion deletePhotoByName(@NotNull(message = "Имя файла не должно быть пустым") String filePath) {
        ExpertisePhoto photoToDelete = expertisePhotoRepository.findExpertisePhotoByFilePath(filePath).orElseThrow(ExpertisePhotoNotFoundException::new);
        UUID expertiseQuestionId = photoToDelete.getExpertiseQuestion().getId();

        expertisePhotoRepository.delete(photoToDelete); // Удаляем фото из БД
        minioIntegration.deleteAnswerPhoto(filePath); // Удаляем фото из хранилища MinIO

        return getExpertiseQuestionById(expertiseQuestionId); // Возвращаем обновлённый вопрос экспертизы
    }
}