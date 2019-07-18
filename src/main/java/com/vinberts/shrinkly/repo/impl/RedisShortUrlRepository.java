package com.vinberts.shrinkly.repo.impl;

import com.google.common.primitives.Longs;
import com.vinberts.shrinkly.config.SpringRedisConfig;
import com.vinberts.shrinkly.persistence.model.redis.ShortUrl;
import com.vinberts.shrinkly.repo.ShortUrlRepository;
import com.vinberts.shrinkly.service.ShrinklyUrlHashService;
import com.vinberts.shrinkly.service.impl.ShrinklyUrlHashServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
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

    private boolean alreadySetup = false;
    private static final String REDIS_KEY_PREFIX = "shrinkly.url:";
    private static final String REDIS_KEY_PATTERN = REDIS_KEY_PREFIX + "*";
    private static final String REDIS_KEY_PREFIX_CLICKS = "shrinkly.clicks:";
    private static final String REDIS_KEY_PATTERN_CLICKS = REDIS_KEY_PREFIX_CLICKS + "*";

    private ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext(SpringRedisConfig.class);

    @SuppressWarnings("unchecked")
    private RedisTemplate<String, String> redisTemplate = (RedisTemplate<String, String>) ctx.getBean("redisTemplate");

    private BigInteger count;

    @PostConstruct
    public void init() {
        if (alreadySetup) {
            log.info("url count init method called - current count " + count);
        } else {
            count = BigInteger.valueOf(redisTemplate.keys(REDIS_KEY_PATTERN).size());
            alreadySetup = true;
            log.info("Initialized total url count to " + count);
        }
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
                    log.debug("Setting custom short url " + customAlias + " to expire in " + diffSec + " seconds");
                    redisTemplate.opsForValue().set(keyToUse, url, diffSec, TimeUnit.SECONDS);
                    // set initial click count
                    redisTemplate.opsForValue().set(REDIS_KEY_PATTERN_CLICKS + customAlias, "0", diffSec, TimeUnit.SECONDS);
                } else {
                    redisTemplate.opsForValue().set(keyToUse, url);
                    // set initial click count
                    redisTemplate.opsForValue().set(REDIS_KEY_PATTERN_CLICKS + customAlias, "0");
                }
            } catch (Exception e) {
                log.error("Error trying to set key for custom alias, ", e);
                return null;
            }
            return new ShortUrl(url, customAlias);
        }
    }

    @Override
    public ShortUrl encodeLongUrlWithExpiration(final String url, final Integer size, final LocalDateTime expiryTime) {
        // check if url has already been added

        if (redisTemplate.hasKey(url) && expiryTime == null) {
            String id = redisTemplate.opsForValue().get(url);
            return new ShortUrl(url, id);
        }
        if (size != null) {
            // else create a new short url
            ShrinklyUrlHashService shrinklyUrlHashService = new ShrinklyUrlHashServiceImpl();
            String id = shrinklyUrlHashService.generateRandomHash(size);
            String keyToUse = REDIS_KEY_PREFIX + id;
            if (redisTemplate.hasKey(keyToUse)) {
                int tries = 0;
                do {
                    tries++;
                    id = shrinklyUrlHashService.generateRandomHash(size);
                    keyToUse = REDIS_KEY_PREFIX + id;
                    if (tries > 10 && tries < 20) {
                        id = shrinklyUrlHashService.generateRandomHash(size) + "0";
                        keyToUse = REDIS_KEY_PREFIX + id;
                    } else if (tries > 20 && tries < 30) {
                        id = shrinklyUrlHashService.generateRandomHash(size) + "o";
                        keyToUse = REDIS_KEY_PREFIX + id;
                    } else if (tries > 30) {
                        log.warn("key reuse: " + keyToUse + " tries: " + tries);
                        id = shrinklyUrlHashService.generateHash(url);
                        keyToUse = REDIS_KEY_PREFIX + id;
                    }
                } while (redisTemplate.hasKey(keyToUse));
            }
            try {
                if (expiryTime != null) {
                    LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
                    final long diffSec = Duration.between(now, expiryTime).getSeconds();
                    log.debug("Setting short url " + id + " to expire in " + diffSec + " seconds");
                    redisTemplate.opsForValue().set(keyToUse, url, diffSec, TimeUnit.SECONDS);

                    // set initial click count
                    redisTemplate.opsForValue().set(REDIS_KEY_PATTERN_CLICKS + id, "0", diffSec, TimeUnit.SECONDS);
                } else {
                    redisTemplate.opsForValue().set(keyToUse, url);
                    redisTemplate.opsForValue().set(url, id);
                    // set initial click count
                    redisTemplate.opsForValue().set(REDIS_KEY_PATTERN_CLICKS + id, "0");
                }
            } catch (Exception e) {
                log.error("Error trying to set key, ", e);
            }

            return new ShortUrl(url, id);
        } else {
            return this.encodeLongUrl(url);
        }
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
            final String oldCountStr = redisTemplate.opsForValue().get(countKey);
            Long oldClick = Longs.tryParse(oldCountStr, 10);
            Long newClick = oldClick + 1L;
            ShortUrl shortUrl = new ShortUrl(url, shortCode, newClick);
            if (expireSeconds > 0) {
                shortUrl.setExpirationInSeconds(expireSeconds);
            } else {
                shortUrl.setExpirationInSeconds(-1L);
            }
            redisTemplate.opsForValue().set(countKey, newClick.toString());
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
        final String countStr = redisTemplate.opsForValue().get(countKey);
        Long clicks = Longs.tryParse(countStr, 10);
        if (clicks != null) {
            return clicks;
        } else {
            return 0L;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ShortUrl encodeLongUrl(final String url, final Integer size) {
        return this.encodeLongUrlWithExpiration(url, size, null);
    }

    @Override
    public void removeShortCode(final String shortCode) {
        final String countKey = REDIS_KEY_PATTERN_CLICKS + shortCode;
        final String keyToRemove = REDIS_KEY_PREFIX + shortCode;
        redisTemplate.delete(keyToRemove);
        redisTemplate.delete(countKey);
    }

    @Override
    public ShortUrl encodeLongUrl(final String url) {
        return this.encodeLongUrl(url, 6);
    }

    @Override
    public BigInteger getTotalStoredUrls() {
        this.incrementCount();
        return count;
    }

    @Override
    public void incrementCount() {
        count = count.add(BigInteger.valueOf(1));
    }

}
