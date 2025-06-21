package com.example.expertise.dto.expertise;

import com.example.expertise.model.expertise.Expertise;
import com.example.expertise.util.mappers.ChecklistInstanceMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class ExpertiseResponseDto {
    private UUID id;
    private UUID profileId;
    private String templateName;
    private String caseNumber;
    private String speciality;
    private String name;
    private LocalDate rulingDate;
    private String courtName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate signDate;
    private String presidingJudge;
    private List<ExpertiseJudgeDto> expertiseJudges;
    private String plaintiff;
    private String location;
    private String volumeCount;
    private String participants;
    private LocalDateTime inspectionDateTime;
    private List<ExpertiseQuestionDto> questions;

    public ExpertiseResponseDto(Expertise expertise, ChecklistInstanceMapper checklistInstanceMapper, ObjectMapper objectMapper) {
        this.id = expertise.getId();
        this.profileId = expertise.getProfileId();
        this.templateName = expertise.getTemplateName();
        this.speciality = expertise.getSpeciality();
        this.caseNumber = expertise.getCaseNumber();
        this.name = expertise.getName();
        this.rulingDate = expertise.getRulingDate();
        this.courtName = expertise.getCourtName();
        this.startDate = expertise.getStartDate();
        this.endDate = expertise.getEndDate();
        this.signDate = expertise.getSignDate();
        this.presidingJudge = expertise.getPresidingJudge();
        this.expertiseJudges = expertise.getExpertiseJudges() != null ?
                expertise.getExpertiseJudges().stream().map(ExpertiseJudgeDto::new).collect(Collectors.toList()) : null;
        this.plaintiff = expertise.getPlaintiff();
        this.location = expertise.getLocation();
        this.volumeCount = expertise.getVolumeCount();
        this.participants = expertise.getParticipants();
        this.inspectionDateTime = expertise.getInspectionDateTime();
        this.questions = expertise.getQuestions() != null ?
                expertise.getQuestions().stream()
                        .map(question -> new ExpertiseQuestionDto(question, checklistInstanceMapper, objectMapper))
                        .collect(Collectors.toList()) :
                null;
    }

}