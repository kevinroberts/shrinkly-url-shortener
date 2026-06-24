package com.vinberts.shrinkly.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pins every requestMatchers rule in SpringSecurityConfig.
 *
 * Design notes:
 *  - @WithMockUser / @WithAnonymousUser set the SecurityContext without hitting the DB.
 *  - @MockitoBean PersistentTokenRepository avoids a boot-time dependency on the
 *    persistent_logins table that Flyway would normally create (disabled here).
 *  - All POST requests include .with(csrf()) so CSRF never masks an auth failure.
 *  - notRedirectedToLogin / notRedirectedToUnauthorized encode the "security passed"
 *    assertion: the controller may return any status; security is OK as long as it
 *    did not redirect to /login (unauthenticated) or /unauthorized (access denied).
 */
@SpringBootTest(properties = "spring.flyway.enabled=false")
class SecurityRulesTest {

    @Autowired
    private WebApplicationContext wac;

    /**
     * Mocked to avoid a boot-time schema requirement. The real bean uses
     * JdbcTokenRepositoryImpl which needs the persistent_logins table
     * created by Flyway (disabled for tests).
     */
    @MockitoBean
    private PersistentTokenRepository persistentTokenRepository;

    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
    }

    // --- helpers ---

    private static ResultMatcher notRedirectedToLogin() {
        return result -> {
            int status = result.getResponse().getStatus();
            if (status == 302 || status == 301) {
                String location = result.getResponse().getHeader("Location");
                assertThat(location).as("should not redirect to login").doesNotContain("/login");
            }
            // Non-redirect statuses (200, 500, 404, etc.) mean security passed — controller was reached.
        };
    }

    private static ResultMatcher notRedirectedToUnauthorized() {
        return result -> {
            int status = result.getResponse().getStatus();
            assertThat(status).as("should not be forbidden (403)").isNotEqualTo(403);
            if (status == 302 || status == 301) {
                String location = result.getResponse().getHeader("Location");
                assertThat(location).as("should not redirect to /unauthorized").doesNotContain("/unauthorized");
            }
        };
    }

    // --- permitAll: static resources ---

    @Test
    @WithAnonymousUser
    void cssIsPublic() throws Exception {
        mvc.perform(get("/css/app.css"))
                .andExpect(notRedirectedToLogin());
    }

    @Test
    @WithAnonymousUser
    void jsIsPublic() throws Exception {
        mvc.perform(get("/js/shrinkly.js"))
                .andExpect(notRedirectedToLogin());
    }

    // --- permitAll: root and single-segment paths ---

    @Test
    @WithAnonymousUser
    void rootIsPublic() throws Exception {
        mvc.perform(get("/"))
                .andExpect(notRedirectedToLogin());
    }

    @Test
    @WithAnonymousUser
    void loginPageIsPublic() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(notRedirectedToLogin());
    }

    // --- permitAll: public multi-segment paths ---

    @Test
    @WithAnonymousUser
    void shrinkApiIsPublic() throws Exception {
        mvc.perform(get("/shrink/api/test"))
                .andExpect(notRedirectedToLogin());
    }

    @Test
    @WithAnonymousUser
    void inviteAcceptIsPublic() throws Exception {
        mvc.perform(get("/invite/accept"))
                .andExpect(notRedirectedToLogin());
    }

    @Test
    @WithAnonymousUser
    void passwordResetIsPublic() throws Exception {
        mvc.perform(get("/user/resetPassword"))
                .andExpect(notRedirectedToLogin());
    }

    // --- /admin/** requires ROLE_ADMIN ---

    @Test
    @WithAnonymousUser
    void adminRequiresAuthentication() throws Exception {
        mvc.perform(get("/admin/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminForbiddenForRoleUser() throws Exception {
        mvc.perform(get("/admin/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/unauthorized"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminAllowedForRoleAdmin() throws Exception {
        try {
            mvc.perform(get("/admin/"))
                    .andExpect(notRedirectedToLogin())
                    .andExpect(notRedirectedToUnauthorized());
        } catch (Exception ex) {
            // AssertionError (from ResultMatcher/assertThat failures) extends Error, not Exception —
            // it bypasses this catch and propagates, failing the test correctly.
            // Only controller/template exceptions land here; those prove security passed.
        }
    }

    // --- /user requires ROLE_USER or ROLE_ADMIN ---

    @Test
    @WithAnonymousUser
    void userPageRequiresAuthentication() throws Exception {
        mvc.perform(get("/user"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userPageAllowedForRoleUser() throws Exception {
        // Security passes the request; controller may fail (no DB in test context).
        // We only assert that security did not redirect away.
        try {
            mvc.perform(get("/user"))
                    .andExpect(notRedirectedToLogin())
                    .andExpect(notRedirectedToUnauthorized());
        } catch (Exception ex) {
            // AssertionError (from ResultMatcher/assertThat failures) extends Error, not Exception —
            // it bypasses this catch and propagates, failing the test correctly.
            // Only controller/template exceptions land here; those prove security passed.
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void userPageAllowedForRoleAdmin() throws Exception {
        try {
            mvc.perform(get("/user"))
                    .andExpect(notRedirectedToLogin())
                    .andExpect(notRedirectedToUnauthorized());
        } catch (Exception ex) {
            // AssertionError (from ResultMatcher/assertThat failures) extends Error, not Exception —
            // it bypasses this catch and propagates, failing the test correctly.
            // Only controller/template exceptions land here; those prove security passed.
        }
    }

    // --- /user/remove regression: was blocked for ALL roles in Spring Security 7
    //     because multi-segment paths are not covered by "/*" and unmatched requests
    //     are denied. Fixed by listing /user/remove explicitly. These tests prevent
    //     that class of regression from silently re-entering the codebase. ---

    @Test
    @WithAnonymousUser
    void userRemoveRequiresAuthentication() throws Exception {
        mvc.perform(post("/user/remove").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userRemoveAllowedForRoleUser() throws Exception {
        try {
            mvc.perform(post("/user/remove").with(csrf()))
                    .andExpect(notRedirectedToLogin())
                    .andExpect(notRedirectedToUnauthorized());
        } catch (Exception ex) {
            // AssertionError (from ResultMatcher/assertThat failures) extends Error, not Exception —
            // it bypasses this catch and propagates, failing the test correctly.
            // Only controller/template exceptions land here; those prove security passed.
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void userRemoveAllowedForRoleAdmin() throws Exception {
        try {
            mvc.perform(post("/user/remove").with(csrf()))
                    .andExpect(notRedirectedToLogin())
                    .andExpect(notRedirectedToUnauthorized());
        } catch (Exception ex) {
            // AssertionError (from ResultMatcher/assertThat failures) extends Error, not Exception —
            // it bypasses this catch and propagates, failing the test correctly.
            // Only controller/template exceptions land here; those prove security passed.
        }
    }

    // --- /shrinklyAnalytics/** requires ROLE_USER or ROLE_ADMIN ---

    @Test
    @WithAnonymousUser
    void analyticsRequiresAuthentication() throws Exception {
        mvc.perform(get("/shrinklyAnalytics/abc123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void analyticsAllowedForRoleUser() throws Exception {
        try {
            mvc.perform(get("/shrinklyAnalytics/abc123"))
                    .andExpect(notRedirectedToLogin())
                    .andExpect(notRedirectedToUnauthorized());
        } catch (Exception ex) {
            // AssertionError (from ResultMatcher/assertThat failures) extends Error, not Exception —
            // it bypasses this catch and propagates, failing the test correctly.
            // Only controller/template exceptions land here; those prove security passed.
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void analyticsAllowedForRoleAdmin() throws Exception {
        try {
            mvc.perform(get("/shrinklyAnalytics/abc123"))
                    .andExpect(notRedirectedToLogin())
                    .andExpect(notRedirectedToUnauthorized());
        } catch (Exception ex) {
            // AssertionError (from ResultMatcher/assertThat failures) extends Error, not Exception —
            // it bypasses this catch and propagates, failing the test correctly.
            // Only controller/template exceptions land here; those prove security passed.
        }
    }

    // --- CHANGE_PASSWORD_PRIVILEGE ---

    @Test
    @WithAnonymousUser
    void savePasswordRequiresAuthentication() throws Exception {
        mvc.perform(post("/user/savePassword").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void savePasswordForbiddenWithoutPrivilege() throws Exception {
        // ROLE_USER alone does not carry CHANGE_PASSWORD_PRIVILEGE
        mvc.perform(post("/user/savePassword").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/unauthorized"));
    }

    @Test
    @WithMockUser(authorities = "CHANGE_PASSWORD_PRIVILEGE")
    void savePasswordAllowedWithPrivilege() throws Exception {
        try {
            mvc.perform(post("/user/savePassword").with(csrf()))
                    .andExpect(notRedirectedToLogin())
                    .andExpect(notRedirectedToUnauthorized());
        } catch (Exception ex) {
            // AssertionError (from ResultMatcher/assertThat failures) extends Error, not Exception —
            // it bypasses this catch and propagates, failing the test correctly.
            // Only controller/template exceptions land here; those prove security passed.
        }
    }

    // --- /updatePassword* and /user/updatePassword* (also require CHANGE_PASSWORD_PRIVILEGE) ---

    @Test
    @WithAnonymousUser
    void updatePasswordRequiresAuthentication() throws Exception {
        mvc.perform(post("/user/updatePassword").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updatePasswordForbiddenWithoutPrivilege() throws Exception {
        mvc.perform(post("/user/updatePassword").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/unauthorized"));
    }

    @Test
    @WithMockUser(authorities = "CHANGE_PASSWORD_PRIVILEGE")
    void updatePasswordAllowedWithPrivilege() throws Exception {
        try {
            mvc.perform(post("/user/updatePassword").with(csrf()))
                    .andExpect(notRedirectedToLogin())
                    .andExpect(notRedirectedToUnauthorized());
        } catch (Exception ex) {
            // AssertionError (from ResultMatcher/assertThat failures) extends Error, not Exception —
            // it bypasses this catch and propagates, failing the test correctly.
            // Only controller/template exceptions land here; those prove security passed.
        }
    }

    // --- permitAll: /user/changePassword* and /user/resendRegistrationToken* ---

    @Test
    @WithAnonymousUser
    void changePasswordPageIsPublic() throws Exception {
        mvc.perform(get("/user/changePassword"))
                .andExpect(notRedirectedToLogin());
    }

    @Test
    @WithAnonymousUser
    void resendRegistrationTokenIsPublic() throws Exception {
        mvc.perform(get("/user/resendRegistrationToken"))
                .andExpect(notRedirectedToLogin());
    }
}
