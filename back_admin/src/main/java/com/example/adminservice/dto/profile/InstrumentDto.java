package com.example.adminservice.dto.profile;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Инструменты эксперта
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InstrumentDto {

    private String id;

    /**
     * Название инструмента
     */
    private String name;

    /**
     * Инвентарный номер инструмента
     */
    private String number;

    /**
     * Дата поверки инструмента
     */
    private LocalDate date;

}
