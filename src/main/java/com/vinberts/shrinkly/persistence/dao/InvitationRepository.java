package com.vinberts.shrinkly.persistence.dao;

import com.vinberts.shrinkly.persistence.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 */
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    Invitation findByToken(String token);

    Invitation findFirstByEmailAndExpiryDateGreaterThan(String email, LocalDateTime now);

    List<Invitation> findAllByExpiryDateGreaterThanOrderByDateAddedDesc(LocalDateTime now);

    @Modifying
    @Query("delete from Invitation i where i.expiryDate <= ?1")
    void deleteAllExpiredSince(LocalDateTime now);
}
