package com.example.expertise.repository.expertise;

import com.example.expertise.model.expertise.Expertise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью Expertise.
 */
@Repository
public interface ExpertiseRepository extends JpaRepository<Expertise, UUID> {

    /**
     * Находит все экспертизы, созданные более двух суток назад.
     *
     * @return список экспертиз, которым более 48 часов
     */
    @Query(value = "SELECT * FROM expertise WHERE created_at <= CURRENT_TIMESTAMP - INTERVAL '2 days'", nativeQuery = true)
    List<Expertise> findAllOlderThanTwoDays();

    /**
     * Находит экспертизу по уникальному идентификатору эксперта.
     *
     * @param profileId уникальный идентификатор эксперта
     * @return найденная экспертиза или null, если экспертиза не найдена
     */
    List<Expertise> findAllByProfileId(UUID profileId);

    /**
     * Находит экспертизу по уникальному идентификатору экспертизы.
     *
     * @param expertiseId уникальный идентификатор экспертизы
     * @return найденная экспертиза или null, если экспертиза не найдена
     */
    Optional<Expertise> findById(UUID expertiseId);

    /**
     * Находит все экспертизы.
     *
     * @return список всех экспертиз
     */
    List<Expertise> findAll();

    /**
     * Удаляет экспертизу по уникальному идентификатору эксперта.
     *
     * @param profileId уникальный идентификатор эксперта
     */
    void deleteByProfileId(UUID profileId);

    /**
     * Удаляет экспертизу по уникальному идентификатору экспертизы через прямой SQL-запрос.
     *
     * @param expertiseId уникальный идентификатор экспертизы
     */
    @Modifying
    @Query(value = "DELETE FROM expertise WHERE id = :expertiseId", nativeQuery = true)
    void deleteByIdDirect(@Param("expertiseId") UUID expertiseId);
}
