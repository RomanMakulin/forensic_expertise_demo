package com.example.expertise.services.expertise.impl;

import com.example.expertise.model.expertise.ExpertiseQuestion;
import com.example.expertise.services.expertise.ConclusionGenerator;
import com.example.expertise.integration.gigachat.GigaChatIntegration;
import com.example.expertise.util.SSLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Реализация генератора выводов.
 */
@Component
public class ConclusionGeneratorImpl implements ConclusionGenerator {
    private static final Logger log = LoggerFactory.getLogger(ConclusionGeneratorImpl.class);

    private final GigaChatIntegration gigaChatIntegration;

    public ConclusionGeneratorImpl(GigaChatIntegration gigaChatIntegration) {
        this.gigaChatIntegration = gigaChatIntegration;
    }

    /**
     * Добавляет заключение к вопросу, если оно существует и не пустое.
     */
    @Override
    public void addConclusionIfPresent(ExpertiseQuestion expertiseQuestion, String answer) {
        String question = expertiseQuestion.getQuestionText();
        String conclusion = generateAnswerConclusion(question, answer);
        if (conclusion != null && !conclusion.isEmpty()) {
            expertiseQuestion.setAnswerConclusion(conclusion);
        }
    }

    /**
     * Генерирует вывод по ответу на вопрос экспертизы.
     *
     * @param question ответ на вопрос экспертизы
     * @param answer   ответ на вопрос экспертизы
     * @return генерированный вывод или null, если вывод не может быть сгенерирован
     */
    private String generateAnswerConclusion(String question, String answer) {
        String prompt = "Лаконично и понятно сформируй уникальный и профессиональный вывод по ответу: " + answer + " на вопрос: " + question +
                ". Не используй лишних слов . Ответ дай в виде развернутого ответа, который можно копировать и вставлять";
        try {
            SSLUtils.disableSSLVerification();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return gigaChatIntegration.sendMessage(prompt);
    }
}
