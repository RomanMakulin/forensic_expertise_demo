package com.example.expertise.util.mappers;

import com.example.expertise.dto.checklist.ChecklistInstanceDto;
import com.example.expertise.dto.checklist.CreateChecklistInstanceDto;
import com.example.expertise.model.checklist.ChecklistInstance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.LinkedHashMap;
import java.util.Map;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ChecklistInstanceMapper {

    @Mapping(target = "data", expression = "java(toJson(dto.getData(), objectMapper))")
    ChecklistInstance toEntity(CreateChecklistInstanceDto dto, @Context ObjectMapper objectMapper);

    @Mapping(target = "questionId", source = "expertiseQuestion.id")
    @Mapping(target = "templateId", source = "checklistTemplate.id")
    @Mapping(target = "data", expression = "java(fromJson(entity.getData(), objectMapper))")
    ChecklistInstanceDto toDto(ChecklistInstance entity, @Context ObjectMapper objectMapper);

    default String toJson(Map<String, Object> data, @Context ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сериализации JSON", e);
        }
    }

    @SuppressWarnings("unchecked")
    default Map<String, Object> fromJson(String json, @Context ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(json, LinkedHashMap.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка десериализации JSON", e);
        }
    }
}

