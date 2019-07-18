package com.vinberts.shrinkly.service.impl;

import com.google.common.collect.Lists;
import com.timgroup.jgravatar.Gravatar;
import com.timgroup.jgravatar.GravatarDefaultImage;
import com.timgroup.jgravatar.GravatarRating;
import com.vinberts.shrinkly.persistence.dao.AbuseRepository;
import com.vinberts.shrinkly.persistence.dao.PasswordResetTokenRepository;
import com.vinberts.shrinkly.persistence.dao.RoleRepository;
import com.vinberts.shrinkly.persistence.dao.UserRepository;
import com.vinberts.shrinkly.persistence.dao.UserShortUrlRepository;
import com.vinberts.shrinkly.persistence.dao.VerificationTokenRepository;
import com.vinberts.shrinkly.persistence.model.PasswordResetToken;
import com.vinberts.shrinkly.persistence.model.User;
import com.vinberts.shrinkly.persistence.model.UserShortUrl;
import com.vinberts.shrinkly.persistence.model.VerificationToken;
import com.vinberts.shrinkly.repo.impl.RedisShortUrlRepository;
import com.vinberts.shrinkly.service.IUserService;
import com.vinberts.shrinkly.web.dto.UserDto;
import com.vinberts.shrinkly.web.errors.UserAlreadyExistException;
import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 *
 */
@Service
@Transactional
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private AbuseRepository abuseRepository;

    @Autowired
    private UserShortUrlRepository shortUrlRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordResetTokenRepository passwordTokenRepository;

    @Autowired
    private RedisShortUrlRepository redisShortUrlRepository;

    @Autowired
    private SessionRegistry sessionRegistry;

    public static final String TOKEN_INVALID = "invalidToken";
    public static final String TOKEN_EXPIRED = "expired";
    public static final String TOKEN_VALID = "valid";
    public static final String TOKEN_VALID_ALREADY_USED = "validAndUsed";

    @Override
    public User registerNewUserAccount(final UserDto accountDto) throws UserAlreadyExistException {
        if (emailExists(accountDto.getEmail())) {
            throw new UserAlreadyExistException("There is an account with that email address: " + accountDto.getEmail());
        }
        if (usernameExists(accountDto.getUsername())) {
            throw new UserAlreadyExistException("There is an account with that username: " + accountDto.getUsername());
        }
        final User user = new User();
        user.setUsername(accountDto.getUsername());
        user.setDateAdded(LocalDateTime.now());
        user.setPassword(passwordEncoder.encode(accountDto.getPassword()));
        user.setEmail(accountDto.getEmail());
        user.setSecret(Base32.random());
        user.setCredentialsNonExpired(true);
        user.setAccountNonLocked(true);
        user.setAccountNonExpired(true);
        Gravatar gravatar = new Gravatar(100, GravatarRating.GENERAL_AUDIENCES, GravatarDefaultImage.IDENTICON);
        String profileUrl = gravatar.getUrl(accountDto.getEmail());
        profileUrl = StringUtils.replaceOnceIgnoreCase(profileUrl, "http", "https");
        user.setProfileImage(profileUrl);
        user.setRoles(Arrays.asList(roleRepository.findByName("ROLE_USER")));
        return userRepository.save(user);
    }

    @Override
    public User getUser(final String verificationToken) {
        final VerificationToken token = tokenRepository.findByToken(verificationToken);
        if (token != null) {
            return token.getUser();
        }
        return null;
    }

    @Override
    public void saveRegisteredUser(final User user) {
        userRepository.save(user);
    }

    @Override
    public void deleteUser(final User user) {
        final VerificationToken verificationToken = tokenRepository.findByUser(user);

        if (verificationToken != null) {
            tokenRepository.delete(verificationToken);
        }

        final PasswordResetToken passwordToken = passwordTokenRepository.findByUser(user);

        if (passwordToken != null) {
            passwordTokenRepository.delete(passwordToken);
        }

        // delete user's custom short urls
        Collection<UserShortUrl> userShortUrls = shortUrlRepository.findAllByUserAndCustomIsTrue(user);

        for (UserShortUrl shortUrl: userShortUrls) {
            redisShortUrlRepository.removeShortCode(shortUrl.getShortUrl());
        }

        shortUrlRepository.deleteAllByUser(user);

        abuseRepository.deleteAllByUser(user);
        // finally delete the user object
        userRepository.delete(user);
    }

    @Override
    public void createVerificationTokenForUser(final User user, final String token) {
        final VerificationToken myToken = new VerificationToken(token, user);
        tokenRepository.save(myToken);
    }

    @Override
    public VerificationToken getVerificationToken(final String VerificationToken) {
        return tokenRepository.findByToken(VerificationToken);
    }

    @Override
    public void expireVerificationToken(final String token) {
        LocalDateTime expiry = LocalDateTime.now().minusHours(1);
        tokenRepository.setTokenAsExpired(token, expiry);
    }

    @Override
    public VerificationToken generateNewVerificationToken(final String existingVerificationToken) {
        VerificationToken vToken = tokenRepository.findByToken(existingVerificationToken);
        String newToken = new BigInteger(200, new SecureRandom()).toString(36);
        vToken.updateToken(newToken);
        vToken = tokenRepository.save(vToken);
        return vToken;
    }

    @Override
    public void createPasswordResetTokenForUser(final User user, final String token) {
        final PasswordResetToken myToken = new PasswordResetToken(token, user);
        passwordTokenRepository.save(myToken);
    }

    @Override
    public User findUserByEmail(final String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User findUserByUsername(final String username) {
        return userRepository.findUserByUsername(username);
    }

    @Override
    public PasswordResetToken getPasswordResetToken(final String token) {
        return passwordTokenRepository.findByToken(token);
    }

    @Override
    public User getUserByPasswordResetToken(final String token) {
        return passwordTokenRepository.findByToken(token)
                .getUser();
    }

    @Override
    public Optional<User> getUserByID(final long id) {
        return userRepository.findById(id);
    }

    @Override
    public void changeUserPassword(final User user, final String password) {
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    @Override
    public boolean checkIfValidOldPassword(final User user, final String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }

    @Override
    public String validateVerificationToken(final String token) {
        final VerificationToken verificationToken = tokenRepository.findByToken(token);
        if (verificationToken == null) {
            return TOKEN_INVALID;
        }

        final User user = verificationToken.getUser();
        final LocalDateTime now = LocalDateTime.now();


        final Long diffSec = Duration.between(now, verificationToken.getExpiryDate()).getSeconds();

        if (diffSec <= 0L) {
            if (user.isEnabled()) {
                tokenRepository.delete(verificationToken);
                return TOKEN_VALID_ALREADY_USED;
            } else {
                return TOKEN_EXPIRED;
            }
        }

        user.setEnabled(true);
        user.setAccountNonLocked(true);
        // tokenRepository.delete(verificationToken);
        userRepository.save(user);
        return TOKEN_VALID;
    }

    @Override
    public String generateQRUrl(final User user) throws UnsupportedEncodingException {
        return null;
    }

    @Override
    public User updateUser2FA(final boolean use2FA) {
        return null;
    }

    @Override
    public List<User> getUsersFromSessionRegistry() {
        //sessionRegistry.getSessionIds()
        List<?> users = sessionRegistry.getAllPrincipals();
        List<User> activeUsers = Lists.newArrayList();

        for (final Object principal : users) {
                // get user sessions that are not expired
                List<SessionInformation> activeUserSessions =
                        sessionRegistry.getAllSessions(principal, false); // Should not return null;

                if (!activeUserSessions.isEmpty()) {
                    // add active user
                    activeUsers.add((User) principal);
                }
        }

        return activeUsers;
    }

    private boolean emailExists(final String email) {
        return userRepository.findByEmail(email) != null;
    }

    private boolean usernameExists(final String username) {
        return userRepository.findUserByUsername(username) != null;
    }
}
