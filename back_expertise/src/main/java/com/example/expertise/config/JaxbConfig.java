package com.example.expertise.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для работы с JAXB.
 */
@Configuration
public class JaxbConfig {

    /**
     * Инициализация JAXB.
     */
    @PostConstruct
    public void initJaxb() {
        System.setProperty("jakarta.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
    }
}