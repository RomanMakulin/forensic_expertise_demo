package com.example.expertise.repository.expertise;

import com.example.expertise.model.expertise.ExpertisePhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью фотографии экспертизы.
 */
@Repository
public interface ExpertisePhotoRepository extends JpaRepository<ExpertisePhoto, UUID> {

    /**
     * Поиск фотографии ответа экспертизы по названию фото
     *
     * @param filePath название фото
     * @return найденная фотография ответа экспертизы или null, если фотография ответа экспертизы не найдена
     */
    Optional<ExpertisePhoto> findExpertisePhotoByFilePath(String filePath);

}
