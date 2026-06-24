package com.vinberts.shrinkly.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * An outstanding invitation to create an account. Holds the invitee's email and a
 * single-use token until the account is created on acceptance, at which point the
 * row is deleted. Expired rows are purged by TokensPurgeTask.
 */
@Entity
@Table(name = "app_invitation")
@Getter
@Setter
public class Invitation {

    private static final int EXPIRATION_MINUTES = 60 * 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "expiry_date", columnDefinition = "timestamp without time zone")
    private LocalDateTime expiryDate;

    @Column(name = "date_added", columnDefinition = "timestamp without time zone")
    private LocalDateTime dateAdded;

    public Invitation() {
    }

    public Invitation(final String email, final String token) {
        this.email = email;
        this.token = token;
        this.dateAdded = LocalDateTime.now();
        this.expiryDate = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
    }
}
