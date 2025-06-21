package com.example.expertise.util.mappers;

import com.example.expertise.dto.expertise.CreateExpertiseDto;
import com.example.expertise.model.expertise.Expertise;
import com.example.expertise.model.expertise.ExpertiseJudge;
import com.example.expertise.model.expertise.ExpertiseQuestion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper для преобразования данных экспертизы в сущность Expertise
 * Null поля игнорируются
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ExpertiseMapper {

    /**
     * Преобразование данных создания экспертизы в сущность Expertise
     *
     * @param createExpertiseDto данные для создания экспертизы
     * @param expertise          сущность Expertise
     */
    @Mapping(target = "templateName", source = "templateName")
    @Mapping(target = "expertiseJudges", expression = "java(mapExpertiseJudges(createExpertiseDto.getExpertiseJudges(), expertise))")
    @Mapping(target = "questions", expression = "java(mapQuestions(createExpertiseDto.getQuestions(), expertise))")
    void updateExpertiseFromDto(CreateExpertiseDto createExpertiseDto, @MappingTarget Expertise expertise);

    /**
     * Преобразование списка ФИО судей в список сущностей ExpertiseJudge
     *
     * @param judgeNames список ФИО судей
     * @param expertise  сущность Expertise
     * @return список сущностей ExpertiseJudge
     */
    default List<ExpertiseJudge> mapExpertiseJudges(List<String> judgeNames, Expertise expertise) {
        if (judgeNames == null) return null;

        // Создаём новый список судей
        List<ExpertiseJudge> newJudges = judgeNames.stream()
                .map(name -> new ExpertiseJudge(name, expertise))
                .collect(Collectors.toList());

        // Устанавливаем новый список
        expertise.setExpertiseJudges(newJudges);
        return newJudges;
    }

    /**
     * Преобразование списка текстов вопросов в список сущностей ExpertiseQuestion
     *
     * @param questionTexts список текстов вопросов
     * @param expertise     сущность Expertise
     * @return список сущностей ExpertiseQuestion
     */
    default List<ExpertiseQuestion> mapQuestions(List<String> questionTexts, Expertise expertise) {
        if (questionTexts == null) return null;

        // Создаём новый список вопросов
        List<ExpertiseQuestion> newQuestions = questionTexts.stream()
                .map(text -> new ExpertiseQuestion(text, expertise))
                .collect(Collectors.toList());

        // Устанавливаем новый список
        expertise.setQuestions(newQuestions);
        return newQuestions;
    }
}