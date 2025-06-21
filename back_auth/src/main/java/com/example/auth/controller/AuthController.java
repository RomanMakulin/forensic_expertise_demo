package com.example.auth.controller;

import com.example.auth.config.AppConfig;
import com.example.auth.dto.auth.*;
import com.example.auth.service.auth.AuthService;
import com.example.auth.service.auth.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.UUID;

/**
 * Контроллер для регистрации и аутентификации пользователей.
 */
@Tag(name = "Аутентификация", description = "API для регистрации, входа и верификации пользователей")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final LoginService loginService;
    private final AppConfig appConfig;

    public AuthController(AuthService authService, LoginService loginService, AppConfig appConfig) {
        this.authService = authService;
        this.loginService = loginService;
        this.appConfig = appConfig;
    }

    /**
     * Регистрация нового пользователя.
     *
     * @param request Запрос на регистрацию
     * @return 200 OK, если регистрация успешна
     */
    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Позволяет зарегистрировать нового пользователя, передав его данные в теле запроса.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных")
            }
    )
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegistrationRequest request) {
        authService.register(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Аутентификация пользователя.
     *
     * @param request Запрос с учетными данными пользователя
     * @return Данные аутентифицированного пользователя
     */
    @Operation(
            summary = "Вход пользователя",
            description = "Позволяет пользователю войти в систему, передав учетные данные.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Аутентификация успешна"),
                    @ApiResponse(responseCode = "401", description = "Ошибка аутентификации")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<AuthUserDetailsResponse> login(@Valid @RequestBody LoginRequest request,
                                                         HttpServletResponse response) {
        AuthUserDetailsResponse authResponse = authService.login(request);

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/refresh")
                .maxAge(21600) // 6 часов = 6 * 60 * 60 = 21600 секунд
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return ResponseEntity.ok(authResponse);
    }


    /**
     * Разлогирование пользователя
     *
     * @param response объект HttpServletResponse для добавления headers
     * @return статус 200 (OK) при успешном выполнении
     */
    @Operation(
            summary = "Выход из системы",
            description = "Завершает сеанс пользователя, удаляя refresh token cookie",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный выход из системы"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
            }
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/refresh")
                .maxAge(0) // Устанавливаем срок жизни 0 - cookie будет удалена
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return ResponseEntity.ok().build();
    }

    /**
     * Подтверждение email пользователя.
     *
     * @param userId Уникальный идентификатор пользователя
     * @return Перенаправление на страницу входа после подтверждения
     */
    @Operation(
            summary = "Подтверждение email",
            description = "Верифицирует email пользователя по уникальному идентификатору и перенаправляет на страницу входа.",
            responses = {
                    @ApiResponse(responseCode = "302", description = "Перенаправление на страницу входа"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
            }
    )
    @GetMapping("/verify-email/{userId}")
    public RedirectView verifyEmail(
            @Parameter(description = "UUID пользователя для подтверждения email")
            @PathVariable UUID userId) {
        System.out.println("test");
        authService.verifyRegistration(userId);
        String redirectUrl = appConfig.getPaths().getFrontend().get("login");
        return new RedirectView(redirectUrl);
    }

    /**
     * API для получения нового access токена по refreshToken
     *
     * @param refreshToken refreshToken пользователя из httpOnly cookie
     * @return новая информации авторизации по юзеру
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthUserDetailsResponse> refreshToken(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {

        AuthUserDetailsResponse authResponse = loginService.loginByRefreshToken(refreshToken);

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/refresh")
                .maxAge(21600) // 6 часов = 6 * 60 * 60 = 21600 секунд
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return ResponseEntity.ok(authResponse);
    }

    /**
     * API для обработки входа через соц.сеть ВКонтакте.
     * Получает код авторизации от ВКонтакте и обрабатывает его для получения информации об авторизации.
     * Возвращает информацию об авторизации пользователя (токены доступа и идентификатор пользователя).
     *
     * @param code Код авторизации от ВКонтакте
     * @return Информация об авторизации пользователя (токены доступа и идентификатор пользователя)
     */
    @GetMapping("/vk/callback")
    public ResponseEntity<AuthUserDetailsResponse> handleVkCallback(@RequestParam("code") String code) {
        if (code.isBlank()) throw new IllegalArgumentException("Authorization code is missing or empty");
        return ResponseEntity.ok(authService.handleVkCallback(code));
    }
}
