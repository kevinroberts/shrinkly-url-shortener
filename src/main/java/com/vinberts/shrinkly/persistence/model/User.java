package com.vinberts.shrinkly.persistence.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.Collection;

@Entity
@Getter
@Setter
@Table(name = "app_user",
        uniqueConstraints = {
        @UniqueConstraint(name = "app_user_uk", columnNames = "user_name") })
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name", length = 36, nullable = false, unique = true)
    private String username;

    @Column(name = "password", length = 128, nullable = false)
    private String password;

    @Column(name = "enabled", columnDefinition = "boolean default false", nullable = false)
    private boolean enabled;

    @Column(name = "user_email", length = 254, nullable = false, unique = true)
    private String email;

    @Column(name = "profile_image", length = 1024)
    private String profileImage;

    @Column(name = "date_added", columnDefinition = "timestamp without time zone NOT NULL DEFAULT now()")
    private LocalDateTime dateAdded;

    @Column(name = "last_login", columnDefinition = "timestamp without time zone")
    private LocalDateTime lastLogin;

    @Column(name = "is_using_2fa", columnDefinition = "boolean default false", nullable = false)
    private boolean isUsing2FA;

    @Column(name = "secret")
    private String secret;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id", columnDefinition = "bigint"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id", columnDefinition = "bigint"))
    private Collection<Role> roles;

    @Transient
    private Collection<GrantedAuthority> authorities;

    @Column(name = "credentials_non_expired")
    private boolean credentialsNonExpired;

    @Column(name = "account_non_expired")
    private boolean accountNonExpired;

    @Column(name = "account_non_locked")
    private boolean accountNonLocked;

    /**
     * Returns {@code true} if the supplied object is a {@code User} instance with the
     * same {@code username} value.
     * <p>
     * In other words, the objects are equal if they have the same username, representing
     * the same principal. Username(s) are considered unique
     */
    @Override
    public boolean equals(Object rhs) {
        if (rhs instanceof User) {
            return username.equals(((User) rhs).getUsername());
        }
        return false;
    }

    /**
     * Returns the hashcode of the {@code username}.
     */
    @Override
    public int hashCode() {
        return username.hashCode();
    }

}
