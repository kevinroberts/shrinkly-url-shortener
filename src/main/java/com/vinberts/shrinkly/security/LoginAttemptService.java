package com.vinberts.shrinkly.security;

import com.google.common.hash.Hashing;
import com.google.common.primitives.Ints;
import com.vinberts.shrinkly.config.SpringRedisConfig;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Implements a brute force prevention service backed by a
 * Redis cache that expires entries after 30 minutes
 */
@Service
public class LoginAttemptService {
    private final int MAX_ATTEMPT = 10;
    private static final String REDIS_KEY_PREFIX = "shrinkly.login:";

    private ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext(SpringRedisConfig.class);

    @SuppressWarnings("unchecked")
    private RedisTemplate<String, String> redisTemplate = (RedisTemplate<String, String>) ctx.getBean("redisTemplate");

    public LoginAttemptService() {
        super();
    }

    public void loginSucceeded(final String key) {
        final String keyToRemove = REDIS_KEY_PREFIX + Hashing.murmur3_32().hashString(key, StandardCharsets.UTF_8);
        redisTemplate.delete(keyToRemove);
    }

    public void loginFailed(final String key) {
        final String keyToGet = REDIS_KEY_PREFIX + Hashing.murmur3_32().hashString(key, StandardCharsets.UTF_8);
        if (redisTemplate.hasKey(keyToGet)) {
            final String attemptStr = redisTemplate.opsForValue().get(keyToGet);
            Integer currentAttempts = Ints.tryParse(attemptStr, 10);
            currentAttempts++;
            redisTemplate.opsForValue().set(keyToGet, currentAttempts.toString());
        } else {
            final Integer attempts = 0;
            redisTemplate.opsForValue().set(keyToGet, attempts.toString(), Duration.ofMinutes(30));
        }

    }

    public boolean isBlocked(final String key) {
        final String keyToGet = REDIS_KEY_PREFIX + Hashing.murmur3_32().hashString(key, StandardCharsets.UTF_8);
        redisTemplate.hasKey(keyToGet);
        if (redisTemplate.hasKey(keyToGet)) {
            final String attemptStr = redisTemplate.opsForValue().get(keyToGet);
            Integer currentAtempts = Ints.tryParse(attemptStr, 10);
            return currentAtempts >= MAX_ATTEMPT;
        } else {
            return false;
        }
    }
}
