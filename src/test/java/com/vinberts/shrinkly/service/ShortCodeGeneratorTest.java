package com.vinberts.shrinkly.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShortCodeGeneratorTest {

    // Redis is not needed for encode(); pass null and a fixed test key.
    private final ShortCodeGenerator generator = new ShortCodeGenerator(null, "test-cipher-key");

    @Test
    void encodesToSevenCharsFromAlphabet() {
        String code = generator.encode(1L);
        assertEquals(7, code.length());
        for (char c : code.toCharArray()) {
            assertTrue(ShortCodeGenerator.ALPHABET.indexOf(c) >= 0, "unexpected char: " + c);
        }
    }

    @Test
    void firstHundredThousandCodesAreUniqueAndSevenChars() {
        // Exercises the cycle-walk branch in encode(): ~20% of Feistel outputs land in
        // [62^7, 2^42) and are re-permuted, so this asserts walked values still yield
        // unique, valid 7-char codes.
        Set<String> seen = new HashSet<>();
        for (long n = 1; n <= 100_000; n++) {
            String code = generator.encode(n);
            assertEquals(7, code.length(), "wrong length at n=" + n + ": " + code);
            assertTrue(seen.add(code), "duplicate code at n=" + n + ": " + code);
        }
    }

    @Test
    void encodeIsDeterministic() {
        assertEquals(generator.encode(12345L), generator.encode(12345L));
    }

    @Test
    void consecutiveCountersAreScattered() {
        String a = generator.encode(1000L);
        String b = generator.encode(1001L);
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) != b.charAt(i)) {
                diff++;
            }
        }
        assertTrue(diff >= 3, "expected scattering, got " + a + " vs " + b);
    }

    @Test
    void generatedCodesAvoidReservedWords() {
        for (long n = 1; n <= 100_000; n++) {
            assertFalse(ShortCodeGenerator.INVALID_HASHES.contains(generator.encode(n)));
        }
    }

    @Test
    void differentKeysProduceDifferentCodes() {
        ShortCodeGenerator other = new ShortCodeGenerator(null, "a-different-key");
        assertNotEquals(generator.encode(1L), other.encode(1L));
    }

    @Test
    @SuppressWarnings("unchecked")
    void nextCodeReturnsCodeForCounterValue() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.increment("shrinkly.url.counter")).thenReturn(42L);
        when(redis.hasKey(org.mockito.ArgumentMatchers.anyString())).thenReturn(false);

        ShortCodeGenerator gen = new ShortCodeGenerator(redis, "test-cipher-key");
        assertEquals(gen.encode(42L), gen.nextCode());
    }

    @Test
    @SuppressWarnings("unchecked")
    void nextCodeSkipsAlreadyTakenCode() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);
        // First counter value collides with an existing key; second is free.
        when(ops.increment("shrinkly.url.counter")).thenReturn(5L, 6L);

        ShortCodeGenerator gen = new ShortCodeGenerator(redis, "test-cipher-key");
        String taken = gen.encode(5L);
        String free = gen.encode(6L);
        when(redis.hasKey("shrinkly.url:" + taken)).thenReturn(true);
        when(redis.hasKey("shrinkly.url:" + free)).thenReturn(false);

        assertEquals(free, gen.nextCode());
        verify(ops, times(2)).increment("shrinkly.url.counter");
    }
}
