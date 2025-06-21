package com.example.expertise.controller;

import com.example.expertise.dto.checklist.ChecklistInstanceDto;
import com.example.expertise.dto.checklist.ChecklistTemplateInfoDto;
import com.example.expertise.dto.checklist.CreateChecklistInstanceDto;
import com.example.expertise.model.checklist.ChecklistTemplate;
import com.example.expertise.services.expertise.checklists.ChecklistsService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для работы с чек-листами и параметрами.
 */
@RestController
@RequestMapping("/api/expertise")
public class ChecklistController {

    private static final Logger log = LoggerFactory.getLogger(ChecklistController.class);
    private final ChecklistsService checklistsService;

    public ChecklistController(ChecklistsService checklistsService) {
        this.checklistsService = checklistsService;
    }

    /**
     * Получить список шаблонов чек-листов
     *
     * @return объекты с информацией о шаблонах чек-листов (id, name)
     */
    @GetMapping("/templates")
    public ResponseEntity<List<ChecklistTemplateInfoDto>> getTemplatesInfo() {
        return ResponseEntity.ok(checklistsService.getTemplatesInfo());
    }

    /**
     * Получить шаблон чек-листа по его идентификатору
     *
     * @param id идентификатор шаблона чек-листа
     * @return объект шаблона чек-листа
     */
    @GetMapping("/templates/{id}")
    public ResponseEntity<ChecklistTemplate> getTemplate(@PathVariable UUID id) {
        return ResponseEntity.ok(checklistsService.getTemplate(id));
    }

    /**
     * Создать экземпляр чек-листа на основе шаблона по его идентификатору
     *
     * @param instanceDto объект для создания экземпляра чек-листа
     * @param request     запрос с файлами (необязательный параметр)
     * @return созданный экземпляр чек-листа с данными (id, name)
     */
    @PostMapping(value = "/checklist-instance/submit", consumes = {"multipart/form-data"})
    public ResponseEntity<ChecklistInstanceDto> submitChecklistInstance(
            @RequestPart("payload") @Valid CreateChecklistInstanceDto instanceDto,
            MultipartHttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(checklistsService.submitChecklistInstance(instanceDto, request));
    }

    /**
     * Получить экземпляр чек-листа по его идентификатору
     *
     * @param id идентификатор экземпляра чек-листа
     * @return экземпляр чек-листа
     */
    @GetMapping("/checklist-instance/{id}")
    public ResponseEntity<ChecklistInstanceDto> getChecklistInstance(@PathVariable UUID id) {
        return ResponseEntity.ok(checklistsService.getChecklistInstance(id));
    }

    /**
     * Удалить файл из хранилища
     *
     * @param name       имя файла в хранилище minIO
     * @param bucket     название бакета в хранилище minIO (ключ из json)
     * @param instanceId идентификатор экземпляра чек-листа
     * @return сообщение об успешном удалении файла
     */
    @DeleteMapping("/checklist-instance/delete-files/{name}/{bucket}/{instance_id}")
    public ResponseEntity<ChecklistInstanceDto> deleteFile0(
            @PathVariable String name,
            @PathVariable String bucket,
            @PathVariable("instance_id") UUID instanceId) {
        return ResponseEntity.ok(checklistsService.deleteInstanceFile(name, bucket, instanceId));
    }

    /**
     * Удалить экземпляр чек-листа и связанные с ним файлы
     *
     * @param id    идентификатор экземпляра чек-листа
     * @return сообщение об успешном удалении экземпляра чек-листа и файлов
     */
    @DeleteMapping("/checklist-instance/{id}")
    public ResponseEntity<Void> deleteChecklistInstance(@PathVariable UUID id) {
        checklistsService.deleteChecklistInstanceById(id);
        return ResponseEntity.ok().build();
    }

}