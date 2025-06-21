package com.example.adminservice.integration.minio;

import com.example.adminservice.config.AppConfig;
import com.example.adminservice.integration.IntegrationHelper;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * Реализация сервиса для работы с Minio
 */
@Validated
@Service
public class MinioIntegrationImpl implements MinioIntegration {

    private final IntegrationHelper integrationHelper;
    private final AppConfig appConfig;

    public MinioIntegrationImpl(IntegrationHelper integrationHelper, AppConfig appConfig) {
        this.integrationHelper = integrationHelper;
        this.appConfig = appConfig;
    }

    /**
     * Получить файл из микросервиса Minio.
     *
     * @param fileName      имя файла
     * @param fileExtension расширение файла
     * @param fileBucket    название бакета
     * @param link          флаг возвращаемого типа (true - ссылка, false - массив байтов)
     * @return ссылка на файл / массив байтов
     */
    @Override
    public <T> T getFileRequest(@NotNull(message = "fileName null") String fileName,
                                @NotNull(message = "fileExtension null") String fileExtension,
                                @NotNull(message = "fileBucket null") String fileBucket,
                                @NotNull(message = "link null") boolean link,
                                Class<T> responseType) {
        String requestUrl = integrationHelper.urlBuilder(appConfig.getPaths().getMinio()
                .get("get-file-by-params"), Map.of(
                "fileName", fileName,
                "fileExtension", fileExtension,
                "fileBucket", fileBucket,
                "link", String.valueOf(link)));
        return integrationHelper.sendRequest(requestUrl, HttpMethod.GET, null, responseType);
    }

    /**
     * Удалить файл из микросервиса Minio.
     *
     * @param fileName      название файла
     * @param fileExtension расширение файла
     * @param fileBucket    название бакета
     */
    @Override
    public void deleteFileRequest(@NotNull(message = "fileName null") String fileName,
                                  @NotNull(message = "fileExtension null") String fileExtension,
                                  @NotNull(message = "fileBucket null") String fileBucket) {
        String requestUrl = integrationHelper.urlBuilder(appConfig.getPaths().getMinio()
                .get("delete-file-by-params"), Map.of(
                "fileName", fileName,
                "fileExtension", fileExtension,
                "fileBucket", fileBucket));
        integrationHelper.sendRequest(requestUrl, HttpMethod.DELETE, null, Void.class);
    }
}
