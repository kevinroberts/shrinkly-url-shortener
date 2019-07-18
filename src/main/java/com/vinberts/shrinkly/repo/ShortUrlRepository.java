package com.vinberts.shrinkly.repo;

import com.vinberts.shrinkly.persistence.model.redis.ShortUrl;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 *
 */
public interface ShortUrlRepository {

    ShortUrl encodeLongUrlCustom(String url, String customAlias);

    ShortUrl encodeLongUrlCustomWithExpiration(String url, String customAlias, LocalDateTime expiryTime);

    Optional<ShortUrl> decodeAndIncrementShortUrl(String shortCode);

    Optional<ShortUrl> getShortCodeIfExists(String shortCode);

    Long getClicksForShortUrl(String shortCode);

    ShortUrl encodeLongUrl(String url, Integer size);

    ShortUrl encodeLongUrlWithExpiration(String url, Integer size, LocalDateTime expiryTime);

    ShortUrl encodeLongUrl(String url);

    BigInteger getTotalStoredUrls();

    void removeShortCode(String shortCode);

    void incrementCount();

}
