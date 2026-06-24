package com.vinberts.shrinkly.persistence.model;

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
import java.time.LocalDateTime;

/**
 *
 */
@Entity
@Data
@Table(name = "app_abuse")
public class Abuse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_url", nullable = false, length = 120)
    String shortUrl;

    @Column(name = "full_url", nullable = false, length = 2000)
    String fullUrl;

    @Column(name = "ip_address", length = 120)
    String ipAddress;

    @Column(name = "date_added", nullable = false, columnDefinition = "timestamp without time zone NOT NULL DEFAULT now()")
    LocalDateTime dateAdded;

    @Column(name = "addressed", nullable = false, columnDefinition = "boolean DEFAULT false")
    Boolean addressed = false;

    @OneToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(nullable = true, name = "user_id", foreignKey = @ForeignKey(name = "fk_app_abuse_user"))
    private User user;

}
