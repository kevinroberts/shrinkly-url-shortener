package com.vinberts.shrinkly.web.controllers;

import com.vinberts.shrinkly.persistence.model.User;
import com.vinberts.shrinkly.persistence.model.UserShortUrl;
import com.vinberts.shrinkly.repo.impl.RedisShortUrlRepository;
import com.vinberts.shrinkly.service.IShortUrlAnalyticsService;
import com.vinberts.shrinkly.service.IShortUrlService;
import com.vinberts.shrinkly.service.IUserService;
import com.vinberts.shrinkly.web.errors.MyResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private IShortUrlService shortUrlService;

    @Autowired
    private IShortUrlAnalyticsService shortUrlAnalyticsService;

    @Autowired
    private RedisShortUrlRepository shortUrlRepository;

    @Value("${shrinkly.base.url}")
    private String serverUrl;

    private static final int DEFAULT_PAGE_SIZE = 20;

    @GetMapping("/")
    public ModelAndView welcome(@PageableDefault(size = DEFAULT_PAGE_SIZE) @SortDefault.SortDefaults({
            @SortDefault(sort = "dateAdded", direction = Sort.Direction.DESC)
    }) Pageable pageable, Map<String, Object> model, HttpServletRequest request) throws UnsupportedEncodingException {
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
                String decodedId = URLDecoder.decode(shortUrl.getShortUrl(), StandardCharsets.UTF_8.name());
                valuesMap.put("id", decodedId);
                StringSubstitutor finalUrlReplacement = new StringSubstitutor(valuesMap);
                String template = "${serverUrl}/${id}";
                String shortUrlStr = finalUrlReplacement.replace(template);
                shortUrl.setShortCode(shortUrl.getShortUrl());
                shortUrl.setClicks(shortUrlRepository.getClicksForShortUrl(shortUrl.getShortUrl()));
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

            model.put("dateSortDir", pageable.getSort().toString().equals("dateAdded: DESC") ? "DESC" : "ASC" );
            model.put("shortUrls", resultPage);
        }

        ModelAndView mv = new ModelAndView("home");
        mv.addAllObjects(model);

        return mv;

    }


    @RequestMapping(value = "/shrinklyAnalytics/{id}", method = RequestMethod.GET)
    public ModelAndView analytics(@PathVariable String id,  Map<String, Object> model) throws UnsupportedEncodingException {

        Authentication auth
                = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {

            model.put("pageTitle", "Shrinkly Analytics");

            String encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8.toString());

            UserShortUrl shortUrl = shortUrlService.findByUserAndShortUrl((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), encodedId);

            if (shortUrl != null) {
                shortUrl.setClicks(shortUrlRepository.getClicksForShortUrl(shortUrl.getShortUrl()));
                shortUrl.setShortCode(id);

                model.put("shortUrl", shortUrl);

                model.put("uniqueIps", shortUrlAnalyticsService.getUniqueIpsForShortUrl(encodedId));
                model.put("uniqueCountries", shortUrlAnalyticsService.getUniqueCountriesForShortUrl(encodedId));
                model.put("referrers", shortUrlAnalyticsService.getReferrersByShortUrl(encodedId));
                model.put("countryHits", shortUrlAnalyticsService.getCountryHitsByShortUrl(encodedId));
                Long percentageMobile = shortUrlAnalyticsService.getPercentageOfDesktopVsMobile(encodedId);
                DecimalFormat df = new DecimalFormat("##.##%");
                if (percentageMobile > 0) {
                    model.put("percentMobile", df.format(percentageMobile) + " Mobile");
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
    public ModelAndView admin(Map<String, Object> model) {
        model.put("pageTitle", "Shrinkly Admin");
        model.put("loggedUsers", userService.getUsersFromSessionRegistry());
        ModelAndView mv = new ModelAndView("admin");
        mv.addAllObjects(model);

        return mv;
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
