package com.vinberts.shrinkly.utils;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.RegexValidator;
import org.apache.commons.validator.routines.UrlValidator;

import javax.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

import static com.vinberts.shrinkly.service.impl.ShrinklyUrlHashServiceImpl.INVALID_HASHES;


/**
 *
 */
@Slf4j
public final class LinkUtil {
    public static final String REL_COLLECTION = "collection";
    public static final String REL_NEXT = "next";
    public static final String REL_PREV = "prev";
    public static final String REL_FIRST = "first";
    public static final String REL_LAST = "last";

    private static Set<String> urlShorteningServices = Sets.newHashSet("is.gd", "lc.chat",
            "j.mp", "goo.gl", "owl.ly", "bit.ly", "t.co", "xn--kn8h.to", "clk.im", "snip.ly",
            "shrinkly.net", "soo.gd", "s2r.co", "clicky.me", "tinyurl.com", "budurl.com");

    private LinkUtil() {
        throw new AssertionError();
    }

    /**
     * Creates a Link Header to be stored in the {@link HttpServletResponse} to provide Discoverability features to the user
     *
     * @param uri
     *            the base uri
     * @param rel
     *            the relative path
     *
     * @return the complete url
     */
    public static String createLinkHeader(final String uri, final String rel) {
        return "<" + uri + ">; rel=\"" + rel + "\"";
    }

    public static boolean isValidCustomAlias(final String alias) {
        // no whitespace or url reserved characters such as ; ? / = @
        RegexValidator reservedCharactersCheck = new RegexValidator("^[^;/?:@=&\\s]+");

        if (alias.length() > 50) {
            return false;
        }

        if (reservedCharactersCheck.isValid(alias) && !INVALID_HASHES.contains(alias)) {
            return true;
        }
        return false;
    }

    public static boolean isValidFullUrl(final String url) {
        final UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});
        if (url.startsWith("mailto:")) {
            EmailValidator emailValidator = EmailValidator.getInstance(false, true);
            // only support email plus subject
            String mailTo = StringUtils.removeStart(url, "mailto:");
            if (StringUtils.contains(mailTo, "?subject=")) {
                String[] parts = mailTo.split("\\?subject=");
                String emailPart = parts[0];
                if (emailValidator.isValid(emailPart)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                if (emailValidator.isValid(mailTo)) {
                    return true;
                } else {
                    return false;
                }
            }
        } else if (url.startsWith("tel:")) {
            String tel = StringUtils.removeStart(url, "tel:");
            RegexValidator regexValidator = new RegexValidator("([+(\\d]{1})(([\\d+() -.]){5,16})([+(\\d]{1})");
            if (regexValidator.isValid(tel)) {
                return true;
            } else {
                return false;
            }
        } else if (urlValidator.isValid(url)) {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                String domain = getDomainName(url).toLowerCase();
                if (urlShorteningServices.contains(domain)) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getDomainName(String url) {
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (URISyntaxException e) {
            return "";
        }
    }

    public static boolean isRedirectedUrl(String urlstr) {
        URL url;

        try {
            url = new URL(urlstr);
        } catch (MalformedURLException e) {
            log.error("Malformed url passed", urlstr);
            return false;
        }

        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setInstanceFollowRedirects(false);
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36");
            con.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            con.addRequestProperty("Referer", "https://shrinkly.net");
            con.connect();
            int resCode = con.getResponseCode();
            if (resCode == HttpURLConnection.HTTP_SEE_OTHER
                    || resCode == HttpURLConnection.HTTP_MOVED_PERM
                    || resCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                String Location = con.getHeaderField("Location");
                if (Location.startsWith("/")) {
                    Location = url.getProtocol() + "://" + url.getHost() + Location;
                }
                con.disconnect();
                log.info("Url is a redirect to location: " + Location);
                return true;
            } else {
                con.disconnect();
            }
        } catch (Exception e) {
            log.error("could not connect to user url " + url.toString(), e);
        }
        return false;
    }


}
