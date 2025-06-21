package com.example.mailnotification.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * Сервис для отправки электронной почты.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    /**
     * Хост SMTP-сервера.
     */
    @Value("${mail.host}")
    private String host;

    /**
     * Порт SMTP-сервера.
     */
    @Value("${mail.port}")
    private int port;

    /**
     * Имя пользователя для аутентификации на SMTP-сервере.
     */
    @Value("${mail.username}")
    private String username;

    /**
     * Пароль для аутентификации на SMTP-сервере.
     */
    @Value("${mail.password}")
    private String password;

    /**
     * Отправляет электронное письмо.
     *
     * @param to      адрес получателя
     * @param subject тема письма
     * @param content содержание письма
     */
    public void sendEmail(String to, String subject, String content) {

        Properties props = setProperties();
        Session session = createSession(props);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to)
            );
            message.setSubject(subject);
            message.setContent(content, "text/html; charset=utf-8");

            Transport.send(message);
            log.info("Email успешно отправлен на адрес: {}", to);
        } catch (AuthenticationFailedException e) {
            log.error("Ошибка аутентификации: проверь логин и пароль.", e);
            throw new RuntimeException("Ошибка при отправке email: ", e);
        } catch (MessagingException e) {
            log.error("Ошибка при отправке email", e);
            throw new RuntimeException("Ошибка при отправке email: ", e);
        }
    }

    /**
     * Устанавливает свойства для отправки электронной почты.
     *
     * @return свойства для отправки электронной почты
     */
    private Properties setProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        return props;
    }

    /**
     * Создает сессию для отправки электронной почты.
     *
     * @param props свойства для отправки электронной почты
     * @return сессия для отправки электронной почты
     */
    private Session createSession(Properties props) {
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }
}

