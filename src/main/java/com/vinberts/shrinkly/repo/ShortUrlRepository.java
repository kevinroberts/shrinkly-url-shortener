package com.vinberts.shrinkly.repo;

import com.vinberts.shrinkly.persistence.model.redis.ShortUrl;

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

    ShortUrl encodeLongUrl(String url);

    ShortUrl encodeLongUrlWithExpiration(String url, LocalDateTime expiryTime);

    void removeShortCode(String shortCode);

}
