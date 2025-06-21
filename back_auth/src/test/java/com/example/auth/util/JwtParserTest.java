package com.example.auth.util;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtParserTest {

    @InjectMocks
    private JwtParser jwtParser;

    @Test
    void extractRoleFromToken() {
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"resource_access\":{\"my-client\":{\"roles\":[\"admin\"]}}}".getBytes());

        String token = "header." + payload + ".signature";

        String role = JwtParser.extractRoleFromToken(token, "my-client");

        assertEquals("admin", role);
    }
}