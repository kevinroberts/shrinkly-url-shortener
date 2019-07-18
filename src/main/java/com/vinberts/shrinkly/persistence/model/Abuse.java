package com.vinberts.shrinkly.persistence.model;

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

    @OneToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(nullable = true, name = "user_id", foreignKey = @ForeignKey(name = "fk_app_abuse_user"))
    private User user;

}
