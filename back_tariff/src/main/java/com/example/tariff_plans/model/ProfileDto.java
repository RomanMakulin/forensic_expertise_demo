package com.example.tariff_plans.model;

import lombok.Data;

import java.util.UUID;

@Data
public class ProfileDto {
    private UUID id;
    private UUID userId;
    private UUID planId;
    private String photo;
    private String template;
    private String passport;
    private String diplom;
    private String phone;
    private UUID locationId;
    private UUID statusId;
    private String planStartDate;
}
