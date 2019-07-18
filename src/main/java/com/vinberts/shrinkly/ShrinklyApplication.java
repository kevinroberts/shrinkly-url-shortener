package com.vinberts.shrinkly;

import com.vinberts.shrinkly.events.event.PaginatedResultsRetrievedEvent;
import com.vinberts.shrinkly.persistence.model.Abuse;
import com.vinberts.shrinkly.persistence.model.User;
import com.vinberts.shrinkly.persistence.model.UserShortUrl;
import com.vinberts.shrinkly.persistence.model.redis.ShortUrl;
import com.vinberts.shrinkly.repo.impl.RedisShortUrlRepository;
import com.vinberts.shrinkly.security.captcha.ICaptchaService;
import com.vinberts.shrinkly.service.IAbuseService;
import com.vinberts.shrinkly.service.IShortUrlAnalyticsService;
import com.vinberts.shrinkly.service.IShortUrlService;
import com.vinberts.shrinkly.web.errors.MyResourceNotFoundException;
import com.vinberts.shrinkly.web.errors.UserNotAuthorizedException;
import com.vinberts.shrinkly.web.util.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import static com.vinberts.shrinkly.utils.LinkUtil.isValidCustomAlias;
import static com.vinberts.shrinkly.utils.LinkUtil.isValidFullUrl;
import static com.vinberts.shrinkly.utils.WebUtil.getClientIP;

@SpringBootApplication
@RestController
@Slf4j
@RequestMapping("/")
public class ShrinklyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShrinklyApplication.class, args);
    }

    @Autowired
    private Environment env;

    @Autowired
    private RedisShortUrlRepository shortUrlRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private IShortUrlService shortUrlService;

    @Autowired
    IAbuseService abuseService;

    @Autowired
    IShortUrlAnalyticsService analyticsService;

    @Autowired
    private ICaptchaService captchaService;

    @Value("${shrinkly.base.url}")
    private String serverUrl;

    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;

    @PostConstruct
    public void init() {
        log.info("Running in env mode: " + Arrays.toString(env.getActiveProfiles()));
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @PostMapping(value = "/shrink")
    public ResponseEntity<Object> save(@RequestBody Map<String, Object> payload) {
        log.debug("Hit /shrink endpoint");
        if (payload.containsKey("url")) {
            final String url = payload.get("url").toString();
            boolean isCustom = false;
            boolean isExpiryLink = false;
            LocalDateTime expiryDateTime = null;

            String customAlias = "";
            if (payload.containsKey("customAlias")) {
                customAlias = payload.get("customAlias").toString();
            }
            if (payload.containsKey("expireLink") && payload.containsKey("expiryDateTime")) {
                isExpiryLink = Boolean.parseBoolean(payload.get("expireLink").toString());
                if (isExpiryLink) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
                        expiryDateTime = LocalDateTime.parse(payload.get("expiryDateTime").toString(), formatter);
                    } catch (DateTimeParseException e) {
                        GenericResponse genericResponse = new GenericResponse("Invalid expiration date specified. Please select a new date.","","customExpiry");
                        return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
                    }
                    LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
                    final long diffSec = Duration.between(now, expiryDateTime).getSeconds();
                    if (diffSec <= 0L) {
                        GenericResponse genericResponse = new GenericResponse("Invalid expiration date specified. (Past Date)","","customExpiry");
                        return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
                    }
                }
            }

            if (isValidFullUrl(url) && url.length() <= 2000) {
                ShortUrl shortUrl;

                if (customAlias.length() != 0 && customAlias.length() > 2) {
                    // validate and attempt to create a new short url with custom alias
                    if (customAlias.length() > 50) {
                        GenericResponse genericResponse = new GenericResponse("Please select a custom alias less than 50 characters","","customAlias");
                        return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
                    }
                    if (isValidCustomAlias(customAlias)) {
                        try {
                            String newCustomShortCode = URLEncoder.encode(customAlias, StandardCharsets.UTF_8.toString());
                            if (isExpiryLink) {
                                shortUrl = shortUrlRepository.encodeLongUrlCustomWithExpiration(url, newCustomShortCode, expiryDateTime);
                            } else {
                                shortUrl = shortUrlRepository.encodeLongUrlCustom(url, newCustomShortCode);
                            }
                            if (shortUrl == null) {
                                GenericResponse genericResponse = new GenericResponse("The alias you entered has already been used by someone. Please pick something else.","","customAlias");
                                return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
                            }
                            isCustom = true;
                        } catch (UnsupportedEncodingException e) {
                            log.error("Invalid custom alias submitted, " + customAlias, e);
                            GenericResponse genericResponse = new GenericResponse("You entered an invalid custom alias, please select something else.","","customAlias");
                            return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
                        }
                    } else {
                        GenericResponse genericResponse = new GenericResponse("You entered an invalid custom alias, please select something else.","","customAlias");
                        return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
                    }
                } else {
                    shortUrl = getShortUrlForRequest(url, isExpiryLink, expiryDateTime);
                }

                // get final formatted shrinkly url
                Map<String, String> valuesMap = new HashMap<>();
                valuesMap.put("serverUrl", serverUrl);
                if (isCustom) {
                    valuesMap.put("id", customAlias);
                } else {
                    valuesMap.put("id", shortUrl.getShortenedKey());
                }
                StringSubstitutor finalUrlReplacement = new StringSubstitutor(valuesMap);
                String template = "${serverUrl}/${id}";
                String shortUrlStr = finalUrlReplacement.replace(template);

                // if user is auth-ed store their new short url
                Authentication auth
                        = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {

                    final User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    final UserShortUrl userShortUrl = new UserShortUrl();

                    if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().size() == 1) {
                        boolean isPasswordResetUser = false;
                        for (GrantedAuthority userAuth: SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
                            if (userAuth.getAuthority().equals("CHANGE_PASSWORD_PRIVILEGE")) {
                                isPasswordResetUser = true;
                            }
                        }
                        if (isPasswordResetUser) {
                            GenericResponse genericResponse = new GenericResponse("Your account is currently pending a password reset request. Please complete the reset to continue using your account.","","customAlias");
                            return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
                        }
                    }

                    // if is custom don't check for existing url
                    if (isCustom) {
                        saveNewShortUrl(shortUrl.getUrl(), shortUrl.getShortenedKey(), user, true, userShortUrl, isExpiryLink, expiryDateTime);
                    } else {
                        // check if they've already created this short code
                        final UserShortUrl existingShortUrlByKey = shortUrlService.findByUserAndShortUrl(user, shortUrl.getShortenedKey());

                        if (existingShortUrlByKey == null) {
                            saveNewShortUrl(shortUrl.getUrl(), shortUrl.getShortenedKey(), user, false, userShortUrl, isExpiryLink, expiryDateTime);
                        } else {
                            // check if this url has already been added by this user
                            final UserShortUrl existingShortUrl = shortUrlService.findByUserAndFullUrl(user, shortUrl.getUrl());
                            if (existingShortUrl == null) {
                                saveNewShortUrl(shortUrl.getUrl(), shortUrl.getShortenedKey(), user, false, userShortUrl, isExpiryLink, expiryDateTime);
                            }
                        }
                    }
                }

                if (log.isDebugEnabled()) {
                    log.debug("Stored " + shortUrlRepository.getTotalStoredUrls() + " urls");
                } else {
                    // increment count of total short urls
                    shortUrlRepository.incrementCount();
                }
                shortUrl.setClicks(shortUrlRepository.getClicksForShortUrl(shortUrl.getShortenedKey()));
                shortUrl.setShortUrl(shortUrlStr);
                return new ResponseEntity<>(shortUrl, HttpStatus.CREATED);
            } else {
                GenericResponse genericResponse = new GenericResponse("Your URL was not valid. Please submit a valid URl.","","url");
                return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
            }
        } else {
            GenericResponse genericResponse = new GenericResponse("Please pass a valid url as a parameter.","","url");
            return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping(value = "/reportAbuse")
    public ResponseEntity<Object> abusePost(@RequestBody Map<String, Object> payload, HttpServletRequest request) throws UnsupportedEncodingException {
        log.debug("Hit /reportAbuse post endpoint");

        if (!payload.containsKey("g-recaptcha-response")) {
            GenericResponse genericResponse = new GenericResponse("Please prove that you are not a robot.","","captcha");
            return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
        }

        captchaService.processResponse(payload.get("g-recaptcha-response").toString());

        if (payload.containsKey("shortCode")) {

            String shortCode = payload.get("shortCode").toString();

            if (shortCode.length() > 50 || shortCode.length() < 2) {
                GenericResponse genericResponse = new GenericResponse("Please pass a valid short code as a parameter.","","shortCode");
                return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
            }

            String decodedId = URLEncoder.encode(shortCode, StandardCharsets.UTF_8.toString());
            Optional<ShortUrl> optionalShortUrl = shortUrlRepository.getShortCodeIfExists(decodedId);

            if (optionalShortUrl.isPresent()) {
                Abuse abuse = new Abuse();
                abuse.setShortUrl(decodedId);
                abuse.setFullUrl(optionalShortUrl.get().getUrl());
                abuse.setIpAddress(getClientIP(request));

                Authentication auth
                        = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {
                    final User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                    abuse.setUser(user);
                }

                abuseService.saveNewAbuse(abuse);
                GenericResponse genericResponse = new GenericResponse("Your abuse report was successfully recorded. We will review it and determine the appropriate action.");
                return new ResponseEntity<>(genericResponse, HttpStatus.CREATED);

            } else {
                GenericResponse genericResponse = new GenericResponse("Please pass a valid short code as a parameter.","","shortCode");
                return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
            }
        } else {
            GenericResponse genericResponse = new GenericResponse("Please pass a valid short code as a parameter.","","shortCode");
            return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/shrink/api/list")
    @ResponseBody
    public Page<UserShortUrl> findPaginatedShortUrls(@PageableDefault(page = DEFAULT_PAGE_NUMBER, size = DEFAULT_PAGE_SIZE) @SortDefault.SortDefaults({
            @SortDefault(sort = "dateAdded", direction = Sort.Direction.DESC)
    }) Pageable pageable, final UriComponentsBuilder uriBuilder,
                                                     final HttpServletResponse response) throws UnsupportedEncodingException {
        Authentication auth
                = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            throw new UserNotAuthorizedException();
        }

        final Page<UserShortUrl> resultPage = shortUrlService.findPaginated((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), pageable);

        if (pageable.getPageNumber() > resultPage.getTotalPages()) {
            throw new MyResourceNotFoundException();
        }
        // add links to next page to Header
        eventPublisher.publishEvent(new PaginatedResultsRetrievedEvent<>(UserShortUrl.class, uriBuilder, response,
                pageable.getPageNumber(), resultPage.getTotalPages(), pageable.getPageSize()));

        // add clicks and links to the results
        for (UserShortUrl shortUrl: resultPage.getContent()) {
            Map<String, String> valuesMap = new HashMap<>();
            valuesMap.put("serverUrl", serverUrl);
            String decodedId = URLDecoder.decode(shortUrl.getShortUrl(), StandardCharsets.UTF_8.name());
            valuesMap.put("id", decodedId);
            StringSubstitutor finalUrlReplacement = new StringSubstitutor(valuesMap);
            String template = "${serverUrl}/${id}";
            String shortUrlStr = finalUrlReplacement.replace(template);
            shortUrl.setClicks(shortUrlRepository.getClicksForShortUrl(shortUrl.getShortUrl()));
            shortUrl.setShortCode(shortUrl.getShortUrl());
            shortUrl.setShortUrl(shortUrlStr);
        }

        return resultPage;
    }

    @RequestMapping(value = "/robots.txt")
    public void robots(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setContentType("text/plain");
            response.getWriter().write("User-agent: *\nDisallow: /user\n");
        } catch (IOException e) {
            log.error("Exception writing robots txt", e);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public void redirect(@PathVariable String id,HttpServletRequest request, HttpServletResponse resp) throws Exception {
        String encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8.toString());
        Optional<ShortUrl> shortUrlOptional = shortUrlRepository.decodeAndIncrementShortUrl(encodedId);
        if (shortUrlOptional.isPresent()) {
            log.debug("Got short code from request: " + shortUrlOptional.get().toString());
            // add analytics data to db
            LocalDateTime expiryDate = LocalDateTime.now();

            if (shortUrlOptional.get().getExpirationInSeconds() > 0) {
                expiryDate = expiryDate.plusSeconds(shortUrlOptional.get().getExpirationInSeconds());
            }

            analyticsService.addAnalyticsForShortUrl(shortUrlOptional.get().getShortenedKey(),
                    Optional.ofNullable(request.getHeader("User-Agent")),
                    getClientIP(request),
                    Optional.ofNullable(request.getHeader("referer")),
                    shortUrlOptional.get().getExpirationInSeconds() > 0 ? Optional.of(expiryDate) : Optional.empty());
            resp.sendRedirect(shortUrlOptional.get().getUrl());
        } else
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void saveNewShortUrl(String fullUrl, String shortUrl, User user, boolean isCustom,
                                 UserShortUrl userShortUrl, boolean isExpiryLink, LocalDateTime expiryDateTime) {
        userShortUrl.setFullUrl(fullUrl);
        userShortUrl.setShortUrl(shortUrl);
        userShortUrl.setUser(user);
        userShortUrl.setCustom(isCustom);

        if (isExpiryLink) {
            userShortUrl.setExpiryDate(expiryDateTime);
        }

        shortUrlService.saveNewShortUrl(userShortUrl);
        log.debug("Saved new short url for user: " + user.getUsername() + " is custom: " + isCustom);
    }

    private ShortUrl getShortUrlForRequest(String url, boolean isExpiryLink, LocalDateTime expiryDate) {
        ShortUrl shortUrl;
        int size;
        BigInteger totalStored = shortUrlRepository.getTotalStoredUrls();
        if (totalStored.compareTo(new BigInteger("3500")) < 0) {
            size = 2;
        } else if (totalStored.compareTo(new BigInteger("3500")) > 0
                && totalStored.compareTo(new BigInteger("230000")) < 0 ) {
            size = 3;
        } else if (totalStored.compareTo(new BigInteger("230000")) > 0
                && totalStored.compareTo(new BigInteger("14000000")) < 0) {
            size = 4;
        } else if (totalStored.compareTo(new BigInteger("14000000")) > 0
                && totalStored.compareTo(new BigInteger("900000000")) < 0) {
            size = 5;
        } else {
            size = 6;
        }
        if (isExpiryLink) {
            shortUrl = shortUrlRepository.encodeLongUrlWithExpiration(url, size, expiryDate);
        } else {
            shortUrl = shortUrlRepository.encodeLongUrl(url, size);
        }
        return shortUrl;
    }

}
