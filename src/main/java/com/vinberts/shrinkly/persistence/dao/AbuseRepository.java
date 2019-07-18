package com.vinberts.shrinkly.persistence.dao;

import com.vinberts.shrinkly.persistence.model.Abuse;
import com.vinberts.shrinkly.persistence.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 */
public interface AbuseRepository extends JpaRepository<Abuse, Long> {

    Page<Abuse> queryAllByDateAddedIsNotNullOrderByDateAddedDesc(Pageable pageable);

    void deleteAllByUser(User user);

}
