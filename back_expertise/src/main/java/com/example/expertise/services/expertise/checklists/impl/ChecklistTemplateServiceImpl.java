package com.example.expertise.services.expertise.checklists.impl;

import com.example.expertise.dto.checklist.ChecklistTemplateInfoDto;
import com.example.expertise.model.checklist.ChecklistTemplate;
import com.example.expertise.repository.checklist.ChecklistTemplateRepository;
import com.example.expertise.services.expertise.checklists.ChecklistTemplateService;
import com.example.expertise.util.mappers.ChecklistTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Реализация сервиса для работы с шаблонами чек-листов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChecklistTemplateServiceImpl implements ChecklistTemplateService {

    private final ChecklistTemplateRepository checklistTemplateRepository;
    private final ChecklistTemplateMapper checklistTemplateMapper;

    /**
     * Возвращает список информации о всех шаблонах чек-листов.
     *
     * @return список информации о шаблонах чек-листов
     */
    @Override
    public List<ChecklistTemplateInfoDto> getAllTemplatesInfo() {
        List<ChecklistTemplate> templates = checklistTemplateRepository.findAll();
        try {
            return checklistTemplateMapper.toDtoList(templates);
        } catch (Exception e) {
            log.error("Ошибка при маппинге шаблонов чек-листов", e);
            throw new RuntimeException("Ошибка при маппинге шаблонов чек-листов", e);
        }
    }

    /**
     * Возвращает список всех шаблонов чек-листов.
     *
     * @return список шаблонов чек-листов
     */
    @Override
    public List<ChecklistTemplate> getAllTemplates() {
        return checklistTemplateRepository.findAll();
    }

    /**
     * Возвращает шаблон чек-листа по его ID.
     *
     * @param id ID шаблона чек-листа
     * @return шаблон чек-листа
     * @throws RuntimeException если шаблон чек-листа по ID не найден
     */
    @Override
    public ChecklistTemplate getTemplateById(UUID id) {
        return checklistTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Шаблон чек-листа по ID не найден"));
    }

    @Override
    public UUID getTemplateIdByName(String name) {
        return checklistTemplateRepository.getChecklistTemplateIdByName(name)
                .orElseThrow(() -> new RuntimeException("Шаблон чек-листа по имени не найден"));
    }

}
