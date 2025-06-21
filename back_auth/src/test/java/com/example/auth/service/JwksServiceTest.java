package com.example.auth.service;

import com.example.auth.util.KeycloakConsts;
import com.nimbusds.jose.jwk.JWKSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwksServiceTest {

    @Mock
    private KeycloakConsts keycloakConsts;

    @InjectMocks
    private JwksService jwksService;

    @Test
    void loadJwks() throws IOException {
        when(keycloakConsts.getAuthServerUrlAdmin()).thenReturn("http://mock-server");
        when(keycloakConsts.getRealm()).thenReturn("mock-realm");

        // mock static method JWKSet.load(url)
        try (MockedStatic<JWKSet> jwkSetMockedStatic = Mockito.mockStatic(JWKSet.class)) {
            JWKSet mockSet = new JWKSet();
            jwkSetMockedStatic.when(() -> JWKSet.load(Mockito.any(URL.class)))
                    .thenReturn(mockSet);

            // when
            JWKSet result = jwksService.loadJwks();

            // then
            assertNotNull(result);
        }
    }
}