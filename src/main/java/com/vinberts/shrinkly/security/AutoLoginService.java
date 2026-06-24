package com.vinberts.shrinkly.security;

import com.vinberts.shrinkly.persistence.model.User;
import com.vinberts.shrinkly.service.impl.UserDetailsServiceImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Logs a user in programmatically (no password) and persists the security context
 * to the session, so the authentication survives the redirect. Shared by the
 * registration-confirm and invitation-accept flows.
 */
@Service
public class AutoLoginService {

    private final UserDetailsServiceImpl userDetailsService;
    // Spring Security 6+ requires programmatic SecurityContext changes to be saved explicitly.
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AutoLoginService(final UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public void loginWithoutPassword(final User user, final HttpServletRequest request, final HttpServletResponse response) {
        final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        final Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        final SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }
}
