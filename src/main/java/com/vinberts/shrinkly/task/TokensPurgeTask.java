package com.vinberts.shrinkly.task;

import com.vinberts.shrinkly.persistence.dao.PasswordResetTokenRepository;
import com.vinberts.shrinkly.persistence.dao.VerificationTokenRepository;
import com.vinberts.shrinkly.service.IInvitationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 *
 */
@Slf4j
@Service
@Transactional
public class TokensPurgeTask {

    @Autowired
    VerificationTokenRepository tokenRepository;

    @Autowired
    PasswordResetTokenRepository passwordTokenRepository;

    @Autowired
    IInvitationService invitationService;

    @Scheduled(cron = "${purge.cron.expression}")
    public void purgeExpired() {

        LocalDateTime now = LocalDateTime.now();
        log.info("Running TokensPurgeTask for tokens older than {}", now);

        passwordTokenRepository.deleteAllExpiredSince(now);
        tokenRepository.deleteAllExpiredSince(now);
        invitationService.purgeExpired(now);
    }
}
