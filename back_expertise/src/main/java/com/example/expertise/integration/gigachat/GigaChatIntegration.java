package com.example.expertise.integration.gigachat;

/**
 * Интерфейс для работы с API GigaChat
 */
public interface GigaChatIntegration {

    /**
     * Отправить сообщение в чат (ИИ)
     *
     * @param message текст сообщения
     * @return сгенерированный ответ сервера
     */
    String sendMessage(String message);

}
