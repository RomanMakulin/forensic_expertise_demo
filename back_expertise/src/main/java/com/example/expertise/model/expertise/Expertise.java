package com.example.expertise.model.expertise;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Сущность для представления экспертизы.
 */
@Entity
@Table(name = "expertise")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Expertise {

    /**
     * Уникальный идентификатор экспертизы
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * ID профиля эксперта
     */
    @Column(name = "profile_id")
    @NotNull(message = "ID профиля не может быть пустым")
    private UUID profileId;

    /**
     * Имя шаблона экспертизы, без расширения (без .docx)
     */
    @Column(name = "template_name")
    @NotNull(message = "ID шаблона не может быть пустым")
    private String templateName;

    /**
     * Специализация экспертизы
     */
    private String speciality;

    /**
     * Номер_дела
     */
    @Column(name = "case_number")
    private String caseNumber;

    /**
     * Наименование_экспертизы_какой
     */
    private String name;

    /**
     * Определение_от_число
     */
    @Column(name = "ruling_date")
    private LocalDate rulingDate;

    /**
     * Суд_какого
     */
    @Column(name = "court_name")
    private String courtName;

    /**
     * Дата_начала_экспертизы
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * Дата_окончания_экспертизы
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Дата_подписки_минус_2_дня_от_даты_начал
     */
    @Column(name = "sign_date")
    private LocalDate signDate;

    /**
     * Председательствующего_судьи
     */
    @Column(name = "presiding_judge")
    private String presidingJudge;

    /**
     * Судей
     */
    @OneToMany(mappedBy = "expertise", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<ExpertiseJudge> expertiseJudges;

    /**
     * По_иску
     */
    private String plaintiff;

    /**
     * Месторасположения_объекта_экспертизы
     */
    private String location;

    /**
     * Сколько_томов
     */
    @Column(name = "volume_count")
    private String volumeCount;

    /**
     * Присутствующие_при_осмотре
     */
    private String participants;

    /**
     * Дата_и_время_осмотра
     */
    @Column(name = "inspection_date_time")
    private LocalDateTime inspectionDateTime;

    /**
     * Все_вопросы
     */
    @OneToMany(mappedBy = "expertise", cascade = CascadeType.ALL)
    @JsonManagedReference
    @OrderColumn(name = "question_order") // Колонка для хранения порядка
    private List<ExpertiseQuestion> questions;

    /**
     * Дата создания экспертизы
     */
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    // Custom constructors


    // Getters and setters

    /**
     * При установке даты начала экспертизы автоматически выставляется дата подписания (2 дня до даты начала экспертизы)
     *
     * @param startDate - дата начала экспертизы
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        if (startDate != null) {
            this.signDate = startDate.minusDays(2);
        }
    }

    @Override
    public String toString() {
        return "Expertise{" +
                "id=" + id +
                ", profileId=" + profileId +
                ", templateName='" + templateName + '\'' +
                ", caseNumber='" + caseNumber + '\'' +
                ", name='" + name + '\'' +
                ", speciality='" + speciality + '\'' +
                ", rulingDate=" + rulingDate +
                ", courtName='" + courtName + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", signDate=" + signDate +
                ", presidingJudge='" + presidingJudge + '\'' +
                ", expertiseJudges=" + expertiseJudges +
                ", plaintiff='" + plaintiff + '\'' +
                ", location='" + location + '\'' +
                ", volumeCount='" + volumeCount + '\'' +
                ", participants='" + participants + '\'' +
                ", inspectionDateTime=" + inspectionDateTime +
                ", questions=" + questions +
                ", createdAt=" + createdAt +
                '}';
    }
}
