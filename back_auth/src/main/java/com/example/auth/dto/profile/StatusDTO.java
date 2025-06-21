package com.example.auth.dto.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StatusDTO {

    private String name = "Создан";

    @JsonProperty("verification_result")
    private String verificationResult;

    @JsonProperty("activity_status")
    private String activityStatus;

}
