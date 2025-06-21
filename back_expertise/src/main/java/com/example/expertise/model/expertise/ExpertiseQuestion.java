package com.example.expertise.model.expertise;

import com.example.expertise.model.checklist.ChecklistInstance;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

/**
 * Сущность, для представления вопроса в экспертизе.
 */
@Entity
@Table(name = "expertise_question")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExpertiseQuestion {

    /**
     * Уникальный идентификатор вопроса
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Текст вопроса
     */
    @Column(name = "question_text")
    private String questionText;

    /**
     * Ответ на вопрос
     */
    private String answer;

    /**
     * Список фотографий, связанных с ответом
     */
    @OneToMany(mappedBy = "expertiseQuestion", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<ExpertisePhoto> photos;

    /**
     * Вывод экспертного заключения на ответ
     */
    @Column(name = "answer_conclusion")
    private String answerConclusion;

    /**
     * Связь с экспертизой, к которой относится вопрос
     */
    @ManyToOne
    @JoinColumn(name = "expertise_id")
    @JsonBackReference
    private Expertise expertise;

    /**
     * Чек-лист
     */
    @OneToMany(mappedBy = "expertiseQuestion", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<ChecklistInstance> checklistInstances;

    // Custom constructors

    /**
     * Создает новый вопрос с заданным текстом и связанным с ним экпертизой.
     *
     * @param questionText текст вопроса
     * @param expertise    экспертиза, к которой относится вопрос
     */
    public ExpertiseQuestion(String questionText, Expertise expertise) {
        this.questionText = questionText;
        this.expertise = expertise;
    }

    @Override
    public String toString() {
        return "ExpertiseQuestion{" +
                "id=" + id +
                ", questionText='" + questionText + '\'' +
                ", answer='" + answer + '\'' +
                ", photos=" + photos +
                ", answerConclusion='" + answerConclusion + '\'' +
                '}';
    }
}

