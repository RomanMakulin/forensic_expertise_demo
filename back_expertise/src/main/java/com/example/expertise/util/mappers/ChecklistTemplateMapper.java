package com.example.expertise.util.mappers;

import com.example.expertise.dto.checklist.ChecklistTemplateInfoDto;
import com.example.expertise.model.checklist.ChecklistTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * Маппер шаблонов чек-листов
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ChecklistTemplateMapper {
    // Маппинг одной сущности в DTO
    @Mapping(source = "id", target = "checklistTemplateId")
    ChecklistTemplateInfoDto toDto(ChecklistTemplate template);

    // Обратный маппинг
    @Mapping(source = "checklistTemplateId", target = "id")
    ChecklistTemplate toEntity(ChecklistTemplateInfoDto dto);

    // Маппинг списка сущностей в список DTO
    List<ChecklistTemplateInfoDto> toDtoList(List<ChecklistTemplate> templates);

    // Обратный маппинг списка
    List<ChecklistTemplate> toEntityList(List<ChecklistTemplateInfoDto> dtos);
}
