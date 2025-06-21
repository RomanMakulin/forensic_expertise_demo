package com.example.expertise.services.expertise.checklists;

import com.example.expertise.dto.checklist.ChecklistTemplateInfoDto;
import com.example.expertise.model.checklist.ChecklistTemplate;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с шаблонами чек-листов
 */
public interface ChecklistTemplateService {

    /**
     * Получение информации о всех шаблонах чек-листов
     *
     * @return список информации о шаблонах чек-листов (урезанный объект)
     */
    List<ChecklistTemplateInfoDto> getAllTemplatesInfo();

    /**
     * Получить всех шаблоны чек-листов
     *
     * @return список шаблонов чек-листов
     */
    List<ChecklistTemplate> getAllTemplates();

    /**
     * Получение объекта шаблона чек-листа по его уникальному идентификатору
     *
     * @param id уникальный идентификатор шаблона чек-листа
     * @return объект шаблона чек-листа
     */
    ChecklistTemplate getTemplateById(UUID id);

    /**
     * Получить уникальный идентификатор шаблона чек-листа по его названию
     *
     * @param name название шаблона чек-листа
     * @return уникальный идентификатор шаблона чек-листа
     */
    UUID getTemplateIdByName(String name);
}
