package com.example.expertise.model.checklist;

import com.example.expertise.model.expertise.ExpertiseQuestion;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность для представления экземпляра чек-листа.
 */
@Entity
@Table(name = "checklist_instance")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChecklistInstance {

    /**
     * Уникальный идентификатор экземпляра чек-листа
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Ссылка на вопрос экспертизы, к которому относится экземпляр чек-листа
     */
    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    @JsonBackReference
    @JsonProperty("expertise_question")
    private ExpertiseQuestion expertiseQuestion;

    @ManyToOne
    @JoinColumn(name = "checklist_template_id", nullable = false)
    @JsonBackReference
    @JsonProperty("checklist_template")
    private ChecklistTemplate checklistTemplate;

    /**
     * Заполненные данные экземпляра чек-листа в формате JSON
     */
    @Column(columnDefinition = "text", nullable = false)
    private String data;

    /**
     * Дата и время создания экземпляра чек-листа
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    /**
     * Дата и время последнего обновления экземпляра чек-листа
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
