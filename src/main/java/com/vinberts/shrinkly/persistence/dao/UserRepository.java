package com.vinberts.shrinkly.persistence.dao;

import com.vinberts.shrinkly.persistence.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 */
public interface UserRepository extends JpaRepository<User, Long> {

    User findUserByUsername(String username);

    User findByEmail(String email);

    @Override
    void delete(User user);

}
