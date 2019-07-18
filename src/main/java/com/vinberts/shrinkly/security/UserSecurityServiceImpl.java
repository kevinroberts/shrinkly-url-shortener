package com.vinberts.shrinkly.security;

import com.vinberts.shrinkly.persistence.dao.PasswordResetTokenRepository;
import com.vinberts.shrinkly.persistence.model.PasswordResetToken;
import com.vinberts.shrinkly.persistence.model.User;
import com.vinberts.shrinkly.service.impl.UserDetailsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

/**
 *
 */
@Slf4j
@Service
@Transactional
public class UserSecurityServiceImpl implements ISecurityUserService {

    @Autowired
    private PasswordResetTokenRepository passwordTokenRepository;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;


    @Override
    public void removePasswordResetToken(final String token) {
        final PasswordResetToken passToken = passwordTokenRepository.findByToken(token);
        if (passToken != null) {
            passwordTokenRepository.delete(passToken);
        }
    }

    @Override
    public Optional<String> validatePasswordResetToken(final long id, final String token) {
        final PasswordResetToken passToken = passwordTokenRepository.findByToken(token);
        if ((passToken == null) || (passToken.getUser().getUserId() != id)) {
            log.debug("Got invalid password token " + token);
            return Optional.of("invalidToken");
        }

        log.debug("Got password token for user " + passToken.getUser().getUsername());

        final LocalDateTime now = LocalDateTime.now();

        final Long diffSec = Duration.between(now, passToken.getExpiryDate()).getSeconds();

        if (diffSec <= 0L) {
            log.debug("Password token expired " + passToken.getExpiryDate());
            return Optional.of("expired");
        }

        final User user = passToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                Arrays.asList(new SimpleGrantedAuthority("CHANGE_PASSWORD_PRIVILEGE")));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return Optional.empty();
    }
}
