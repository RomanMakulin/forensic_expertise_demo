package com.example.adminservice.controller;

import com.example.adminservice.dto.profile.ProfileDto;
import com.example.adminservice.dto.profileCancel.ProfileCancelFromFront;
import com.example.adminservice.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления профайлами
 */
@Tag(name = "Администратирование профилей", description = "Управление профайлами через панель администратора")
@RestController
@RequestMapping("/api/administration")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Возвращает список всех профилей
     *
     * @return список профилей
     */
    @Operation(summary = "Возвращает список всех профилей", description = "Возвращает список всех профилей",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список всех профилей"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректный запрос"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Ошибка сервера")
            })
    @GetMapping("/get-all-profiles")
    public ResponseEntity<List<ProfileDto>> getAllProfiles() {
        return ResponseEntity.ok(adminService.getAllProfiles());
    }

    /**
     * Возвращает список всех профилей, которые не прошли проверку администратором
     *
     * @return список профилей
     */
    @Operation(summary = "Возвращает список всех профилей, которые не прошли проверку администратором", description = "Возвращает список всех профилей, которые не прошли проверку администратором",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список всех профилей, которые не прошли проверку администратором"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректный запрос"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Ошибка сервера")
            })
    @GetMapping("/get-unverified-profiles")
    public ResponseEntity<List<ProfileDto>> getUnverifiedProfiles() {
        return ResponseEntity.ok(adminService.getUnverifiedProfiles());
    }

    /**
     * Подтверждает профиль пользователя
     *
     * @param profileId идентификатор профиля
     */
    @Operation(summary = "Подтверждает профиль пользователя", description = "Подтверждает профиль пользователя",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Подтверждение профиля пользователя"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректный запрос"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Ошибка сервера")
            })
    @GetMapping("/validate-profile/{profileId}")
    public ResponseEntity<Void> validateProfile(@PathVariable("profileId") String profileId) {
        adminService.verifyProfile(profileId);
        return ResponseEntity.ok().build();
    }

    /**
     * Отменяет подтверждение профиля пользователя
     *
     * @param profileCancelFromFront dto с неподходящими данными
     */
    @Operation(summary = "Отменяет подтверждение профиля пользователя", description = "Отправляет на сервер DTO с неподтвержденной информацией для удаление данных из БД",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Отмена подтверждения профиля пользователя"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Некорректный запрос"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Ошибка сервера")
            })
    @PostMapping("/cancel-validation")
    public ResponseEntity<Void> cancelValidationProfile(@Valid @RequestBody ProfileCancelFromFront profileCancelFromFront) {
        adminService.cancelValidationProfile(profileCancelFromFront);
        return ResponseEntity.ok().build();
    }

}
