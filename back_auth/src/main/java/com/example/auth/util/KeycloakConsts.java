package com.example.auth.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class KeycloakConsts {

//    client realm settings

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String resource;

    @Value("${keycloak.credentials.secret}")
    private String secret;

//    admin:

    @Value("${keycloak.main.server-url}")
    private String authServerUrlAdmin;

    @Value("${keycloak.main.realm}")
    private String realmAdmin;

    @Value("${keycloak.main.clientId}")
    private String clientIdAdmin;

    @Value("${keycloak.main.username}")
    private String usernameAdmin;

    @Value("${keycloak.main.password}")
    private String passwordAdmin;

    // OAuth2
    @Value("${keycloak.oauth.redirect-uri}")
    private String redirectUri;

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getAuthServerUrl() {
        return authServerUrl;
    }

    public String getRealm() {
        return realm;
    }

    public String getResource() {
        return resource;
    }

    public String getSecret() {
        return secret;
    }

    public String getAuthServerUrlAdmin() {
        return authServerUrlAdmin;
    }

    public String getRealmAdmin() {
        return realmAdmin;
    }

    public String getClientIdAdmin() {
        return clientIdAdmin;
    }

    public String getUsernameAdmin() {
        return usernameAdmin;
    }

    public String getPasswordAdmin() {
        return passwordAdmin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeycloakConsts that = (KeycloakConsts) o;
        return Objects.equals(authServerUrl, that.authServerUrl) && Objects.equals(realm, that.realm) && Objects.equals(resource, that.resource) && Objects.equals(secret, that.secret) && Objects.equals(authServerUrlAdmin, that.authServerUrlAdmin) && Objects.equals(realmAdmin, that.realmAdmin) && Objects.equals(clientIdAdmin, that.clientIdAdmin) && Objects.equals(usernameAdmin, that.usernameAdmin) && Objects.equals(passwordAdmin, that.passwordAdmin) && Objects.equals(redirectUri, that.redirectUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authServerUrl, realm, resource, secret, authServerUrlAdmin, realmAdmin, clientIdAdmin, usernameAdmin, passwordAdmin, redirectUri);
    }
}
