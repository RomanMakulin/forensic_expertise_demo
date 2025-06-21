package com.example.expertise.services.expertise.checklists.impl;

import com.example.expertise.dto.checklist.ChecklistInstanceDto;
import com.example.expertise.dto.checklist.ChecklistTemplateInfoDto;
import com.example.expertise.dto.checklist.CreateChecklistInstanceDto;
import com.example.expertise.integration.IntegrationHelper;
import com.example.expertise.integration.minio.MinioIntegration;
import com.example.expertise.model.checklist.ChecklistInstance;
import com.example.expertise.model.checklist.ChecklistTemplate;
import com.example.expertise.model.expertise.ExpertiseQuestion;
import com.example.expertise.repository.checklist.ChecklistInstanceRepository;
import com.example.expertise.services.cache.ChecklistTemplateCache;
import com.example.expertise.services.expertise.checklists.ChecklistTemplateService;
import com.example.expertise.services.expertise.checklists.ChecklistsService;
import com.example.expertise.services.expertise.checklists.render.files.ChecklistDataRender;
import com.example.expertise.services.expertise.checklists.render.files.ChecklistDataDispatcher;
import com.example.expertise.services.expertise.ExpertiseQuestionService;
import com.example.expertise.util.FilesDataUtils;
import com.example.expertise.util.mappers.ChecklistInstanceMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.*;

/**
 * Реализация сервиса для работы с чек-листами
 */
@Slf4j
@Service
@AllArgsConstructor
public class ChecklistsServiceImpl implements ChecklistsService {

    private final ChecklistInstanceRepository checklistInstanceRepository;
    private final ExpertiseQuestionService expertiseQuestionService;
    private final ChecklistTemplateCache checklistTemplateCache;
    private final ChecklistInstanceMapper checklistInstanceMapper;
    private final IntegrationHelper integrationHelper;
    private final ChecklistTemplateService checklistTemplateService;
    private final ObjectMapper objectMapper;
    private final ChecklistDataDispatcher fileProcessorDispatcher;
    private final MinioIntegration minioIntegration;

    @Override
    public List<ChecklistTemplateInfoDto> getTemplatesInfo() {
        return checklistTemplateService.getAllTemplatesInfo();
    }

    @Override
    public ChecklistTemplate getTemplate(@NotNull UUID id) {
        return checklistTemplateService.getTemplateById(id);
    }

    @Override
    @Transactional
    public ChecklistInstanceDto submitChecklistInstance(CreateChecklistInstanceDto dto, MultipartHttpServletRequest request) {
        ExpertiseQuestion question = expertiseQuestionService.getExpertiseQuestionById(dto.getQuestionId());
        ChecklistTemplate template = checklistTemplateService.getTemplateById(dto.getChecklistTemplateId());
        ChecklistInstance instance = findOrCreate(dto, question, template);

        applyChecklistData(request, template, instance, dto);

        return generateResponseDto(instance, template.getId());
    }

    /**
     * Генерация ответа, содержащего информацию для отрисовки фронта с актуальным данными + заголовками полей чек-листа.
     * Заголовки берутся из кэша и формируются в map (ключ - значение).
     *
     * @param instance   экземпляр чек-листа
     * @param templateId идентификатор шаблона чек-листа
     * @return ChecklistInstanceDto
     */
    private ChecklistInstanceDto generateResponseDto(ChecklistInstance instance, UUID templateId) {
        ChecklistInstanceDto responseDto = checklistInstanceMapper.toDto(instance, objectMapper);
        responseDto.setFieldNameCache(checklistTemplateCache.getFieldNameMap(templateId));
        return responseDto;
    }

    /**
     * Установка JSON данных чек-листа (файлы и поля)
     * Реализация зависит от процессора и шаблона чек-листа
     *
     * @param request  запрос
     * @param template шаблон чек-листа
     * @param instance экземпляр чек-листа
     * @param dto      DTO
     */
    private void applyChecklistData(MultipartHttpServletRequest request, ChecklistTemplate template, ChecklistInstance instance, CreateChecklistInstanceDto dto) {
        if (hasRealFiles(request)) {
            selectProcessor(request, template, instance, dto);
            log.info("Файлы успешно сохранены");
        } else {
            setInstanceDataFromDto(instance, dto);
            log.info("Файлы не были загружены - их нет");
        }
        checklistInstanceRepository.save(instance);
    }

    /**
     * Проверка, есть ли в запросе настоящие файлы (не только JSON)
     *
     * @param request запрос
     * @return true, если есть настоящие файлы, иначе false
     */
    private boolean hasRealFiles(MultipartHttpServletRequest request) {
        if (request == null) {
            return false;
        }

        return request.getMultiFileMap().values().stream()
                .flatMap(List::stream)
                .anyMatch(file ->
                        file != null
                                && !file.isEmpty()
                                && !"application/json".equalsIgnoreCase(file.getContentType()));
    }

    /**
     * Выбор нужного процессора файлов в зависимости от шаблона
     *
     * @param request  запрос
     * @param template шаблон чек-листа
     * @param instance экземпляр чек-листа
     */
    private void selectProcessor(MultipartHttpServletRequest request, ChecklistTemplate template, ChecklistInstance instance, CreateChecklistInstanceDto dto) {
        ChecklistDataRender processor = fileProcessorDispatcher.getProcessor(template.getName());
        if (processor != null) {
            String token = integrationHelper.getTokenFromRequest();
            processor.updateInstanceWithFilesAndFields(instance, request, token, dto);
        }
    }

    /**
     * Установка данных чек-листа из DTO
     *
     * @param instance экземпляр чек-листа
     * @param dto      DTO
     */
    private void setInstanceDataFromDto(ChecklistInstance instance, CreateChecklistInstanceDto dto) {
        try {
            log.info("instance до записи: {}", instance.getData());
            instance.setData(objectMapper.writeValueAsString(dto.getData()));
            log.info("instance после записи: {}", instance.getData());
        } catch (IOException e) {
            log.error("Ошибка при сохранении данных чек-листа", e);
            throw new RuntimeException("Ошибка при сохранении данных чек-листа", e);
        }
    }

    @Override
    public ChecklistInstanceDto getChecklistInstance(UUID id) {
        ChecklistInstance instance = getChecklistInstanceById(id);
        ChecklistInstanceDto dto = checklistInstanceMapper.toDto(instance, objectMapper);
        dto.setFieldNameCache(checklistTemplateCache.getFieldNameMap(instance.getChecklistTemplate().getId()));
        return dto;
    }

    @Override
    public List<ChecklistTemplate> getAllChecklistTemplates() {
        return checklistTemplateService.getAllTemplates();
    }

    @Override
    public ChecklistInstanceDto deleteInstanceFile(@NotNull String fileName, @NotNull String fileBucket, @NotNull UUID instanceId) {
        ChecklistInstance instance = getChecklistInstanceById(instanceId);

        // Выбираем нужный файловый процессор в зависимости от шаблона
        ChecklistDataRender processor = fileProcessorDispatcher.getProcessor(instance.getChecklistTemplate().getName());
        processor.processDeletedFiles(fileName, fileBucket, instance);

        checklistInstanceRepository.save(instance);
        return checklistInstanceMapper.toDto(instance, objectMapper);
    }

    /**
     * Получить чек-лист по ID
     *
     * @param id ID чек-листа
     * @return Экземпляр чек-листа
     */
    private ChecklistInstance getChecklistInstanceById(UUID id) {
        return checklistInstanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Экземпляр чек-листа по ID не найден"));
    }

    /**
     * Получить существующий или создать новый экземпляр чек-листа
     *
     * @param dto      DTO для создания чек-листа
     * @param question Вопрос, к которой привязан чек-лист
     * @param template Шаблон чек-листа
     * @return Экземпляр чек-листа
     */
    private ChecklistInstance findOrCreate(CreateChecklistInstanceDto dto, ExpertiseQuestion question, ChecklistTemplate template) {
        return checklistInstanceRepository
                .findByExpertiseQuestionIdAndChecklistTemplateId(question.getId(), template.getId())
                .orElseGet(() -> {
                    ChecklistInstance newInstance = checklistInstanceMapper.toEntity(dto, objectMapper);
                    newInstance.setExpertiseQuestion(question);
                    newInstance.setChecklistTemplate(template);
                    return checklistInstanceRepository.save(newInstance);
                });
    }

    @Override
    @Transactional
    public void deleteChecklistInstanceById(@NotNull UUID id) {
        try {
            // Извлекаем поле data из найденного чек-листа
            String jsonString = getChecklistInstanceById(id).getData(); // строка JSON из БД
            Map<String, Object> data = objectMapper.readValue(jsonString, new TypeReference<>() {
            });

            // Собираем все файлы в формате bucket -> List<fileNames>
            Map<String, List<String>> filesToDelete = new HashMap<>();
            FilesDataUtils.collectFilesFromData(data, filesToDelete);

            // Удаляем файлы из MinIO
            filesToDelete.forEach((bucket, fileNames) -> {
                for (String fileName : fileNames) minioIntegration.deleteFileByFullName(bucket, fileName);
            });

            // Удаляем сам экземпляр
            checklistInstanceRepository.deleteById(id);
            log.info("Экземпляр чек-листа с ID {} успешно удален", id);

        } catch (Exception e) {
            log.error("Ошибка при удалении чек-листа с ID {}", id, e);
        }
    }

}
