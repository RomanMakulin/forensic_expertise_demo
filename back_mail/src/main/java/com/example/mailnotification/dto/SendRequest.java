package com.example.mailnotification.dto;

/**
 * Объект для отправки письма
 */
public class SendRequest {

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

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }
}
