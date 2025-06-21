package com.example.minioservice.controller;


import com.example.minioservice.service.ProfileManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Контроллер работы с файлами пользователя через minIO
 */
@Tag(name = "Получение файлов", description = "Получение файлов пользователя")
@RestController
@RequestMapping("/api/files")
public class ManageController {

    private final ProfileManageService profileManageService;

    public ManageController(ProfileManageService profileManageService) {
        this.profileManageService = profileManageService;
    }

    /**
     * API для загрузки разных типов файла (дипломы, шаблоны, сертификаты, переподготовка и т.д.)
     *
     * @param fileName      имя файла (profileId_fileId или же profileId)
     * @param fileExtension расширение файла (.docx, .pdf)
     * @param fileBucket    название бакета - папки, в который сохраняется файл
     * @param file          файл
     * @return ссылка на файл
     */
    @Operation(summary = "Сохранение файла пользователя по параметрам. Например, диплом, шаблон, сертификат и т.д.")
    @PostMapping("/upload-file-by-params")
    public ResponseEntity<String> uploadFileByParams(@RequestParam("fileName") String fileName,
                                                     @RequestParam("fileExtension") String fileExtension,
                                                     @RequestParam("fileBucket") String fileBucket,
                                                     @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(profileManageService.uploadFileByParams(fileName, fileExtension, fileBucket, file));
    }

    /**
     * API для получения разных типов файла (дипломы, шаблоны, сертификаты, переподготовка и т.д.)
     *
     * @param fileName      название файла
     * @param fileExtension расширение файла (.docx, .pdf)
     * @param fileBucket    бакет, в котором находится файл (папка)
     * @param isLink        флаг, указывающий на то, нужно ли вернуть ссылку на файл или сам файл в byte[]
     * @return ссылка на файл
     */
    @Operation(summary = "Получение файла по параметрам. Например: диплом, сертификат, шаблон, и т.д")
    @GetMapping("/get-file-by-params")
    public ResponseEntity<?> getFileLinkByParams(@RequestParam("fileName") String fileName,
                                                 @RequestParam("fileExtension") String fileExtension,
                                                 @RequestParam("fileBucket") String fileBucket,
                                                 @RequestParam("link") boolean isLink) {
        if (isLink) return ResponseEntity.ok(profileManageService.getFileLinkByParams(fileName, fileExtension, fileBucket));
        return ResponseEntity.ok(profileManageService.getFileByteByParams(fileName, fileExtension, fileBucket));
    }

    /**
     * Удаление абстрактного файла пользователя по заданным параметрам (шаблон, паспорт, фото и т.д.)
     *
     * @param fileName      имя файла
     * @param fileExtension расширение файла (например: .jpg, .png, .pdf, .txt и т.д.)
     * @param fileBucket    бакет, в котором лежит файл (папка)
     */
    @Operation(summary = "Удаление файла пользователя по заданным параметрам")
    @DeleteMapping("/delete-file-by-params")
    public ResponseEntity<Void> deleteFileByParam(@RequestParam("fileName") String fileName,
                                                  @RequestParam("fileExtension") String fileExtension,
                                                  @RequestParam("fileBucket") String fileBucket) {
        profileManageService.deleteFileByParam(fileName, fileExtension, fileBucket);
        return ResponseEntity.ok().build();
    }

}
