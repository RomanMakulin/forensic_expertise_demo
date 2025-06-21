package com.example.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Таблица для хранения токенов для сброса пароля
 */
@Entity
@Table(name="password_reset_token")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Токен для сброса пароля
     */
    @Column(nullable = false, unique = true)
    private String token;

    /**
     * Пользователь, для которого сброшен пароль
     */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Дата истечения срока действия токена
     */
    @Column(nullable = false)
    private LocalDateTime expiryDate;

    public PasswordResetToken(String token, User user, LocalDateTime expiryDate) {
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
    }
}
