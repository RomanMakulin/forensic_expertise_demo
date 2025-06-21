package com.example.tariff_plans.controller;


import com.example.tariff_plans.model.Plan;
import com.example.tariff_plans.service.AdminPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tariff/admin/")
@PreAuthorize("hasRole('ADMIN')") // Все методы доступны только администраторам
public class AdminTariffPlanController {


}