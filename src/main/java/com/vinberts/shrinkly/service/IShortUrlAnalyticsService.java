package com.vinberts.shrinkly.service;

import com.vinberts.shrinkly.persistence.model.CountryHit;
import com.vinberts.shrinkly.persistence.model.Referrers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 *
 */
public interface IShortUrlAnalyticsService {

    void addAnalyticsForShortUrl(String shortUrl, Optional<String> userAgent, String ipAddress, Optional<String> referrer, Optional<LocalDateTime> expiry);

    Long getUniqueIpsForShortUrl(String shortUrl);

    Long getUniqueCountriesForShortUrl(String shortUrl);

    Long getPercentageOfDesktopVsMobile(String shortUrl);

    List<Referrers> getReferrersByShortUrl(String shortUrl);

    List<CountryHit> getCountryHitsByShortUrl(String shortUrl);

}
