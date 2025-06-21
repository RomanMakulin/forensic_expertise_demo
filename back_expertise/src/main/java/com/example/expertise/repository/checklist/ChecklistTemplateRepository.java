package com.example.expertise.repository.checklist;

import com.example.expertise.model.checklist.ChecklistTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью "Шаблон экспертизы".
 */
@Repository
public interface ChecklistTemplateRepository extends JpaRepository<ChecklistTemplate, UUID> {

    /**
     * Получает идентификатор шаблона чек-листа по его имени.
     *
     * @param name имя шаблона экспертизы
     * @return идентификатор шаблона экспертизы или пустой объект, если шаблон не найден
     */
    @Query("SELECT c.id FROM ChecklistTemplate c WHERE c.name = :name")
    Optional<UUID> getChecklistTemplateIdByName(@Param("name") String name);

}
