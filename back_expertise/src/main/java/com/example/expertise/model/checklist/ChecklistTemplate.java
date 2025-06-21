package com.example.expertise.model.checklist;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Сущность для представления шаблона чек-листа.
 */
@Entity
@Table(name = "checklist_template")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChecklistTemplate {

    /**
     * Уникальный идентификатор шаблона чек-листа
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Наименование шаблона чек-листа
     */
    @Column(nullable = false)
    private String name;

    /**
     * Структура шаблона в формате JSONB
     */
    @Column(columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> structure;

    /**
     * Дата и время создания записи
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления записи
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
