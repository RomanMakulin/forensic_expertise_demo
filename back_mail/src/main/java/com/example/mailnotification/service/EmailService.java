package com.example.mailnotification.service;

/**
 * Интерфейс для отправки электронной почты.
 */
public interface EmailService {

    /**
     * Отправляет электронное письмо.
     *
     * @param to      адрес получателя
     * @param subject тема письма
     * @param content содержание письма
     */
    void sendEmail(String to, String subject, String content);
}

