package com.vinberts.shrinkly.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 */
@Entity
@Data
@Table(name = "app_user_short_urls")
public class UserShortUrl implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "short_url", nullable = false, length = 120)
    String shortUrl;

    @Column(name = "full_url", nullable = false, length = 2000)
    String fullUrl;

    @Column(name = "date_added", nullable = false, columnDefinition = "timestamp without time zone NOT NULL DEFAULT now()")
    LocalDateTime dateAdded;

    @Column(name = "expiration_date", columnDefinition = "timestamp without time zone")
    LocalDateTime expiryDate;

    @Column(name = "is_custom", columnDefinition = "boolean default false", nullable = false)
    private boolean custom;

    @Column(name = "clicks", nullable = false)
    private Long clicks;

    @Transient
    String shortCode;

    @JsonIgnore
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id", foreignKey = @ForeignKey(name = "fk_app_user_id_short_url"))
    private User user;

}
