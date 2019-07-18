package com.vinberts.shrinkly.service;

import com.vinberts.shrinkly.persistence.model.User;
import com.vinberts.shrinkly.persistence.model.UserShortUrl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 */
public interface IShortUrlService {


    Page<UserShortUrl> findPaginated(User user, Pageable pageable);

    Long getCountByUser(User user);

    void saveNewShortUrl(UserShortUrl userShortUrl);

    UserShortUrl findByUserAndFullUrl(User user, String fullUrl);

    UserShortUrl findByUserAndShortUrl(User user, String shortUrl);

}
