package com.vinberts.shrinkly.service.impl;

import com.vinberts.shrinkly.persistence.dao.UserShortUrlRepository;
import com.vinberts.shrinkly.persistence.model.User;
import com.vinberts.shrinkly.persistence.model.UserShortUrl;
import com.vinberts.shrinkly.service.IShortUrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 *
 */
@Service
@Transactional
public class ShortUrlServiceImpl implements IShortUrlService {

    @Autowired
    UserShortUrlRepository userShortUrlRepository;

    @Override
    public Page<UserShortUrl> findPaginated(final User user, final Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            if (pageable.getSort().toString().equals("dateAdded: DESC")) {
                return userShortUrlRepository.queryAllByUserOrderByDateAddedDesc(user, pageable);
            } else if (pageable.getSort().toString().equals("dateAdded: ASC")) {
                return userShortUrlRepository.queryAllByUserOrderByDateAddedAsc(user, pageable);
            }
        }
        return userShortUrlRepository.queryAllByUser(user, pageable);
    }

    @Override
    public Long getCountByUser(final User user) {
        return userShortUrlRepository.countByUser(user);
    }

    @Override
    public void saveNewShortUrl(final UserShortUrl userShortUrl) {
        userShortUrl.setClicks(0L);
        userShortUrl.setDateAdded(LocalDateTime.now());
        userShortUrlRepository.save(userShortUrl);
    }

    @Override
    public UserShortUrl findByUserAndFullUrl(final User user, final String fullUrl) {
        return userShortUrlRepository.findFirstByUserAndFullUrl(user, fullUrl);
    }

    @Override
    public UserShortUrl findByUserAndShortUrl(final User user, final String shortUrl) {
        return userShortUrlRepository.findFirstByUserAndShortUrl(user, shortUrl);
    }

}
