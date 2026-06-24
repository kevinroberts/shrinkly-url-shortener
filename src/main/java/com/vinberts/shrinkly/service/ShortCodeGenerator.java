package com.vinberts.shrinkly.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Set;

/**
 * Allocates short codes by passing a monotonic Redis counter through a keyed
 * Feistel permutation and base-62 encoding it to a fixed 7-character width.
 * The permutation is a bijection, so distinct counter values never collide.
 *
 * <p>The secret key makes the sequence non-sequential and not predictable
 * without the key, which defeats casual enumeration of other users' links.
 * This is a lightweight obfuscation suited to a URL shortener's threat model,
 * not a cryptographic access-control guarantee.
 */
@Component
public class ShortCodeGenerator {

    /** Reserved path segments a generated code must never equal. */
    public static final Set<String> INVALID_HASHES = Set.of("login", "user", "shrinklyAnalytics",
            "registration", "shrink", "admin", "invalidSession", "reportAbuse", "updatePassword",
            "registrationConfirm", "successRegister", "badUserToken", "forgetPassword", "policy",
            "unauthorized", "privacy");

    // 62 chars: A-Z without O, a-z, 1-9, '-', '_' (avoids the ambiguous 0/O pair).
    static final String ALPHABET =
            "ABCDEFGHIJKLMNPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz123456789-_";
    private static final int BASE = 62;
    private static final int CODE_LENGTH = 7;
    private static final long DOMAIN = 3_521_614_606_208L; // 62^7
    private static final int HALF_BITS = 21;               // 42-bit block, two 21-bit halves
    private static final int HALF_MASK = (1 << HALF_BITS) - 1;
    private static final int ROUNDS = 6;

    private static final String COUNTER_KEY = "shrinkly.url.counter";
    private static final String URL_KEY_PREFIX = "shrinkly.url:";

    private final StringRedisTemplate redisTemplate;
    private final byte[] cipherKey;
    private final ThreadLocal<Mac> macThreadLocal;

    public ShortCodeGenerator(final StringRedisTemplate redisTemplate,
                              @Value("${shrinkly.shortcode.cipher-key}") final String cipherKey) {
        this.redisTemplate = redisTemplate;
        this.cipherKey = cipherKey.getBytes(StandardCharsets.UTF_8);
        this.macThreadLocal = ThreadLocal.withInitial(() -> {
            try {
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(this.cipherKey, "HmacSHA256"));
                return mac;
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException("HMAC-SHA256 unavailable", e);
            }
        });
    }

    /**
     * Allocate the next unique short code: INCR the counter, encode it, and
     * skip the (astronomically rare) case where the result collides with a
     * reserved word or an existing custom alias. A skipped code permanently
     * consumes its counter slot (retrying the same slot would require a CAS and
     * is not worth it for an event this rare), leaving a harmless gap in the
     * sequence.
     */
    public String nextCode() {
        while (true) {
            final Long n = redisTemplate.opsForValue().increment(COUNTER_KEY);
            if (n == null) {
                throw new IllegalStateException("Redis INCR returned null for " + COUNTER_KEY);
            }
            final String code = encode(n);
            if (INVALID_HASHES.contains(code)
                    || Boolean.TRUE.equals(redisTemplate.hasKey(URL_KEY_PREFIX + code))) {
                continue;
            }
            return code;
        }
    }

    /**
     * Pure function of (n, cipherKey): map a counter value to a scattered,
     * fixed-width 7-character code. No randomness or shared state, so the same
     * inputs always yield the same code.
     */
    String encode(final long n) {
        long m = Math.floorMod(n, DOMAIN);
        int steps = 0;
        do {
            if (++steps > 100) {
                throw new IllegalStateException(
                        "Feistel cycle-walk exceeded limit; check cipher key for fixed-point cycles");
            }
            m = feistel(m);
        } while (m >= DOMAIN); // cycle-walk back into [0, DOMAIN)
        return base62(m);
    }

    private long feistel(final long input) {
        int left = (int) ((input >> HALF_BITS) & HALF_MASK);
        int right = (int) (input & HALF_MASK);
        for (int round = 0; round < ROUNDS; round++) {
            int next = (left ^ roundFunction(round, right)) & HALF_MASK;
            left = right;
            right = next;
        }
        return (((long) left) << HALF_BITS) | right;
    }

    private int roundFunction(final int round, final int half) {
        Mac mac = macThreadLocal.get();
        byte[] data = ByteBuffer.allocate(8).putInt(round).putInt(half).array();
        byte[] digest = mac.doFinal(data);
        int value = ((digest[0] & 0xff) << 16) | ((digest[1] & 0xff) << 8) | (digest[2] & 0xff);
        return value & HALF_MASK;
    }

    private static String base62(final long value) {
        char[] buf = new char[CODE_LENGTH];
        long v = value;
        for (int i = CODE_LENGTH - 1; i >= 0; i--) {
            buf[i] = ALPHABET.charAt((int) (v % BASE));
            v /= BASE;
        }
        return new String(buf);
    }
}
