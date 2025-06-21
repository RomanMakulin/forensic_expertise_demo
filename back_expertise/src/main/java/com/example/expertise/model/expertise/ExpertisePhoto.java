package com.example.expertise.model.expertise;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Сущность для представления фотографии, связанной с ответом на вопрос.
 */
@Entity
@Table(name = "expertise_photo")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExpertisePhoto {

    /**
     * Уникальный идентификатор фотографии
     */
    @Id
    private UUID id;

    /**
     * Путь к файлу фотографии в хранилище (например, MinIO)
     * Путь будет составной по принципу: expertiseId_questionId_photoId.jpg
     */
    @Column(name = "file_path")
    private String filePath;

    /**
     * Связь с вопросом, к которому относится фотография
     */
    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    @JsonBackReference
    private ExpertiseQuestion expertiseQuestion;

    @Override
    public String toString() {
        return "ExpertisePhoto{" +
                "id=" + id +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
