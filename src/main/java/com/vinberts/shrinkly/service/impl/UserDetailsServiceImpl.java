package com.vinberts.shrinkly.service.impl;

import com.vinberts.shrinkly.persistence.dao.UserRepository;
import com.vinberts.shrinkly.persistence.model.Privilege;
import com.vinberts.shrinkly.persistence.model.Role;
import com.vinberts.shrinkly.persistence.model.User;
import com.vinberts.shrinkly.security.LoginAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.vinberts.shrinkly.utils.WebUtil.getClientIP;

/**
 * UserDetailsServiceImpl
 *
 * Custom user details service
 * maps Database user model to a
 * Spring security user
 */
@Service
@Transactional
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {

        final String ip = getClientIP(request);
        // has this use attempted to login with bad credentials too many times
        if (loginAttemptService.isBlocked(ip)) {
            throw new RuntimeException("blocked");
        }

        try {
            User user = this.userRepository.findUserByUsername(username);

            if (user == null) {
                log.warn("Username not found: " + username);
                throw new UsernameNotFoundException("User " + username + " was not found in the database");
            }

            log.debug("Found user: " + user);

            user.setAuthorities((Collection<GrantedAuthority>) getAuthorities(user.getRoles()));

            return user;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private Collection<? extends GrantedAuthority> getAuthorities(final Collection<Role> roles) {
        return getGrantedAuthorities(roles);
    }

//    private final List<String> getPrivileges(final Collection<Role> roles) {
//        final List<String> privileges = new ArrayList<>();
//        final List<Privilege> collection = new ArrayList<>();
//        for (final Role role : roles) {
//            collection.addAll(role.getPrivileges());
//        }
//        for (final Privilege item : collection) {
//            privileges.add(item.getName());
//        }
//
//        return privileges;
//    }

    /**
     * Provides simplified method of Role based authentication
     * to add additional layer of authentication, uncomment the
     * getPrivileges based method.
     * @param roles
     * @return
     */
    private List<GrantedAuthority> getGrantedAuthorities(final Collection<Role> roles) {
        final List<GrantedAuthority> authorities = new ArrayList<>();
        for (final Role role : roles) {

            authorities.add(new SimpleGrantedAuthority(role.getName()));

            final Collection<Privilege> privileges = role.getPrivileges();

            for (Privilege privilege: privileges) {
                authorities.add(new SimpleGrantedAuthority(privilege.getName()));
            }
        }
        return authorities;
    }

}
