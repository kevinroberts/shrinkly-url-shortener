package com.vinberts.shrinkly.service.impl;

import com.vinberts.shrinkly.persistence.dao.InvitationRepository;
import com.vinberts.shrinkly.persistence.model.Invitation;
import com.vinberts.shrinkly.persistence.model.User;
import com.vinberts.shrinkly.service.IInvitationService;
import com.vinberts.shrinkly.service.IUserService;
import com.vinberts.shrinkly.web.errors.InvitationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 *
 */
@Service
@Transactional
public class InvitationServiceImpl implements IInvitationService {

    private final InvitationRepository invitationRepository;
    private final IUserService userService;

    public InvitationServiceImpl(final InvitationRepository invitationRepository,
                                 final IUserService userService) {
        this.invitationRepository = invitationRepository;
        this.userService = userService;
    }

    @Override
    public Invitation createInvitation(final String email) {
        if (userService.findUserByEmail(email) != null) {
            throw new InvitationException("An account already exists for that email.");
        }
        if (invitationRepository.findFirstByEmailAndExpiryDateGreaterThan(email, LocalDateTime.now()) != null) {
            throw new InvitationException("An invitation is already pending for that email.");
        }
        final String token = new BigInteger(200, new SecureRandom()).toString(36);
        return invitationRepository.save(new Invitation(email, token));
    }

    @Override
    public Optional<Invitation> getValidInvitation(final String token) {
        final Invitation invitation = invitationRepository.findByToken(token);
        if (invitation == null || invitation.getExpiryDate().isBefore(LocalDateTime.now())) {
            return Optional.empty();
        }
        return Optional.of(invitation);
    }

    @Override
    public List<Invitation> getPendingInvitations() {
        return invitationRepository.findAllByExpiryDateGreaterThanOrderByDateAddedDesc(LocalDateTime.now());
    }

    @Override
    public void revokeInvitation(final Long id) {
        invitationRepository.deleteById(id);
    }

    @Override
    public User acceptInvitation(final String token, final String username, final String rawPassword) {
        final Invitation invitation = getValidInvitation(token)
                .orElseThrow(() -> new InvitationException("This invitation is invalid or has expired."));
        final User user = userService.createEnabledUser(invitation.getEmail(), username, rawPassword);
        invitationRepository.delete(invitation);
        return user;
    }

    @Override
    public void purgeExpired(final LocalDateTime now) {
        invitationRepository.deleteAllExpiredSince(now);
    }
}
