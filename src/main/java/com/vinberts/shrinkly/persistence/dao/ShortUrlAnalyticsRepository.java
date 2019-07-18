package com.vinberts.shrinkly.persistence.dao;

import com.vinberts.shrinkly.persistence.model.CountryHit;
import com.vinberts.shrinkly.persistence.model.Referrers;
import com.vinberts.shrinkly.persistence.model.ShortUrlAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public interface ShortUrlAnalyticsRepository extends JpaRepository<ShortUrlAnalytics, Long> {

    
    Collection<ShortUrlAnalytics> getAllByShortUrl(String shortUrl);

    @Modifying
    @Query("delete from ShortUrlAnalytics t where t.expiryDate <= ?1")
    void deleteAllExpiredSince(LocalDateTime now);

    @Query("SELECT COUNT(DISTINCT ipAddress) as count from ShortUrlAnalytics WHERE shortUrl = ?1")
    Long getUniqueIpsByShortUrl(String shortUrl);

    @Query("SELECT COUNT(DISTINCT countryCode) as count from ShortUrlAnalytics WHERE shortUrl = ?1")
    Long getUniqueCountriessByShortUrl(String shortUrl);

    @Query("SELECT COUNT(deviceType) as count from ShortUrlAnalytics WHERE deviceType =?1 AND shortUrl = ?2")
    Long getUniqueHitsByDeviceTypeAndShortUrl(String deviceType, String shortUrl);

    @Query(value = "SELECT new com.vinberts.shrinkly.persistence.model.Referrers(COUNT(referrer), referrer) from ShortUrlAnalytics WHERE shortUrl = ?1 and referrer IS NOT NULL group by referrer order by COUNT(referrer) desc")
    List<Referrers> getReferrersByShortUrl(String shortUrl);

    @Query(value = "SELECT new com.vinberts.shrinkly.persistence.model.CountryHit(COUNT(countryCode), countryCode) from ShortUrlAnalytics WHERE shortUrl = ?1 and countryCode IS NOT NULL group by countryCode order by COUNT(countryCode) desc")
    List<CountryHit> getCountryHitsByShortUrl(String shortUrl);

}
