package com.vinberts.shrinkly.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
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

    // Click data persisted in global links table
    @Transient
    Long clicks;

    @Transient
    String shortCode;

    @JsonIgnore
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id", foreignKey = @ForeignKey(name = "fk_app_user_id_short_url"))
    private User user;

}
