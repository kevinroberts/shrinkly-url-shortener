package com.vinberts.shrinkly.persistence.model.redis;

import java.io.Serializable;

/**
 *
 */
public class ShortUrl implements Serializable {
    private static final long serialVersionUID = 1L;
    private String url;
    private String shortenedKey;
    private Long clicks;
    private String shortUrl;
    private Long expirationInSeconds;

    public ShortUrl(final String url, final String shortenedKey) {
        this.url = url;
        this.shortenedKey = shortenedKey;
        this.clicks = 0L;
    }

    public ShortUrl(final String url, final String shortenedKey, final Long clicks) {
        this.url = url;
        this.shortenedKey = shortenedKey;
        this.clicks = clicks;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getShortenedKey() {
        return shortenedKey;
    }

    public void setShortenedKey(final String shortenedKey) {
        this.shortenedKey = shortenedKey;
    }

    public Long getClicks() {
        return clicks;
    }

    public void setClicks(final Long clicks) {
        this.clicks = clicks;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(final String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public Long getExpirationInSeconds() {
        return expirationInSeconds;
    }

    public void setExpirationInSeconds(final Long expirationInSeconds) {
        this.expirationInSeconds = expirationInSeconds;
    }

    @Override
    public String toString() {
        return "ShortUrl{" +
                "url='" + url + '\'' +
                ", shortenedKey='" + shortenedKey + '\'' +
                ", clicks=" + clicks +
                '}';
    }
}
