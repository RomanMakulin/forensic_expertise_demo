package com.example.expertise.dto.expertise;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * DTO для представления данных ответа на вопрос экспертизы
 * с фронта
 */
@Data
public class AnswerDto {

    /**
     * Идентификатор вопроса, на который дан ответ
     */
    @NotNull(message = "Question ID cannot be null")
    @JsonProperty("question_id")
    private UUID questionId;

    /**
     * Ответ на вопрос
     */
    @NotNull(message = "Answer cannot be null")
    private String answer;

    /**
     * Список фотографий, связанных с ответом
     */
    private List<MultipartFile> photos;

}
