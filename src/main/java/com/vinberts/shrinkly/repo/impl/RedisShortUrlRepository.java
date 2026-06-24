package com.vinberts.shrinkly.repo.impl;

import com.google.common.primitives.Longs;
import com.vinberts.shrinkly.persistence.model.redis.ShortUrl;
import com.vinberts.shrinkly.repo.ShortUrlRepository;
import com.vinberts.shrinkly.service.ShortCodeGenerator;
import com.vinberts.shrinkly.web.errors.ShortUrlPersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Repository
@Slf4j
public class RedisShortUrlRepository implements ShortUrlRepository {

    private static final String REDIS_KEY_PREFIX = "shrinkly.url:";
    private static final String REDIS_KEY_PREFIX_CLICKS = "shrinkly.clicks:";
    private static final String REDIS_KEY_PATTERN_CLICKS = REDIS_KEY_PREFIX_CLICKS + "*";

    private final RedisTemplate<String, String> redisTemplate;
    private final ShortCodeGenerator shortCodeGenerator;

    public RedisShortUrlRepository(final StringRedisTemplate redisTemplate,
                                   final ShortCodeGenerator shortCodeGenerator) {
        this.redisTemplate = redisTemplate;
        this.shortCodeGenerator = shortCodeGenerator;
    }

    @Override
    public ShortUrl encodeLongUrlCustomWithExpiration(final String url, final String customAlias, final LocalDateTime expiryTime) {
        final String keyToUse = REDIS_KEY_PREFIX + customAlias;
        if (redisTemplate.hasKey(keyToUse)) {
            // key already exists
            return null;
        } else {
            try {
                if (expiryTime != null) {
                    LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
                    final long diffSec = Duration.between(now, expiryTime).getSeconds();
                    log.debug("Setting custom short url {} to expire in {} seconds", customAlias, diffSec);
                    redisTemplate.opsForValue().set(keyToUse, url, diffSec, TimeUnit.SECONDS);
                    // set initial click count
                    redisTemplate.opsForValue().set(REDIS_KEY_PATTERN_CLICKS + customAlias, "0", diffSec, TimeUnit.SECONDS);
                } else {
                    redisTemplate.opsForValue().set(keyToUse, url);
                    // set initial click count
                    redisTemplate.opsForValue().set(REDIS_KEY_PATTERN_CLICKS + customAlias, "0");
                }
            } catch (Exception e) {
                // A failed write means the alias does not resolve. Throw rather than
                // return null: null is reserved for the alias-already-taken case above,
                // and swallowing it here would let the caller report a bogus success and
                // persist a Postgres row for a link that 404s on redirect.
                log.error("Error trying to set key for custom alias, ", e);
                throw new ShortUrlPersistenceException("Failed to store custom alias: " + customAlias, e);
            }
            return new ShortUrl(url, customAlias);
        }
    }

    @Override
    public ShortUrl encodeLongUrlWithExpiration(final String url, final LocalDateTime expiryTime) {
        final String id = shortCodeGenerator.nextCode();
        final String keyToUse = REDIS_KEY_PREFIX + id;
        try {
            if (expiryTime != null) {
                final LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
                final long diffSec = Duration.between(now, expiryTime).getSeconds();
                log.debug("Setting short url {} to expire in {} seconds", id, diffSec);
                redisTemplate.opsForValue().set(keyToUse, url, diffSec, TimeUnit.SECONDS);
                redisTemplate.opsForValue().set(REDIS_KEY_PATTERN_CLICKS + id, "0", diffSec, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(keyToUse, url);
                redisTemplate.opsForValue().set(REDIS_KEY_PATTERN_CLICKS + id, "0");
            }
        } catch (Exception e) {
            // The short code was never stored, so the link would 404 on redirect.
            // Throw rather than returning a ShortUrl, which would let the caller report
            // a bogus 201 and persist a Postgres row for an unresolvable link.
            log.error("Error trying to set key, ", e);
            throw new ShortUrlPersistenceException("Failed to store short code: " + id, e);
        }
        return new ShortUrl(url, id);
    }

    @Override
    public ShortUrl encodeLongUrl(final String url) {
        return encodeLongUrlWithExpiration(url, null);
    }

    @Override
    public ShortUrl encodeLongUrlCustom(final String url, final String customAlias) {
        return this.encodeLongUrlCustomWithExpiration(url, customAlias, null);
    }

    /**
     * decodeAndIncrementShortUrl
     * Decodes a shortened URL to its original URL.
     * Also increments a click count on the short url
     * @param {String} shortCode
     * @return {ShortUrl}
     */
    @Override
    public Optional<ShortUrl> decodeAndIncrementShortUrl(final String shortCode) {
        final String keyToGet = REDIS_KEY_PREFIX + shortCode;
        final String url = redisTemplate.opsForValue().get(keyToGet);
        if (url != null) {
            Long expireSeconds = redisTemplate.getExpire(keyToGet, TimeUnit.SECONDS);
            final String countKey = REDIS_KEY_PATTERN_CLICKS + shortCode;
            // Atomic INCR: correct under concurrent hits to the same link, and it
            // preserves the counter key's existing TTL. The previous get/+1/set lost
            // increments under concurrency, dropped the TTL on expiring links, and
            // NPE'd when the counter key was missing.
            Long newClick = redisTemplate.opsForValue().increment(countKey);
            if (newClick == null) {
                newClick = 1L;
            }
            ShortUrl shortUrl = new ShortUrl(url, shortCode, newClick);
            if (expireSeconds > 0) {
                shortUrl.setExpirationInSeconds(expireSeconds);
            } else {
                shortUrl.setExpirationInSeconds(-1L);
            }
            return Optional.of(shortUrl);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ShortUrl> getShortCodeIfExists(final String shortCode) {
        final String keyToGet = REDIS_KEY_PREFIX + shortCode;
        final String url = redisTemplate.opsForValue().get(keyToGet);
        if (url != null) {
            return Optional.of(new ShortUrl(url, shortCode));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Long getClicksForShortUrl(final String shortCode) {
        final String countKey = REDIS_KEY_PATTERN_CLICKS + shortCode;
        final Optional<String> countStr = Optional.ofNullable(redisTemplate.opsForValue().get(countKey));
        Long clicks = Longs.tryParse(countStr.orElse("0"), 10);
        if (clicks != null) {
            return clicks;
        } else {
            return 0L;
        }
    }

    @Override
    public void removeShortCode(final String shortCode) {
        final String keyToRemove = REDIS_KEY_PREFIX + shortCode;
        final String countKey = REDIS_KEY_PATTERN_CLICKS + shortCode;
        // Fetch the long URL before deleting the forward key so we can also clean up
        // any legacy url -> id reverse-mapping entry created by the old code. New
        // entries have no such key, so this delete is a harmless best-effort no-op.
        final String url = redisTemplate.opsForValue().get(keyToRemove);
        final boolean urlRemoved = Boolean.TRUE.equals(redisTemplate.delete(keyToRemove));
        final boolean countRemoved = Boolean.TRUE.equals(redisTemplate.delete(countKey));
        if (url != null) {
            redisTemplate.delete(url);
        }
        if (!urlRemoved || !countRemoved) {
            log.error("could not remove redis keys for short code: {}", shortCode);
        }
    }

}
