package com.vinberts.shrinkly.utils;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public class WebUtil {

    public WebUtil() {
            throw new AssertionError();
    }

    public static String getClientIP(HttpServletRequest request) {
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
