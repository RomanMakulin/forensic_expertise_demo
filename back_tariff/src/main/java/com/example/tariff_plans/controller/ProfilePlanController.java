package com.example.tariff_plans.controller;

import com.example.tariff_plans.model.PlanDto;
import com.example.tariff_plans.service.ProfilePlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tariff/profile")
@Tag(name = "Profile Plan Controller", description = "API для управления тарифными планами профилей")
public class ProfilePlanController {

    private final ProfilePlanService profilePlanService;

    public ProfilePlanController(ProfilePlanService profilePlanService) {
        this.profilePlanService = profilePlanService;
    }

    /**
     * Получить список всех доступных тарифных планов.
     *
     * @return Список тарифных планов
     */
    @GetMapping
    @Operation(summary = "Получить список всех тарифных планов", description = "Возвращает список всех доступных тарифных планов.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список тарифных планов успешно получен"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<List<PlanDto>> getAllPlans() {
        List<PlanDto> plans = profilePlanService.getAllPlans();
        return ResponseEntity.ok(plans);
    }

    /**
     * Установить тарифный план для пользователя.
     *
     * @param userId ID пользователя
     * @param planId ID тарифа
     * @return ResponseEntity с кодом 200 при успешном обновлении
     */
    @PutMapping("/{userId}/select-plan/{planId}")
    @Operation(summary = "Выбрать тарифный план для пользователя", description = "Устанавливает тарифный план для указанного пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Тарифный план успешно выбран"),
            @ApiResponse(responseCode = "404", description = "Пользователь или тарифный план не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<Void> selectPlan(
            @Parameter(description = "ID пользователя", required = true) @PathVariable UUID userId,
            @Parameter(description = "ID тарифного плана", required = true) @PathVariable UUID planId) {
        profilePlanService.selectPlan(userId, planId);
        return ResponseEntity.ok().build();
    }

    /**
     * Отменить тарифный план для профиля.
     *
     * @param profileId ID профиля
     * @return ResponseEntity с кодом 200 при успешной отмене
     */
    @DeleteMapping("/cancel-plan/{profileId}")
    @Operation(summary = "Отменить тарифный план для профиля", description = "Отменяет тарифный план для указанного профиля.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Тарифный план успешно отменен"),
            @ApiResponse(responseCode = "404", description = "Профиль не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<Void> cancelPlan(
            @Parameter(description = "ID профиля", required = true) @PathVariable UUID profileId) {
        profilePlanService.cancelPlan(profileId);
        return ResponseEntity.ok().build();
    }
}
