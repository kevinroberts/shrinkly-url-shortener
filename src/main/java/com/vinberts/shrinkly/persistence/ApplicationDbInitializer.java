package com.vinberts.shrinkly.persistence;

import com.vinberts.shrinkly.persistence.dao.PrivilegeRepository;
import com.vinberts.shrinkly.persistence.dao.RoleRepository;
import com.vinberts.shrinkly.persistence.dao.UserRepository;
import com.vinberts.shrinkly.persistence.model.Privilege;
import com.vinberts.shrinkly.persistence.model.Role;
import com.vinberts.shrinkly.persistence.model.User;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 *
 */
@Component
@Profile("local")
@Transactional
public class ApplicationDbInitializer implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    public void run(final ApplicationArguments args) throws Exception {
        // == create initial privileges
        final Privilege readPrivilege = createPrivilegeIfNotFound("READ_PRIVILEGE");
        final Privilege writePrivilege = createPrivilegeIfNotFound("WRITE_PRIVILEGE");
        final Privilege passwordPrivilege = createPrivilegeIfNotFound("CHANGE_PASSWORD_PRIVILEGE");

        // == create initial roles
        final List<Privilege> adminPrivileges = new ArrayList<>(Arrays.asList(readPrivilege, writePrivilege, passwordPrivilege));
        final List<Privilege> userPrivileges = new ArrayList<>(Arrays.asList(readPrivilege, passwordPrivilege));

        final Role adminRole = createRoleIfNotFound("ROLE_ADMIN", adminPrivileges);
        final Role userRole = createRoleIfNotFound("ROLE_USER", userPrivileges);

        // create default admin account
        createUserIfNotFound("admin", "kevin@kevinroberts.us", "admin",
                "https://lh3.googleusercontent.com/-iXqNEhNecVI/AAAAAAAAAAI/AAAAAAAAGrs/HdOTh2F04js/s120-p-rw-no-il/photo.jpg",
                new ArrayList<Role>(Arrays.asList(adminRole, userRole)));
        // create a default user account
        createUserIfNotFound("kevin", "kevin.roberts10@gmail.com", "password",
                "https://lh3.googleusercontent.com/-iXqNEhNecVI/AAAAAAAAAAI/AAAAAAAAGrs/HdOTh2F04js/s120-p-rw-no-il/photo.jpg",
                new ArrayList<Role>(Arrays.asList(userRole)));
    }

    private User createUserIfNotFound(final String username, final String email, final String password, final String profileImage, final Collection<Role> roles) {
        User user = userRepository.findUserByUsername(username);
        if (user == null) {
            // user does not exist yet, create a new one
            user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setProfileImage(profileImage);
            // set default values of a user account
            user.setDateAdded(LocalDateTime.now());
            user.setSecret(Base32.random());
            user.setCredentialsNonExpired(true);
            user.setAccountNonLocked(true);
            user.setAccountNonExpired(true);
            user.setEnabled(true);
        }
        user.setRoles(roles);
        user = userRepository.save(user);
        return user;
    }

    private Privilege createPrivilegeIfNotFound(final String name) {
        Privilege privilege = privilegeRepository.findByName(name);
        if (privilege == null) {
            privilege = new Privilege(name);
            privilege = privilegeRepository.save(privilege);
        }
        return privilege;
    }

    private Role createRoleIfNotFound(final String name, final Collection<Privilege> privileges) {
        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = new Role(name);
        }
        role.setPrivileges(privileges);
        role = roleRepository.save(role);
        return role;
    }
}
