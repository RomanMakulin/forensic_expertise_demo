package com.example.tariff_plans.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Plan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "storage_limit", nullable = false)
    private Integer storageLimit;

    @Column(name = "max_users", nullable = false)
    private Integer maxUsers = 1;

    @Column(name = "max_documents")
    private Integer maxDocuments;

    @Column(name = "additional_document_price", precision = 10, scale = 2)
    private BigDecimal additionalDocumentPrice;

    @Column(name = "has_document_constructor", nullable = false)
    private Boolean hasDocumentConstructor = false;

    @Column(name = "has_legal_database_access", nullable = false)
    private Boolean hasLegalDatabaseAccess = false;

    @Column(name = "has_advanced_legal_database", nullable = false)
    private Boolean hasAdvancedLegalDatabase = false;

    @Column(name = "has_templates", nullable = false)
    private Boolean hasTemplates = false;

    @Column(name = "templates_count", nullable = false)
    private Integer templatesCount = 0;

    @Column(name = "has_expert_support", nullable = false)
    private Boolean hasExpertSupport = false;

    @Column(name = "has_review_functionality", nullable = false)
    private Boolean hasReviewFunctionality = false;

    @Column(name = "max_reviews")
    private Integer maxReviews;

    @Column(name = "unlimited_documents", nullable = false)
    private Boolean unlimitedDocuments = false;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
