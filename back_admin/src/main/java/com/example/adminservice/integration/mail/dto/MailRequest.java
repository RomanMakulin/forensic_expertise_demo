package com.example.adminservice.integration.mail.dto;

import lombok.*;

/**
 * Объект для отправки письма
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MailRequest {

    /**
     * Адрес получателя
     */
    private String to;

    /**
     * Тема письма
     */
    private String subject;

    /**
     * Содержание письма
     */
    private String body;

}
