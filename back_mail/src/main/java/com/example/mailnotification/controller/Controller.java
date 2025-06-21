package com.example.mailnotification.controller;

import com.example.mailnotification.dto.SendRequest;
import com.example.mailnotification.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для отправки писем
 */
@Tag(name = "Почтовая рассылка", description = "Отправка писем")
@RestController
@RequestMapping("/api/notification")
public class Controller {

    /**
     * Сервис для отправки писем
     */
    private final EmailService emailService;

    public Controller(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Отправка письма
     *
     * @param sendRequest объект для отправки письма
     * @return статус отправки
     */
    @Operation(summary = "Отправка письма")
    @PostMapping("/send")
    public ResponseEntity<Void> send(@RequestBody SendRequest sendRequest) {
        emailService.sendEmail(sendRequest.getTo(), sendRequest.getSubject(), sendRequest.getBody());
        return ResponseEntity.ok().build();
    }


}
