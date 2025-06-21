package com.example.expertise.model.expertise;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Сущность для представления судьи в экспертизе.
 */
@Entity
@Table(name = "expertise_judge")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExpertiseJudge {

    /**
     * Уникальный идентификатор судьи
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * ФИО судьи
     */
    @Column(name = "full_name")
    private String fullName;

    @ManyToOne
    @JoinColumn(name = "expertise_id", nullable = false)
    @JsonBackReference
    private Expertise expertise;

    // Custom constructors

    /**
     * Конструктор для создания нового судьи.
     *
     * @param fullName  ФИО судьи
     * @param expertise Экспертиза, к которой относится судья
     */
    public ExpertiseJudge(String fullName, Expertise expertise) {
        this.fullName = fullName;
        this.expertise = expertise;
    }

    @Override
    public String toString() {
        return "ExpertiseJudge{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}
