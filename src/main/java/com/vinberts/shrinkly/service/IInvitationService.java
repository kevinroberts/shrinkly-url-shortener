package com.vinberts.shrinkly.service;

import com.vinberts.shrinkly.persistence.model.Invitation;
import com.vinberts.shrinkly.persistence.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 *
 */
public interface IInvitationService {

    Invitation createInvitation(String email);

    Optional<Invitation> getValidInvitation(String token);

    List<Invitation> getPendingInvitations();

    void revokeInvitation(Long id);

    User acceptInvitation(String token, String username, String rawPassword);

    void purgeExpired(LocalDateTime now);
}
