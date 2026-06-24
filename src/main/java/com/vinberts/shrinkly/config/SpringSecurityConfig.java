package com.vinberts.shrinkly.config;

import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import javax.sql.DataSource;

/**
 *
 */
@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

    // roles admin allow to access /admin/**
    // roles user allow to access /user/**
    // custom 403 access denied handler
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http,
                                                   final AccessDeniedHandler accessDeniedHandler,
                                                   final AuthenticationSuccessHandler myAuthenticationSuccessHandler,
                                                   final AuthenticationFailureHandler authenticationFailureHandler,
                                                   final SessionRegistry sessionRegistry,
                                                   final PersistentTokenRepository persistentTokenRepository) throws Exception {

        // CSRF protection is enabled (session-based form-login app). The token is
        // rendered into a <meta> tag on every page (see fragments/header) and echoed
        // back by jQuery AJAX calls via a global ajaxSend hook in shrinkly.js; Thymeleaf
        // forms with th:action get a hidden _csrf field automatically. GET requests
        // (including the /{id} redirect) are not protected, so they are unaffected.
        http.csrf(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**").permitAll()
                    .requestMatchers("/", "/user/resetPassword*", "/user/changePassword*", "/user/resendRegistrationToken*", "/shrink/api/**", "/invite/**").permitAll()
                    .requestMatchers("/updatePassword*", "/user/updatePassword*", "/user/savePassword*").hasAuthority("CHANGE_PASSWORD_PRIVILEGE")
                    .requestMatchers("/admin/**").hasAnyRole("ADMIN")
                    // /user/remove (delete own account) must be listed explicitly: it is a
                    // multi-segment path, so the "/*" rule below does not cover it, and an
                    // unmatched request is denied — which silently blocked account deletion
                    // for every logged-in user.
                    .requestMatchers("/user", "/user/remove", "/shrinklyAnalytics/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers("/*").permitAll())
                    //.anyRequest().authenticated()
                .formLogin(form -> form
                    .loginPage("/login")
                    .successHandler(myAuthenticationSuccessHandler)
                    .failureHandler(authenticationFailureHandler)
                    .permitAll())
                .logout(logout -> logout
                    // Logout is triggered via GET links and a JS redirect; keep matching
                    // GET so enabling CSRF (which otherwise forces POST logout) doesn't
                    // break those existing flows.
                    .logoutRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/perform_logout"))
                    .deleteCookies("JSESSIONID")
                    .invalidateHttpSession(true)
                    .logoutSuccessUrl("/login?logout")
                    .permitAll())
                .sessionManagement(session -> session
                    .sessionConcurrency(concurrency -> concurrency
                        .maximumSessions(1)
                        .sessionRegistry(sessionRegistry))
                    .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::changeSessionId))
                .exceptionHandling(handling -> handling.accessDeniedHandler(accessDeniedHandler))
                .rememberMe(remember -> remember
                    .tokenRepository(persistentTokenRepository)
                    .tokenValiditySeconds(432000)); // 5 day

        // .invalidSessionUrl("/invalidSession")
        // Removed session invalidation redirect (Mar 12, 2019)
        // For stopping redirects to shortened urls after login sessions expired

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository(final DataSource dataSource) {
        JdbcTokenRepositoryImpl db = new JdbcTokenRepositoryImpl();
        db.setDataSource(dataSource);
        return db;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<HttpSessionEventPublisher>(new HttpSessionEventPublisher());
    }

}
