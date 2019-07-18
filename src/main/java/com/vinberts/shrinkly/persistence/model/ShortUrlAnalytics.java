package com.vinberts.shrinkly.persistence.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 *
 */
@Entity
@Data
@Table(name = "short_url_analytics")
public class ShortUrlAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "short_url", nullable = false, length = 120)
    String shortUrl;

    @Column(name = "browser", length = 255)
    String browser;

    @Column(name = "ip_address", nullable = false, length = 120)
    String ipAddress;

    @Column(name = "operating_system", length = 120)
    String operatingSystem;

    @Column(name = "operating_system_version", length = 15)
    String operatingSystemVersion;

    @Column(name = "country_alpha_two", length = 3)
    String countryCode;

    @Column(name = "us_state_ansi", length = 3)
    String usState;

    @Column(name = "referrer", length = 300)
    String referrer;

    @Column(name = "continent_name", length = 30)
    String continentName;

    @Column(name = "browser_type", length = 40)
    String browserType;

    @Column(name = "browser_version", length = 20)
    String browserVersion;

    @Column(name = "device_type", length = 20)
    String deviceType;

    @Column(name = "date_added", columnDefinition = "timestamp without time zone NOT NULL DEFAULT now()")
    private LocalDateTime dateAdded;

    @Column(name = "expiry_date", columnDefinition = "timestamp without time zone")
    LocalDateTime expiryDate;

}
