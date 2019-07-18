package com.vinberts.shrinkly.task;

import com.vinberts.shrinkly.persistence.dao.ShortUrlAnalyticsRepository;
import com.vinberts.shrinkly.persistence.dao.UserShortUrlRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 *
 */
@Slf4j
@Service
@Transactional
public class ExpiredShortUrlsPurgeTask {

    @Autowired
    UserShortUrlRepository userShortUrlRepository;

    @Autowired
    ShortUrlAnalyticsRepository shortUrlAnalyticsRepository;

    @Scheduled(cron = "${expired.cron.expression}")
    public void purgeExpired() {

        LocalDateTime now = LocalDateTime.now();
        log.debug("Running ExpiredShortUrlsPurgeTask for expired short urls older than " + now.toString());

        userShortUrlRepository.deleteAllExpiredSince(now);
        shortUrlAnalyticsRepository.deleteAllExpiredSince(now);

    }
}
