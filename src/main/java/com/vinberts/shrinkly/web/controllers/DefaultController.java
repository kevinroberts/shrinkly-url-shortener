package com.vinberts.shrinkly.web.controllers;

import com.google.common.primitives.Longs;
import com.vinberts.shrinkly.persistence.model.Abuse;
import com.vinberts.shrinkly.persistence.model.User;
import com.vinberts.shrinkly.persistence.model.UserShortUrl;
import com.vinberts.shrinkly.persistence.model.redis.ShortUrl;
import com.vinberts.shrinkly.repo.impl.RedisShortUrlRepository;
import com.vinberts.shrinkly.security.captcha.ICaptchaService;
import com.vinberts.shrinkly.service.ClickReconciliationService;
import com.vinberts.shrinkly.service.IAbuseService;
import com.vinberts.shrinkly.service.IInvitationService;
import com.vinberts.shrinkly.service.IShortUrlAnalyticsService;
import com.vinberts.shrinkly.service.IShortUrlService;
import com.vinberts.shrinkly.service.IUserService;
import com.vinberts.shrinkly.web.errors.MyResourceNotFoundException;
import com.vinberts.shrinkly.web.util.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Default controller class
 * defines web entry points for Shrinkly
 */
@Controller
@Slf4j
public class DefaultController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IAbuseService abuseService;

    @Autowired
    private IInvitationService invitationService;

    @Autowired
    private IShortUrlService shortUrlService;

    @Autowired
    private IShortUrlAnalyticsService shortUrlAnalyticsService;

    @Autowired
    private RedisShortUrlRepository shortUrlRepository;

    @Autowired
    private ICaptchaService captchaService;

    @Autowired
    private ClickReconciliationService clickReconciliationService;

    @Value("${shrinkly.base.url}")
    private String serverUrl;

    private static final int DEFAULT_PAGE_SIZE = 20;

    @GetMapping("/")
    public ModelAndView welcome(@PageableDefault(size = DEFAULT_PAGE_SIZE) @SortDefault.SortDefaults({
            @SortDefault(sort = "dateAdded", direction = Sort.Direction.DESC)
    }) Pageable pageable, Map<String, Object> model, HttpServletRequest request) {
        model.put("pageTitle", "Shrinkly URL Shortener");

        Authentication auth
                = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {
            // get user short urls
            final Page<UserShortUrl> resultPage = shortUrlService.findPaginated((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), pageable);

            if (pageable.getPageNumber() > resultPage.getTotalPages()) {
                throw new MyResourceNotFoundException();
            }

            // add clicks and links to the results
            for (UserShortUrl shortUrl: resultPage.getContent()) {
                Map<String, String> valuesMap = new HashMap<>();
                valuesMap.put("serverUrl", serverUrl);
                String decodedId = URLDecoder.decode(shortUrl.getShortUrl(), StandardCharsets.UTF_8);
                valuesMap.put("id", decodedId);
                StringSubstitutor finalUrlReplacement = new StringSubstitutor(valuesMap);
                String template = "${serverUrl}/${id}";
                String shortUrlStr = finalUrlReplacement.replace(template);
                // Read the live click count from Redis (source of truth); the DB column
                // is only reconciled in batch now, so it may lag between runs.
                shortUrl.setClicks(shortUrlRepository.getClicksForShortUrl(shortUrl.getShortUrl()));
                shortUrl.setShortCode(shortUrl.getShortUrl());
                shortUrl.setShortUrl(shortUrlStr);
            }


            int totalPages = resultPage.getTotalPages();
            if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
                model.put("pageNumbers", pageNumbers);
            }

            try {
                String message = (String)request.getSession().getAttribute("message");

                if (message != null) {
                    model.put("message", message);
                    request.getSession().removeAttribute("message");
                }
            } catch (Exception e) {
                log.error("could not get message att", e);
            }

            String dateSortDir = pageable.getSort().toString().equals("dateAdded: DESC") ? "DESC" : "ASC";
            String clicksSortDir = pageable.getSort().toString().equals("clicks: DESC") ? "DESC" : "ASC";
            if (pageable.getSort().toString().startsWith("dateAdded")) {
                clicksSortDir = "none";
            }
            if (pageable.getSort().toString().startsWith("clicks")) {
                dateSortDir = "none";
            }
            model.put("dateSortDir", dateSortDir );
            model.put("clicksSortDir", clicksSortDir);
            model.put("shortUrls", resultPage);
        }

        ModelAndView mv = new ModelAndView("home");
        mv.addAllObjects(model);

        return mv;

    }


    @RequestMapping(value = "/shrinklyAnalytics/{id}", method = RequestMethod.GET)
    public ModelAndView analytics(@PathVariable String id,  Map<String, Object> model) {

        Authentication auth
                = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {

            model.put("pageTitle", "Shrinkly Analytics");

            String encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8);

            UserShortUrl shortUrl = shortUrlService.findByUserAndShortUrl((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), encodedId);

            if (shortUrl != null) {
                shortUrl.setClicks(shortUrlRepository.getClicksForShortUrl(shortUrl.getShortUrl()));
                shortUrl.setShortCode(id);

                model.put("shortUrl", shortUrl);

                model.put("uniqueIps", shortUrlAnalyticsService.getUniqueIpsForShortUrl(encodedId));
                model.put("uniqueCountries", shortUrlAnalyticsService.getUniqueCountriesForShortUrl(encodedId));
                model.put("referrers", shortUrlAnalyticsService.getReferrersByShortUrl(encodedId));
                model.put("countryHits", shortUrlAnalyticsService.getCountryHitsByShortUrl(encodedId));
                Long percentMobile = shortUrlAnalyticsService.getPercentageOfDesktopVsMobile(encodedId);
                if (percentMobile > 0) {
                    model.put("percentMobile", percentMobile + "% Mobile");
                } else {
                    model.put("percentMobile", "100% Desktop");
                }

                ModelAndView mv = new ModelAndView("shrinklyAnalytics");
                mv.addAllObjects(model);

                return mv;

            } else {
                return new ModelAndView("error/404");
            }

        } else {
            return new ModelAndView("unauthorized");
        }

    }


    @GetMapping("/admin")
    public ModelAndView admin(@PageableDefault(size = DEFAULT_PAGE_SIZE) @SortDefault.SortDefaults({
            @SortDefault(sort = "dateAdded", direction = Sort.Direction.DESC)
    }) Pageable pageable, Map<String, Object> model) {
        final Page<Abuse> abuses = abuseService.findUnaddressedPaginated(pageable);
        model.put("pageTitle", "Shrinkly Admin");
        model.put("loggedUsers", userService.getUsersFromSessionRegistry());
        model.put("abuses", abuses);
        model.put("pendingInvitations", invitationService.getPendingInvitations());
        int totalPages = abuses.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
            model.put("pageNumbers", pageNumbers);
        }
        ModelAndView mv = new ModelAndView("admin");
        mv.addAllObjects(model);

        return mv;
    }

    @PostMapping("/admin/remove")
    ResponseEntity<Object> adminRemoveUrl(@RequestBody Map<String, Object> payload) {
        if (payload.containsKey("id")) {
            final String id = payload.get("id").toString();
            try {
                Long abuseId = Longs.tryParse(id, 10);
                Abuse abuse = abuseService.getAbuseById(abuseId);
                if (abuse == null) {
                    GenericResponse genericResponse = new GenericResponse("Please pass a valid id as a parameter.","","id");
                    return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
                }

                shortUrlService.deleteByShortUrl(abuse.getShortUrl());
                Optional<ShortUrl> optionalShortUrl = shortUrlRepository.getShortCodeIfExists(abuse.getShortUrl());
                if (optionalShortUrl.isPresent()) {
                    shortUrlRepository.removeShortCode(abuse.getShortUrl());
                }
                abuseService.markAbuseAddressed(abuseId);
                GenericResponse genericResponse = new GenericResponse("Successfully removed short url: " + abuse.getShortUrl(),"","id");
                return new ResponseEntity<>(genericResponse, HttpStatus.OK);
            } catch (Exception e) {
                GenericResponse genericResponse = new GenericResponse("Please pass a valid id as a parameter.","","id");
                return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
            }
        } else {
            GenericResponse genericResponse = new GenericResponse("Please pass a valid id as a parameter.","","id");
            return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/admin/dismiss")
    ResponseEntity<Object> adminDismissAbuse(@RequestBody Map<String, Object> payload) {
        if (payload.containsKey("id")) {
            final String id = payload.get("id").toString();
            try {
                Long abuseId = Longs.tryParse(id, 10);
                Abuse abuse = abuseService.getAbuseById(abuseId);
                if (abuse == null) {
                    GenericResponse genericResponse = new GenericResponse("Please pass a valid id as a parameter.","","id");
                    return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
                }

                abuseService.markAbuseAddressed(abuseId);
                GenericResponse genericResponse = new GenericResponse("Successfully dismissed abuse report for short url: " + abuse.getShortUrl(),"","id");
                return new ResponseEntity<>(genericResponse, HttpStatus.OK);
            } catch (Exception e) {
                GenericResponse genericResponse = new GenericResponse("Please pass a valid id as a parameter.","","id");
                return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
            }
        } else {
            GenericResponse genericResponse = new GenericResponse("Please pass a valid id as a parameter.","","id");
            return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/admin/deleteuser")
    ResponseEntity<Object> adminRemoveUser(@RequestBody Map<String, Object> payload) {
        if (payload.containsKey("id")) {
            final String id = payload.get("id").toString();
            try {
                Long userId = Longs.tryParse(id, 10);
                Optional<User> userOptional = userService.getUserByID(userId);
                if (userOptional.isPresent()) {
                    userService.deleteUser(userOptional.get());
                    GenericResponse genericResponse = new GenericResponse("Successfully removed user: " + userOptional.get().getUsername(),"","id");
                    return new ResponseEntity<>(genericResponse, HttpStatus.OK);
                } else {
                    GenericResponse genericResponse = new GenericResponse("Please pass a user id as a parameter.","","id");
                    return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
                }
            } catch (Exception e) {
                GenericResponse genericResponse = new GenericResponse("Please pass a valid id as a parameter.","","id");
                return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
            }
        } else {
            GenericResponse genericResponse = new GenericResponse("Please pass a valid id as a parameter.","","id");
            return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/admin/reindex")
    ResponseEntity<Object> adminReindex(@RequestBody Map<String, Object> payload) {
        int updated = clickReconciliationService.reconcileAll();

        GenericResponse genericResponse = new GenericResponse("Successfully re-indexed " + updated + " short urls","","id");
        return new ResponseEntity<>(genericResponse, HttpStatus.OK);
    }

    @GetMapping("/policy")
    public ModelAndView policy(Map<String, Object> model) {
        model.put("pageTitle", "Shrinkly URL Shortener Policies");
        ModelAndView mv = new ModelAndView("policy");
        mv.addAllObjects(model);

        return mv;
    }

    @GetMapping("/privacy")
    public ModelAndView privacy(Map<String, Object> model) {
        model.put("pageTitle", "Shrinkly Privacy Policy");

        ModelAndView mv = new ModelAndView("privacy");
        mv.addAllObjects(model);

        return mv;
    }

    @GetMapping("/reportAbuse")
    public ModelAndView abuse(Map<String, Object> model) {
        model.put("pageTitle", "Shrinkly URL Shortener");
        model.put("turnstileSiteKey", captchaService.getCaptchaSite());
        ModelAndView mv = new ModelAndView("reportAbuse");
        mv.addAllObjects(model);

        return mv;
    }

    @GetMapping("/user")
    public ModelAndView user(Map<String, Object> model) {
        model.put("pageTitle", "Shrinkly - User Home");
        ModelAndView mv = new ModelAndView("user");
        mv.addAllObjects(model);

        return mv;
    }

    @GetMapping("/login")
    public ModelAndView login(Map<String, Object> model) {
        model.put("pageTitle", "Shrinkly - Login");
        ModelAndView mv = new ModelAndView("login");
        mv.addAllObjects(model);

        return mv;
    }

    @GetMapping("/unauthorized")
    public ModelAndView error403(Map<String, Object> model) {
        model.put("pageTitle", "Shrinkly - Unauthorized");
        ModelAndView mv = new ModelAndView("error/403");
        mv.addAllObjects(model);

        return mv;
    }

}
