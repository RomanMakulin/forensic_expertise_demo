package com.example.expertise.dto.expertise;

import com.example.expertise.dto.checklist.ChecklistInstanceDto;
import com.example.expertise.model.checklist.ChecklistInstance;
import com.example.expertise.model.expertise.ExpertisePhoto;
import com.example.expertise.model.expertise.ExpertiseQuestion;
import com.example.expertise.util.mappers.ChecklistInstanceMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO для ответа на запрос о вопросе экспертизы.
 */
@Data
public class ExpertiseQuestionDto {
    private UUID id;
    private String questionText;
    private String answer;
    private List<ExpertisePhoto> photos;
    private String answerConclusion;
    private List<ChecklistInstanceDto> checklistInstances;

    public ExpertiseQuestionDto(ExpertiseQuestion question, ChecklistInstanceMapper mapper, ObjectMapper objectMapper) {
        this.id = question.getId();
        this.questionText = question.getQuestionText();
        this.answer = question.getAnswer();
        this.answerConclusion = question.getAnswerConclusion();
        this.photos = question.getPhotos();
        this.checklistInstances = question.getChecklistInstances() != null ?
                question.getChecklistInstances().stream()
                        .map(instance -> mapper.toDto(instance, objectMapper))  // ✅ исправлено
                        .collect(Collectors.toList()) :
                null;
    }
}
