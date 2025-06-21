package com.example.tariff_plans.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PlanDto {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private int storageLimit;
    private int maxUsers;
    private Integer maxDocuments;
    private BigDecimal additionalDocumentPrice;
    private boolean hasDocumentConstructor;
    private boolean hasLegalDatabaseAccess;
    private boolean hasAdvancedLegalDatabase;
    private boolean hasTemplates;
    private int templatesCount;
    private boolean hasExpertSupport;
    private boolean hasReviewFunctionality;
    private Integer maxReviews;
    private boolean unlimitedDocuments;
    private boolean active;
}