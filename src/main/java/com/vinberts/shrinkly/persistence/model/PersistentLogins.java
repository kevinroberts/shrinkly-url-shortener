package com.vinberts.shrinkly.persistence.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 *
 */
@Entity
@Data
@Table(name = "persistent_logins")
public class PersistentLogins {

    @Id
    @Column(name = "series", length = 64, nullable = false)
    String series;

    @Column(name = "username", length = 64, nullable = false)
    String username;

    @Column(name = "token", length = 64, nullable = false)
    String token;

    @Column(name = "last_used", nullable = false, columnDefinition = "timestamp without time zone")
    LocalDateTime lastUsed;

}
