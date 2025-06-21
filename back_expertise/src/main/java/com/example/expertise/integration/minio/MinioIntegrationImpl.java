package com.example.expertise.integration.minio;

import com.example.expertise.config.AppConfig;
import com.example.expertise.integration.IntegrationHelper;
import com.example.expertise.util.FileUploadUtil;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис интеграции с MinIO
 */
@Component
public class MinioIntegrationImpl implements MinioIntegration {

    private static final Logger log = LoggerFactory.getLogger(MinioIntegrationImpl.class);

    private final AppConfig appConfig;
    private final IntegrationHelper integrationHelper;

    public MinioIntegrationImpl(AppConfig appConfig,
                                IntegrationHelper integrationHelper) {
        this.appConfig = appConfig;
        this.integrationHelper = integrationHelper;
    }

    /**
     * Получить файл экспертизы пользователя из MinIO (интеграция)
     *
     * @param templateName название шаблона экспертизы
     * @return InputStreamResource с файлом экспертизы пользователя
     */
    @Override
    public InputStreamResource getExpertiseFile(String templateName) throws IOException {

        String baseUrl = appConfig.getPaths().getMinio().get("get-expertise-file") + "/" + templateName;

        HttpHeaders headers = integrationHelper.createAuthHeaders(null);
        headers.setAccept(List.of(MediaType.APPLICATION_OCTET_STREAM));
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Resource> response = integrationHelper.executeRequest(
                baseUrl,
                HttpMethod.GET,
                requestEntity,
                Resource.class,
                "Ошибка получения шаблона экспертизы по названию. templateName:" + templateName);

        Resource resource = response.getBody();
        if (resource == null) {
            log.error("Пустой ответ для templateName: {}", templateName);
            throw new RuntimeException("Пустой ответ для templateName: " + templateName);
        }

        log.info("Файл экспертизы получен успешно для templateName: {}", templateName);
        return new InputStreamResource(resource.getInputStream());
    }

    /**
     * Получить фото ответа экспертизы из сервиса minio (интеграция)
     *
     * @param photoPath путь к фотографии
     * @return файл в виде байтов
     */
    @Override
    public byte[] getExpertisePhotoAsBytes(String photoPath) {
        String baseUrl = appConfig.getPaths().getMinio().get("get-expertise-answer-photo-file") + "/" + photoPath;

        HttpHeaders headers = integrationHelper.createAuthHeaders(null);
        headers.setAccept(List.of(MediaType.IMAGE_JPEG));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> response = integrationHelper.executeRequest(
                baseUrl,
                HttpMethod.GET,
                requestEntity,
                byte[].class,
                "Ошибка получения фотографии ответа экспертизы. photoPath: " + photoPath);
        return response.getBody();
    }

    /**
     * Получить файл пользователя из MinIO
     *
     * @param fileName      название файла
     * @param fileExtension расширение файла
     * @param fileBucket    название бакета
     * @return файл пользователя в виде байтов
     */
    @Override
    public byte[] getFileByParams(@NotNull(message = "fileName null") String fileName,
                                  @NotNull(message = "fileExtension null") String fileExtension,
                                  @NotNull(message = "fileBucket null") String fileBucket) {
        String requestUrl = integrationHelper.urlBuilder(appConfig.getPaths().getMinio()
                        .get("get-file-by-params"), Map.of(
                        "fileName", fileName,
                        "fileExtension", fileExtension,
                        "fileBucket", fileBucket,
                        "link", "false"),
                false // отключаем кодировку
        );

        HttpHeaders headers = integrationHelper.createAuthHeaders(null);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> response = integrationHelper.executeRequest(
                requestUrl,
                HttpMethod.GET,
                requestEntity,
                byte[].class,
                "Ошибка при получении файла документа профиля для. fileName: " + fileName + ", fileExtension: " + fileExtension + ", fileBucket: " + fileBucket);
        return response.getBody();
    }

    /**
     * Загрузить фотографию ответа экспертизы в MinIO (интеграция)
     *
     * @param photoName название фотографии
     * @param photo     фото
     */
    @Override
    public void uploadAnswerPhoto(String photoName, MultipartFile photo) {
        String baseUrl = appConfig.getPaths().getMinio().get("upload-expertise-answer-photo");

        HttpHeaders headers = integrationHelper.createAuthHeaders(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = getMultiValueMap(photoName, photo);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        integrationHelper.executeRequest(
                baseUrl,
                HttpMethod.POST,
                requestEntity,
                Void.class,
                "Ошибка при загрузке фотографии ответа экспертизы в minIO. photoName: " + photoName);
        log.info("Photo for expertise answer uploaded successfully: {}", photoName);
    }

    @Override
    public String uploadFileByParams(String fileName, String fileExtension, String fileBucket, MultipartFile file) {
        String baseUrl = appConfig.getPaths().getMinio().get("upload-file-by-params");

        HttpHeaders headers = integrationHelper.createAuthHeaders(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("fileName", fileName);
        body.add("fileExtension", fileExtension);
        body.add("fileBucket", fileBucket);
        body.add("file", FileUploadUtil.toInputStreamResource(file));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        return integrationHelper.executeRequest(
                baseUrl,
                HttpMethod.POST,
                requestEntity,
                String.class,
                "Ошибка загрузки файла в MinIO по параметрам. fileName: " + fileName).getBody();
    }

    @Override
    public String uploadFileByParams(String fileName, String fileExtension, String fileBucket, MultipartFile file, String token) {
        String baseUrl = appConfig.getPaths().getMinio().get("upload-file-by-params");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Bearer " + token);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("fileName", fileName);
        body.add("fileExtension", fileExtension);
        body.add("fileBucket", fileBucket);
        body.add("file", FileUploadUtil.toInputStreamResource(file));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        return integrationHelper.executeRequest(
                baseUrl,
                HttpMethod.POST,
                requestEntity,
                String.class,
                "Ошибка загрузки файла в MinIO по параметрам. fileName: " + fileName
        ).getBody();
    }

    @Override
    public void deleteFileByParams(String fileName, String fileExtension, String fileBucket) {
        String url = integrationHelper.urlBuilder(
                appConfig.getPaths().getMinio().get("delete-file-by-params"),
                Map.of(
                        "fileName", fileName,
                        "fileExtension", fileExtension,
                        "fileBucket", fileBucket
                ),
                false // отключим кодировку
        );

        HttpEntity<Void> request = new HttpEntity<>(integrationHelper.createAuthHeaders(null));

        integrationHelper.executeRequest(
                url,
                HttpMethod.DELETE,
                request,
                Void.class,
                "Ошибка удаления файла из MinIO по параметрам. fileName: " + fileName
        );

        log.info("Файл успешно удален из MinIO: {}.{} (bucket: {})", fileName, fileExtension, fileBucket);
    }

    @Override
    public void deleteFileByFullName(String bucket, String fileName) {
        String[] fileNameParts = splitFileName(fileName);
        deleteFileByParams(fileNameParts[0], fileNameParts[1], bucket);
    }

    /**
     * Удалить все фотографии ответов экспертизы пользователя из MinIO (интеграция)
     *
     * @param expertiseId id пользователя
     */
    @Override
    public void deleteAllAnswerPhotos(UUID expertiseId) {
        String baseUrl = appConfig.getPaths().getMinio().get("delete-expertise-answer-photos") + "/" + expertiseId;
        requestForDelete(baseUrl);
        log.info("Deleted all answer photos for profileId: {}", expertiseId);
    }

    /**
     * Удалить фотографию ответа экспертизы в MinIO (интеграция)
     *
     * @param filePath путь к файлу в хранилище
     */
    @Override
    public void deleteAnswerPhoto(String filePath) {
        String baseUrl = appConfig.getPaths().getMinio().get("delete-expertise-answer-photo") + "/" + filePath;
        requestForDelete(baseUrl);
        log.info("Deleted answer photo: {}", filePath);
    }

    /**
     * Общая логика запроса на удаление фото или фотографий
     *
     * @param baseUrl базовый URL запроса
     */
    private void requestForDelete(String baseUrl) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        integrationHelper.executeRequest(baseUrl, HttpMethod.DELETE, requestEntity, Void.class, "Ошибка при удалении фото ответа экспертизы");
    }

    /**
     * Создаем MultiValueMap (body) для загрузки фотографии
     *
     * @param photoName название фотографии
     * @param photo     фото
     * @return MultiValueMap для загрузки фотографии
     */
    private MultiValueMap<String, Object> getMultiValueMap(String photoName, MultipartFile photo) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("photoName", photoName);

        try {
            InputStreamResource resource = new InputStreamResource(photo.getInputStream()) {
                @Override
                public String getFilename() {
                    return photoName;
                }

                @Override
                public long contentLength() {
                    return photo.getSize();
                }
            };
            body.add("photo", resource);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file input stream: " + photoName, e);
        }
        return body;
    }

    /**
     * Получить имя файла без расширения и расширение
     *
     * @param fileName полное имя файла
     * @return массив из имени файла и расширения
     */
    private String[] splitFileName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return new String[]{fileName, ""}; // нет расширения
        }
        String name = fileName.substring(0, dotIndex);
        String extension = "." + fileName.substring(dotIndex + 1);
        return new String[]{name, extension};
    }

}
