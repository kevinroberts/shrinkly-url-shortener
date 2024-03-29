package com.vinberts.shrinkly.service.impl;

import com.blueconic.browscap.Capabilities;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.maxmind.geoip2.record.Country;
import com.vinberts.shrinkly.persistence.dao.ShortUrlAnalyticsRepository;
import com.vinberts.shrinkly.persistence.model.CountryHit;
import com.vinberts.shrinkly.persistence.model.Referrers;
import com.vinberts.shrinkly.persistence.model.ShortUrlAnalytics;
import com.vinberts.shrinkly.service.IServerLocationService;
import com.vinberts.shrinkly.service.IShortUrlAnalyticsService;
import com.vinberts.shrinkly.service.UserAgentParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.vinberts.shrinkly.utils.LinkUtil.getDomainName;

/**
 *
 */
@Service
@Transactional
public class ShortUrlAnalyticsServiceImpl implements IShortUrlAnalyticsService {

    @Autowired
    ShortUrlAnalyticsRepository shortUrlAnalyticsRepository;

    @Autowired
    private UserAgentParserService userAgentParserService;

    @Autowired
    private IServerLocationService serverLocationService;

    private final static Map<String, String> CONTINENT_MAP = Maps.newHashMap(
            new ImmutableMap.Builder<String, String>().put("A1", "--").put("AD", "EU").put("AE", "AS").put("AF", "AS").put("AG", "NA").put("AI", "NA").put("AL", "EU").put("AM", "AS").put("AN", "NA").put("AO", "AF").put("AP", "AS").put("AQ", "AN").put("AR", "SA").put("AS", "OC").put("AT", "EU").put("AU", "OC").put("AW", "NA").put("AX", "EU").put("AZ", "AS").put("BA", "EU").put("BB", "NA").put("BD", "AS").put("BE", "EU").put("BF", "AF").put("BG", "EU").put("BH", "AS").put("BI", "AF").put("BJ", "AF").put("BL", "NA").put("BM", "NA").put("BN", "AS").put("BO", "SA").put("BR", "SA").put("BS", "NA").put("BT", "AS").put("BV", "AN").put("BW", "AF").put("BY", "EU").put("BZ", "NA").put("CA", "NA").put("CC", "AS").put("CD", "AF").put("CF", "AF").put("CG", "AF").put("CH", "EU").put("CI", "AF").put("CK", "OC").put("CL", "SA").put("CM", "AF").put("CN", "AS").put("CO", "SA").put("CR", "NA").put("CU", "NA").put("CV", "AF").put("CX", "AS").put("CY", "AS").put("CZ", "EU").put("DE", "EU").put("DJ", "AF").put("DK", "EU").put("DM", "NA").put("DO", "NA").put("DZ", "AF").put("EC", "SA").put("EE", "EU").put("EG", "AF").put("EH", "AF").put("ER", "AF").put("ES", "EU").put("ET", "AF").put("EU", "EU").put("FI", "EU").put("FJ", "OC").put("FK", "SA").put("FM", "OC").put("FO", "EU").put("FR", "EU").put("FX", "EU").put("GA", "AF").put("GB", "EU").put("GD", "NA").put("GE", "AS").put("GF", "SA").put("GG", "EU").put("GH", "AF").put("GI", "EU").put("GL", "NA").put("GM", "AF").put("GN", "AF").put("GP", "NA").put("GQ", "AF").put("GR", "EU").put("GS", "AN").put("GT", "NA").put("GU", "OC").put("GW", "AF").put("GY", "SA").put("HK", "AS").put("HM", "AN").put("HN", "NA").put("HR", "EU").put("HT", "NA").put("HU", "EU").put("ID", "AS").put("IE", "EU").put("IL", "AS").put("IM", "EU").put("IN", "AS").put("IO", "AS").put("IQ", "AS").put("IR", "AS").put("IS", "EU").put("IT", "EU").put("JE", "EU").put("JM", "NA").put("JO", "AS").put("JP", "AS").put("KE", "AF").put("KG", "AS").put("KH", "AS").put("KI", "OC").put("KM", "AF").put("KN", "NA").put("KP", "AS").put("KR", "AS").put("KW", "AS").put("KY", "NA").put("KZ", "AS").put("LA", "AS").put("LB", "AS").put("LC", "NA").put("LI", "EU").put("LK", "AS").put("LR", "AF").put("LS", "AF").put("LT", "EU").put("LU", "EU").put("LV", "EU").put("LY", "AF").put("MA", "AF").put("MC", "EU").put("MD", "EU").put("ME", "EU").put("MF", "NA").put("MG", "AF").put("MH", "OC").put("MK", "EU").put("ML", "AF").put("MM", "AS").put("MN", "AS").put("MO", "AS").put("MP", "OC").put("MQ", "NA").put("MR", "AF").put("MS", "NA").put("MT", "EU").put("MU", "AF").put("MV", "AS").put("MW", "AF").put("MX", "NA").put("MY", "AS").put("MZ", "AF").put("NA", "AF").put("NC", "OC").put("NE", "AF").put("NF", "OC").put("NG", "AF").put("NI", "NA").put("NL", "EU").put("NO", "EU").put("NP", "AS").put("NR", "OC").put("NU", "OC").put("NZ", "OC").put("O1", "--").put("OM", "AS").put("PA", "NA").put("PE", "SA").put("PF", "OC").put("PG", "OC").put("PH", "AS").put("PK", "AS").put("PL", "EU").put("PM", "NA").put("PN", "OC").put("PR", "NA").put("PS", "AS").put("PT", "EU").put("PW", "OC").put("PY", "SA").put("QA", "AS").put("RE", "AF").put("RO", "EU").put("RS", "EU").put("RU", "EU").put("RW", "AF").put("SA", "AS").put("SB", "OC").put("SC", "AF").put("SD", "AF").put("SE", "EU").put("SG", "AS").put("SH", "AF").put("SI", "EU").put("SJ", "EU").put("SK", "EU").put("SL", "AF").put("SM", "EU").put("SN", "AF").put("SO", "AF").put("SR", "SA").put("ST", "AF").put("SV", "NA").put("SY", "AS").put("SZ", "AF").put("TC", "NA").put("TD", "AF").put("TF", "AN").put("TG", "AF").put("TH", "AS").put("TJ", "AS").put("TK", "OC").put("TL", "AS").put("TM", "AS").put("TN", "AF").put("TO", "OC").put("TR", "EU").put("TT", "NA").put("TV", "OC").put("TW", "AS").put("TZ", "AF").put("UA", "EU").put("UG", "AF").put("UM", "OC").put("US", "NA").put("UY", "SA").put("UZ", "AS").put("VA", "EU").put("VC", "NA").put("VE", "SA").put("VG", "NA").put("VI", "NA").put("VN", "AS").put("VU", "OC").put("WF", "OC").put("WS", "OC").put("YE", "AS").put("YT", "AF").put("ZA", "AF").put("ZM", "AF").put("ZW", "AF").build());

    @Override
    @Async
    public void addAnalyticsForShortUrl(final String shortUrl, final Optional<String> userAgent, final String ipAddress, final Optional<String> referrer, final Optional<LocalDateTime> expiry) {
        ShortUrlAnalytics shortUrlAnalytics = new ShortUrlAnalytics();
        shortUrlAnalytics.setShortUrl(shortUrl);
        shortUrlAnalytics.setIpAddress(ipAddress);
        shortUrlAnalytics.setDateAdded(LocalDateTime.now());

        if (referrer.isPresent()) {
            shortUrlAnalytics.setReferrer(getDomainName(referrer.get()));
        }

        if (expiry.isPresent()) {
            shortUrlAnalytics.setExpiryDate(expiry.get());
        }

        if (userAgent.isPresent()) {
            final Capabilities capabilities = userAgentParserService.getCapabilities(userAgent.get());

            shortUrlAnalytics.setBrowser(capabilities.getBrowser());
            shortUrlAnalytics.setBrowserVersion(capabilities.getBrowserMajorVersion());
            shortUrlAnalytics.setBrowserType(capabilities.getBrowserType());
            shortUrlAnalytics.setOperatingSystem(capabilities.getPlatform());
            shortUrlAnalytics.setOperatingSystemVersion(capabilities.getPlatformVersion());

            shortUrlAnalytics.setDeviceType(capabilities.getDeviceType());
        }

        // ISO continent names
        //AF	Africa
        //AN	Antarctica
        //AS	Asia
        //EU	Europe
        //NA	North america
        //OC	Oceania
        //SA	South america

        Optional<Country> country = serverLocationService.getLocation(ipAddress);

        if (country.isPresent()) {
            String countryIso = country.get().getIsoCode();
            if (CONTINENT_MAP.containsKey(countryIso)) {
                shortUrlAnalytics.setContinentName(CONTINENT_MAP.get(countryIso));
            }
            shortUrlAnalytics.setCountryCode(country.get().getIsoCode());
        }

        shortUrlAnalyticsRepository.save(shortUrlAnalytics);

    }

    @Override
    public Long getUniqueIpsForShortUrl(final String shortUrl) {
        return shortUrlAnalyticsRepository.getUniqueIpsByShortUrl(shortUrl);
    }

    @Override
    public Long getUniqueCountriesForShortUrl(final String shortUrl) {
        return shortUrlAnalyticsRepository.getUniqueCountriessByShortUrl(shortUrl);
    }

    @Override
    public Long getPercentageOfDesktopVsMobile(final String shortUrl) {
        Long mobileHits = shortUrlAnalyticsRepository.getUniqueHitsByDeviceTypeAndShortUrl("Mobile Phone", shortUrl);
        Long desktop = shortUrlAnalyticsRepository.getUniqueHitsByDeviceTypeAndShortUrl("Desktop", shortUrl);
        if (desktop < 1) {
            return 0l;
        }
        return ((mobileHits / desktop) * 100);

    }

    @Override
    public List<Referrers> getReferrersByShortUrl(final String shortUrl) {
        return shortUrlAnalyticsRepository.getReferrersByShortUrl(shortUrl);
    }

    @Override
    public List<CountryHit> getCountryHitsByShortUrl(final String shortUrl) {
        List<CountryHit> countryHits = shortUrlAnalyticsRepository.getCountryHitsByShortUrl(shortUrl);

        // translate country ISO2 code to display name
        countryHits.forEach(countryHit -> {
            Locale l = new Locale("", countryHit.getCountry());
            countryHit.setCountry(l.getDisplayCountry());
        });

        return countryHits;

    }
}
