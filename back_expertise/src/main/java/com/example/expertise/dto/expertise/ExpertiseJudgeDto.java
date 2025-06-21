package com.example.expertise.dto.expertise;

import com.example.expertise.model.expertise.ExpertiseJudge;
import lombok.Data;

import java.util.UUID;

/**
 * DTO для представления информации о судье экспертной экспертизы.
 */
@Data
public class ExpertiseJudgeDto {
    private UUID id;
    private String fullName;

    public ExpertiseJudgeDto(ExpertiseJudge judge) {
        this.id = judge.getId();
        this.fullName = judge.getFullName();
    }
}
