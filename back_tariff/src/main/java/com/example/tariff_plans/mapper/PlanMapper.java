package com.example.tariff_plans.mapper;

import com.example.tariff_plans.model.Plan;
import com.example.tariff_plans.model.PlanDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PlanMapper {

    PlanMapper INSTANCE = Mappers.getMapper(PlanMapper.class);

    @Mapping(source = "id", target = "id")  // Преобразование UUID в String
    @Mapping(source = "storageLimit", target = "storageLimit")
    @Mapping(source = "maxUsers", target = "maxUsers")
    @Mapping(source = "hasDocumentConstructor", target = "hasDocumentConstructor")
    @Mapping(source = "hasLegalDatabaseAccess", target = "hasLegalDatabaseAccess")
    @Mapping(source = "hasAdvancedLegalDatabase", target = "hasAdvancedLegalDatabase")
    @Mapping(source = "hasTemplates", target = "hasTemplates")
    @Mapping(source = "templatesCount", target = "templatesCount")
    @Mapping(source = "hasExpertSupport", target = "hasExpertSupport")
    @Mapping(source = "hasReviewFunctionality", target = "hasReviewFunctionality")
    @Mapping(source = "unlimitedDocuments", target = "unlimitedDocuments")
    @Mapping(source = "active", target = "active")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "maxDocuments", target = "maxDocuments")
    @Mapping(source = "additionalDocumentPrice", target = "additionalDocumentPrice")
    PlanDto planToPlanDto(Plan plan);
}

