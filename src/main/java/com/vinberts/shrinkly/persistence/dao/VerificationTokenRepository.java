package com.vinberts.shrinkly.persistence.dao;

import com.vinberts.shrinkly.persistence.model.User;
import com.vinberts.shrinkly.persistence.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.stream.Stream;

/**
 *
 */
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    VerificationToken findByToken(String token);

    VerificationToken findByUser(User user);

    Stream<VerificationToken> findAllByExpiryDateLessThan(LocalDateTime now);

    void deleteByExpiryDateLessThan(LocalDateTime now);

    void deleteByToken(String token);

    @Modifying
    @Query("UPDATE VerificationToken t SET t.expiryDate = ?2 where t.token = ?1")
    void setTokenAsExpired(String token, LocalDateTime expiry);

    @Modifying
    @Query("delete from VerificationToken t where t.expiryDate <= ?1")
    void deleteAllExpiredSince(LocalDateTime now);
}