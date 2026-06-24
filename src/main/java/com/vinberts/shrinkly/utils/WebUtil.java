package com.vinberts.shrinkly.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 *
 */
public class WebUtil {

    public WebUtil() {
            throw new AssertionError();
    }

    public static String getClientIP(HttpServletRequest request) {
        String remoteip = request.getHeader("CF-Connecting-IP");
        if (remoteip == null) {
            final String xfHeader = request.getHeader("X-Forwarded-For");
            // X-Forwarded-For may be a comma-separated chain; the first entry is the client
            remoteip = xfHeader != null ? xfHeader.split(",")[0] : request.getRemoteAddr();
        }
        return remoteip;
    }
}
