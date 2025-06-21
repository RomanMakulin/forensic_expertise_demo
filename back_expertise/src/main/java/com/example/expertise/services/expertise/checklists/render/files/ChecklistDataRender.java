package com.example.expertise.services.expertise.checklists.render.files;

import com.example.expertise.dto.checklist.CreateChecklistInstanceDto;
import com.example.expertise.model.checklist.ChecklistInstance;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * Интерфейс для управления файлами чек-листов при его создании или редактировании.
 */
public interface ChecklistDataRender {

    /**
     * Загрузка файлов чек-листа
     *
     * @param instance чек-лист
     * @param request  запрос
     * @param token    токен пользователя
     */
    void updateInstanceWithFilesAndFields(ChecklistInstance instance, MultipartHttpServletRequest request, String token, CreateChecklistInstanceDto dto);

    /**
     * Обработка удаленных файлов чек-листа
     *
     * @param fileName имя файла
     * @param bucket   название хранилища
     * @param instance чек-лист
     */
    void processDeletedFiles(String fileName, String bucket, ChecklistInstance instance);
}

