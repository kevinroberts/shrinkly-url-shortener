package com.vinberts.shrinkly.service;

import com.vinberts.shrinkly.persistence.model.UserShortUrl;
import com.vinberts.shrinkly.repo.impl.RedisShortUrlRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reconciles the Postgres {@code clicks} column from the authoritative Redis
 * click counters. Shared by the scheduled {@link com.vinberts.shrinkly.task.ClickCountReconcileTask}
 * and the manual {@code /admin/reindex} endpoint so the loop lives in one place.
 */
@Service
@Slf4j
public class ClickReconciliationService {

    private final IShortUrlService shortUrlService;
    private final RedisShortUrlRepository shortUrlRepository;

    public ClickReconciliationService(final IShortUrlService shortUrlService,
                                      final RedisShortUrlRepository shortUrlRepository) {
        this.shortUrlService = shortUrlService;
        this.shortUrlRepository = shortUrlRepository;
    }

    /**
     * Writes the live Redis click count into the Postgres {@code clicks} column for
     * every short url whose stored count is stale, leaving in-sync rows untouched.
     *
     * @return the number of rows updated
     */
    @Transactional
    public int reconcileAll() {
        final List<UserShortUrl> shortUrls = shortUrlService.getAllShortUrls();
        int updated = 0;
        for (final UserShortUrl shortUrl : shortUrls) {
            final Long redisClicks = shortUrlRepository.getClicksForShortUrl(shortUrl.getShortUrl());
            if (redisClicks != null && !redisClicks.equals(shortUrl.getClicks())) {
                shortUrlService.updateClicksForShortUrl(shortUrl.getShortUrl(), redisClicks);
                updated++;
            }
        }
        log.debug("Reconciled click counts for {} of {} short urls", updated, shortUrls.size());
        return updated;
    }
}
