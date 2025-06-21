package com.example.auth.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProfileCreateDTO {

    @NotBlank(message = "номер телефона не может быть пустым")
    private String phone;

    @NotNull
    private LocationDTO locationDTO;

    @NotNull
    private Set<DirectionDTO> directionDTOList;

}
