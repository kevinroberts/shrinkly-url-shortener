package com.vinberts.shrinkly.service.impl;

import com.vinberts.shrinkly.persistence.dao.InvitationRepository;
import com.vinberts.shrinkly.persistence.model.Invitation;
import com.vinberts.shrinkly.persistence.model.User;
import com.vinberts.shrinkly.service.IUserService;
import com.vinberts.shrinkly.web.errors.InvitationException;
import com.vinberts.shrinkly.web.errors.UserAlreadyExistException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InvitationServiceImplTest {

    private final InvitationRepository repo = mock(InvitationRepository.class);
    private final IUserService userService = mock(IUserService.class);
    private final InvitationServiceImpl service = new InvitationServiceImpl(repo, userService);

    @Test
    void createInvitationRejectsExistingUser() {
        when(userService.findUserByEmail("a@b.com")).thenReturn(new User());
        assertThrows(InvitationException.class, () -> service.createInvitation("a@b.com"));
        verify(repo, never()).save(any());
    }

    @Test
    void createInvitationRejectsExistingPendingInvitation() {
        when(userService.findUserByEmail("a@b.com")).thenReturn(null);
        when(repo.findFirstByEmailAndExpiryDateGreaterThan(eq("a@b.com"), any()))
                .thenReturn(new Invitation("a@b.com", "existing"));
        assertThrows(InvitationException.class, () -> service.createInvitation("a@b.com"));
        verify(repo, never()).save(any());
    }

    @Test
    void createInvitationSavesAndReturnsWithToken() {
        when(userService.findUserByEmail("a@b.com")).thenReturn(null);
        when(repo.findFirstByEmailAndExpiryDateGreaterThan(eq("a@b.com"), any())).thenReturn(null);
        when(repo.save(any(Invitation.class))).thenAnswer(returnsFirstArg());

        Invitation result = service.createInvitation("a@b.com");

        assertEquals("a@b.com", result.getEmail());
        assertTrue(result.getToken() != null && !result.getToken().isEmpty());
        verify(repo).save(any(Invitation.class));
    }

    @Test
    void getValidInvitationEmptyWhenExpired() {
        Invitation expired = new Invitation("a@b.com", "tok");
        expired.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        when(repo.findByToken("tok")).thenReturn(expired);
        assertTrue(service.getValidInvitation("tok").isEmpty());
    }

    @Test
    void getValidInvitationPresentWhenLive() {
        when(repo.findByToken("tok")).thenReturn(new Invitation("a@b.com", "tok"));
        assertTrue(service.getValidInvitation("tok").isPresent());
    }

    @Test
    void getValidInvitationEmptyWhenMissing() {
        when(repo.findByToken("nope")).thenReturn(null);
        assertTrue(service.getValidInvitation("nope").isEmpty());
    }

    @Test
    void acceptInvitationCreatesEnabledUserAndDeletesInvitation() {
        Invitation live = new Invitation("a@b.com", "tok");
        when(repo.findByToken("tok")).thenReturn(live);
        User created = new User();
        when(userService.createEnabledUser("a@b.com", "newuser", "pw")).thenReturn(created);

        User result = service.acceptInvitation("tok", "newuser", "pw");

        assertSame(created, result);
        verify(repo).delete(live);
    }

    @Test
    void acceptInvitationThrowsOnInvalidToken() {
        when(repo.findByToken("bad")).thenReturn(null);
        assertThrows(InvitationException.class, () -> service.acceptInvitation("bad", "u", "p"));
        verify(userService, never()).createEnabledUser(any(), any(), any());
    }

    @Test
    void acceptInvitationPropagatesUsernameTakenAndKeepsInvitation() {
        Invitation live = new Invitation("a@b.com", "tok");
        when(repo.findByToken("tok")).thenReturn(live);
        when(userService.createEnabledUser(any(), any(), any()))
                .thenThrow(new UserAlreadyExistException("username taken"));

        assertThrows(UserAlreadyExistException.class, () -> service.acceptInvitation("tok", "u", "p"));
        verify(repo, never()).delete(any());
    }

    @Test
    void revokeInvitationDeletesById() {
        service.revokeInvitation(5L);
        verify(repo).deleteById(5L);
    }
}
