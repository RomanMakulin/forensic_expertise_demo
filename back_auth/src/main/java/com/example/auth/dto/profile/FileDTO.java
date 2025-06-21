package com.example.auth.dto.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FileDTO {

    /**
     * Идентификатор файла
     */
    private UUID id;

    /**
     * Имя файла
     */
    private String path;

    /**
     * Дата создания файла
     */
    @JsonProperty("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}
