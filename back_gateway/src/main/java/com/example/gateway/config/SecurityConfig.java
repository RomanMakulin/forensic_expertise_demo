package com.example.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Конфигурация безопасности для приложения.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Создает и возвращает цепочку фильтров безопасности.
     *
     * @param http объект ServerHttpSecurity для настройки безопасности
     * @return цепочка фильтров безопасности
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, CorsConfigurationSource corsConfigurationSource) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Отключаем защиту от CSRF
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .pathMatchers("/api/auth/**").permitAll()
                        .pathMatchers("/api/public-notification/**").permitAll()
                        .pathMatchers("/api/profile/create-empty", "/api/profile/create-empty/**").permitAll()
                        .pathMatchers("/api/files/delete-expertise-answer-photos/**").permitAll()
                        .pathMatchers("/api/administration/**").hasRole("ADMIN")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess == null || !resourceAccess.containsKey("afe_app")) return List.of();

            Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get("afe_app");
            List<String> roles = (List<String>) clientAccess.get("roles");

            return roles.stream()
                    .map(role -> "ROLE_" + role.toUpperCase())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        });

        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}
