package com.example.adminservice.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IntegrationHelperTest {

    @Test
    void createAuthHeaders() {
        // given
        Jwt jwt = Jwt.withTokenValue("mocked-token").header("alg", "none").claim("sub", "user").build();
        Authentication auth = new JwtAuthenticationToken(jwt);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        IntegrationHelper helper = new IntegrationHelper(null);

        // when
        HttpHeaders headers = helper.createAuthHeaders();

        // then
        assertEquals("Bearer mocked-token", headers.getFirst("Authorization"));
    }

    @Test
    void urlBuilder() {
        IntegrationHelper helper = new IntegrationHelper(null); // restTemplate не нужен

        Map<String, String> params = Map.of("q", "roma", "sort", "asc");
        String result = helper.urlBuilder("https://example.com/search", params);

        assertTrue(result.contains("q=roma"));
        assertTrue(result.contains("sort=asc"));
        assertTrue(result.startsWith("https://example.com/search"));
    }
}