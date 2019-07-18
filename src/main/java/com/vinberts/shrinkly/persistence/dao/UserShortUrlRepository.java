package com.vinberts.shrinkly.persistence.dao;

import com.vinberts.shrinkly.persistence.model.User;
import com.vinberts.shrinkly.persistence.model.UserShortUrl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 *
 */
public interface UserShortUrlRepository extends JpaRepository<UserShortUrl, Long> {

    Page<UserShortUrl> queryAllByUser(User user, Pageable pageable);

    Page<UserShortUrl> queryAllByUserOrderByDateAddedDesc(User user, Pageable pageable);

    Page<UserShortUrl> queryAllByUserOrderByDateAddedAsc(User user, Pageable pageable);

    Long countByUser(User user);

    Collection<UserShortUrl> findAllByUserAndCustomIsTrue(User user);

    void deleteAllByUser(User user);

    UserShortUrl findFirstByUserAndFullUrl(User user, String fullUrl);

    UserShortUrl findFirstByUserAndShortUrl(User user, String shortUrl);

    @Modifying
    @Query("delete from UserShortUrl t where t.expiryDate <= ?1")
    void deleteAllExpiredSince(LocalDateTime now);

}
