package com.example.expertise.controller;

import com.example.expertise.dto.expertise.CreateExpertiseDto;
import com.example.expertise.dto.expertise.ExpertiseResponseDto;
import com.example.expertise.model.expertise.Expertise;
import com.example.expertise.services.expertise.ExpertiseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * API Контроллер для работы с экспертизами
 */
@RestController
@RequestMapping("/api/expertise")
public class ExpertiseController {

    /**
     * Сервис для работы с экспертизами
     */
    private final ExpertiseService expertiseService;

    public ExpertiseController(ExpertiseService expertiseService) {
        this.expertiseService = expertiseService;
    }

    /**
     * Создает новую экспертизу для указанного профиля
     *
     * @param createExpertiseDto данные для создания экспертизы (вводная часть)
     * @return 200 OK
     */
    @PostMapping("/create-expertise")
    public ResponseEntity<ExpertiseResponseDto> createExpertise(@RequestBody @Valid CreateExpertiseDto createExpertiseDto) {
        return ResponseEntity.ok(expertiseService.createNewExpertise(createExpertiseDto));
    }

    /**
     * Получает экспертизу по идентификатору
     *
     * @param expertiseId идентификатор экспертизы
     * @return 200 OK
     */
    @GetMapping("/get-expertise/{expertiseId}")
    public ResponseEntity<ExpertiseResponseDto> getExpertise(@PathVariable UUID expertiseId) {
        return ResponseEntity.ok(expertiseService.getExpertiseResponseDtoById(expertiseId));
    }

    /**
     * Получить список всех экспертиз по идентификатору пользователя
     *
     * @param profileId идентификатор пользователя
     * @return 200 OK
     */
    @GetMapping("/get-expertises-by-profile-id/{profileId}")
    public ResponseEntity<List<ExpertiseResponseDto>> getExpertisesByProfileId(@PathVariable UUID profileId) {
        return ResponseEntity.ok(expertiseService.getExpertiseResponseDtoByProfileId(profileId));
    }

    /**
     * Удаляет экспертизу по идентификатору
     *
     * @param expertiseId идентификатор экспертизы
     * @return 200 OK
     */
    @DeleteMapping("/delete-expertise/{expertiseId}")
    public ResponseEntity<Void> deleteExpertise(@PathVariable UUID expertiseId) {
        expertiseService.deleteExpertise(expertiseId);
        return ResponseEntity.ok().build();
    }

    /**
     * Генерирует финальный файл экспертизы
     *
     * @param expertiseId идентификатор экспертизы
     * @param mapScreen   скриншот карты объекта экспертизы
     * @return финальный файл экспертизы .docx
     */
    @PostMapping("/generate-final-expertise-file")
    public ResponseEntity<byte[]> generateFinalExpertiseFile(@RequestParam("expertise_id") String expertiseId,
                                                             @RequestPart("map_screen") MultipartFile mapScreen) {
        return ResponseEntity.ok(expertiseService.generateExpertiseFile(UUID.fromString(expertiseId), mapScreen));
    }
}
