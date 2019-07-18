package com.vinberts.shrinkly.service.impl;

import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.vinberts.shrinkly.service.ShrinklyUrlHashService;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 *
 */
public class ShrinklyUrlHashServiceImpl implements ShrinklyUrlHashService {


    public ShrinklyUrlHashServiceImpl() {}

    private final static String possibleChars = "ABCDEFGHIJKLMNPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz123456789-_";

    public final static Set<String> INVALID_HASHES = Sets.newHashSet("login", "user", "shrinklyAnalytics",
            "registration", "shrink", "admin", "invalidSession", "reportAbuse", "updatePassword", "registrationConfirm",
            "successRegister", "badUserToken", "forgetPassword", "policy", "unauthorized", "privacy");


    @Override
    public String generateHash(final String url) {
        final Integer hash = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).asInt();
        if (hash > 0) {
            return toCustomBase(hash, possibleChars);
        } else {
            return Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
        }
    }

    @Override
    public String generateRandomHash(Integer length) {
        //String possibleChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
        // 62 characters without 0 and O as they are easy to mix up
        StringBuilder returnHash = new StringBuilder();
        for (int i = 0; i < length; i++) {
            Double randomChar = Math.floor(Math.random() * possibleChars.length());
            returnHash.append(possibleChars.charAt(randomChar.intValue()));
        }
        // do not allow any reserved hashes
        if (INVALID_HASHES.contains(returnHash.toString())) {
            return this.generateRandomHash(length);
        }

        return returnHash.toString();
    }


    private String toCustomBase(final int num, final String base) {
        final int baseSize = base.length();
        if(num < baseSize) {
            return String.valueOf(base.charAt(num));
        }
        else {
            return toCustomBase(num / baseSize - 1, base) + base.charAt(num % baseSize);
        }
    }

}
