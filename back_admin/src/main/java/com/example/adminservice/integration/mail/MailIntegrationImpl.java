package com.example.adminservice.integration.mail;


import com.example.adminservice.config.AppConfig;
import com.example.adminservice.integration.IntegrationHelper;
import com.example.adminservice.integration.mail.dto.MailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Сервис для интеграции с сервисом отправки писем.
 */
@Service
public class MailIntegrationImpl implements MailIntegration{

    private static final Logger log = LoggerFactory.getLogger(MailIntegrationImpl.class);

    private final RestTemplate restTemplate;

    private final AppConfig appConfig;

    private final IntegrationHelper integrationHelper;

    /**
     * Конструктор класса MailIntegration.
     *
     */
    public MailIntegrationImpl(RestTemplate restTemplate,
                               AppConfig appConfig,
                               IntegrationHelper integrationHelper) {
        this.restTemplate = restTemplate;
        this.appConfig = appConfig;
        this.integrationHelper = integrationHelper;
    }

    /**
     * Отправляет email.
     *
     * @param mailRequest запрос на отправку письма
     */
    @Override
    public void sendMail(MailRequest mailRequest) {
        try {
            String mailApi = appConfig.getPaths().getNotification().get("send-mail");

            // Получаем заголовки с токеном авторизации
            HttpHeaders headers = integrationHelper.createAuthHeaders();

            // Создаем HTTP-запрос с заголовками и телом
            HttpEntity<MailRequest> requestEntity = new HttpEntity<>(mailRequest, headers);

            ResponseEntity<Void> response = restTemplate.exchange(mailApi, HttpMethod.POST, requestEntity, Void.class);

            checkResponseAnswer(response); // Проверяем ответ от сервиса отправки писем
            log.info("Mail sent successfully");
        } catch (Exception e) {
            log.error("Failed to send email to notification service: {}", mailRequest, e);
            throw new RuntimeException("Registration failed with send email to notification service: " + mailRequest, e);
        }
    }

    /**
     * Проверяет ответ от сервиса отправки писем.
     * Если ответ от сервиса отправки писем не 200, то выбрасывает исключение.
     *
     * @param response ответ от сервиса отправки писем
     */
    private void checkResponseAnswer(ResponseEntity<Void> response) {
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Error while checking response answer");
        }
    }


}


