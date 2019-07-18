package com.vinberts.shrinkly.security;

import com.vinberts.shrinkly.persistence.dao.UserRepository;
import com.vinberts.shrinkly.persistence.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;

import static com.vinberts.shrinkly.utils.WebUtil.getClientIP;

/**
 * CustomLoginAuthenticationSuccessHandler
 * Implements custom post authentication logic
 */
@Component("myAuthenticationSuccessHandler")
@Slf4j
public class CustomLoginAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private LoginAttemptService loginAttemptService;

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) throws IOException, ServletException {

        loginAttemptService.loginSucceeded(getClientIP(request));

        redirectStrategy.sendRedirect(request, response, "/?user=" + authentication.getName());
        final HttpSession session = request.getSession(false);

        if (session != null) {
            session.setMaxInactiveInterval(30 * 60);
            if (authentication.getPrincipal() instanceof User) {
                User loggedUser = (User) authentication.getPrincipal();
                loggedUser.setLastLogin(LocalDateTime.now());
                userRepository.save(loggedUser);
            } else {
                User loggedUser = userRepository.findUserByUsername(authentication.getName());
                loggedUser.setLastLogin(LocalDateTime.now());
                userRepository.save(loggedUser);
            }
        }
    }

    public void setRedirectStrategy(final RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
    }

    protected RedirectStrategy getRedirectStrategy() {
        return redirectStrategy;
    }
}
