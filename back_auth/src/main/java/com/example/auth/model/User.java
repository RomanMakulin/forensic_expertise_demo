package com.example.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity()
@Table(name="app_user")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name="full_name")
    private String fullName;

    private String email;

    @Column(name="registration_date")
    private LocalDateTime registrationDate;

    @Column(name="verification_email")
    private boolean verificationEmail;

    @OneToOne
    private Role role;

    @Column(name="keycloak_id")
    private String keycloakId;

    public User(String fullName, String email, LocalDateTime registrationDate, boolean verificationEmail, Role role, String keycloakId) {
        this.fullName = fullName;
        this.email = email;
        this.registrationDate = registrationDate;
        this.verificationEmail = verificationEmail;
        this.role = role;
        this.keycloakId = keycloakId;
    }
}
