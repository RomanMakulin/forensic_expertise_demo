package com.example.expertise.services.expertise.checklists;

import com.example.expertise.dto.checklist.ChecklistInstanceDto;
import com.example.expertise.dto.checklist.ChecklistTemplateInfoDto;
import com.example.expertise.dto.checklist.CreateChecklistInstanceDto;
import com.example.expertise.model.checklist.ChecklistTemplate;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс сервиса чек-листов.
 */
public interface ChecklistsService {
    /**
     * Получить информацию о всех чек-листах.
     *
     * @return список чек-листов
     */
    List<ChecklistTemplateInfoDto> getTemplatesInfo();

    /**
     * Получить шаблон чек-листа.
     *
     * @param id идентификатор шаблона чек-листа
     * @return шаблон чек-листа
     */
    ChecklistTemplate getTemplate(@NotNull UUID id);

    /**
     * Создать экземпляр чек-листа.
     *
     * @param instanceDto данные для создания экземпляра чек-листа
     * @param request     HTTP-запрос с файлами (необязательный)
     * @return экземпляр чек-листа
     */
    ChecklistInstanceDto submitChecklistInstance(CreateChecklistInstanceDto instanceDto, MultipartHttpServletRequest request);

    /**
     * Получить экземпляр чек-листа.
     *
     * @param id идентификатор экземпляра чек-листа
     * @return экземпляр чек-листа
     */
    ChecklistInstanceDto getChecklistInstance(UUID id);

    /**
     * Получить все шаблоны чек-листов (из базы данных)
     *
     * @return список шаблонов чек-листов
     */
    List<ChecklistTemplate> getAllChecklistTemplates();

    /**
     * Удалить файл из хранилища
     *
     * @param fileName   имя файла в хранилище minIO
     * @param fileBucket название бакета в хранилище minIO (ключ из json)
     * @param instanceId идентификатор экземпляра чек-листа
     */
    ChecklistInstanceDto deleteInstanceFile(@NotNull String fileName, @NotNull String fileBucket, @NotNull UUID instanceId);

    /**
     * Удалить экземпляр чек-листа и всех его файлов из хранилища
     *
     * @param id            идентификатор экземпляра чек-листа
     */
    void deleteChecklistInstanceById(@NotNull UUID id);
}
