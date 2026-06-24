package com.vinberts.shrinkly.service;

import com.vinberts.shrinkly.persistence.model.UserShortUrl;
import com.vinberts.shrinkly.repo.impl.RedisShortUrlRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClickReconciliationServiceTest {

    private UserShortUrl shortUrl(final String code, final long clicks) {
        UserShortUrl u = new UserShortUrl();
        u.setShortUrl(code);
        u.setClicks(clicks);
        return u;
    }

    @Test
    void updatesOnlyStaleRowsAndReturnsCount() {
        IShortUrlService shortUrlService = mock(IShortUrlService.class);
        RedisShortUrlRepository repo = mock(RedisShortUrlRepository.class);

        UserShortUrl inSync = shortUrl("aaa", 5L);
        UserShortUrl stale = shortUrl("bbb", 2L);
        when(shortUrlService.getAllShortUrls()).thenReturn(List.of(inSync, stale));
        when(repo.getClicksForShortUrl("aaa")).thenReturn(5L);  // matches DB -> skip
        when(repo.getClicksForShortUrl("bbb")).thenReturn(9L);  // differs -> update

        ClickReconciliationService service = new ClickReconciliationService(shortUrlService, repo);

        assertEquals(1, service.reconcileAll());
        verify(shortUrlService).updateClicksForShortUrl("bbb", 9L);
        verify(shortUrlService, never()).updateClicksForShortUrl("aaa", 5L);
    }

    @Test
    void skipsRowsWithNoRedisCount() {
        IShortUrlService shortUrlService = mock(IShortUrlService.class);
        RedisShortUrlRepository repo = mock(RedisShortUrlRepository.class);

        when(shortUrlService.getAllShortUrls()).thenReturn(List.of(shortUrl("ccc", 3L)));
        when(repo.getClicksForShortUrl("ccc")).thenReturn(null);

        ClickReconciliationService service = new ClickReconciliationService(shortUrlService, repo);

        assertEquals(0, service.reconcileAll());
        verify(shortUrlService, never()).updateClicksForShortUrl(anyString(), any());
    }
}
