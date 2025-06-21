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
public class AppUserDTO {

    private UUID id;

    @JsonProperty("full_name")
    private String fullName;

    private String email;

    @JsonProperty("registration_date")
    private LocalDateTime registrationDate;

    private RoleDTO role;


}
