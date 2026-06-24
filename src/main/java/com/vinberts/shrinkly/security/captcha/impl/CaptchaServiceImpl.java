package com.vinberts.shrinkly.security.captcha.impl;

import com.vinberts.shrinkly.security.captcha.CaptchaAttemptService;
import com.vinberts.shrinkly.security.captcha.CaptchaSettings;
import com.vinberts.shrinkly.security.captcha.ICaptchaService;
import com.vinberts.shrinkly.security.captcha.TurnstileResponse;
import com.vinberts.shrinkly.web.errors.CaptchaInvalidException;
import com.vinberts.shrinkly.web.errors.CaptchaUnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import java.net.URI;
import java.util.regex.Pattern;

import static com.vinberts.shrinkly.utils.WebUtil.getClientIP;

/**
 *
 */
@Slf4j
@Service("captchaService")
public class CaptchaServiceImpl implements ICaptchaService {

    private static final URI VERIFY_URI = URI.create("https://challenges.cloudflare.com/turnstile/v0/siteverify");

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private CaptchaSettings captchaSettings;

    @Autowired
    private CaptchaAttemptService captchaAttemptService;

    @Autowired
    private RestOperations restTemplate;

    // Turnstile tokens are dot-separated base64url segments, at most 2048 characters
    private static final Pattern RESPONSE_PATTERN = Pattern.compile("[A-Za-z0-9._-]+");
    private static final int MAX_RESPONSE_LENGTH = 2048;

    @Override
    public void processResponse(final String response) throws CaptchaInvalidException {
        log.debug("Attempting to validate response {}", response);

        if (captchaAttemptService.isBlocked(getClientIP(request))) {
            throw new CaptchaInvalidException("Client exceeded maximum number of failed attempts");
        }

        if (!responseSanityCheck(response)) {
            throw new CaptchaInvalidException("Response contains invalid characters");
        }

        final MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secret", getCaptchaSecret());
        body.add("response", response);
        body.add("remoteip", getClientIP(request));

        try {
            final TurnstileResponse turnstileResponse = restTemplate.postForObject(VERIFY_URI, body, TurnstileResponse.class);
            log.debug("Turnstile response: {} ", turnstileResponse.toString());

            if (!turnstileResponse.isSuccess()) {
                if (turnstileResponse.hasClientError()) {
                    captchaAttemptService.captchaFailed(getClientIP(request));
                }
                throw new CaptchaInvalidException("Captcha was not successfully validated");
            }
        } catch (RestClientException rce) {
            throw new CaptchaUnavailableException("Registration unavailable at this time. Please try again later.", rce);
        }
        captchaAttemptService.captchaSucceeded(getClientIP(request));
    }

    private boolean responseSanityCheck(final String response) {
        return StringUtils.hasLength(response)
                && response.length() <= MAX_RESPONSE_LENGTH
                && RESPONSE_PATTERN.matcher(response).matches();
    }

    @Override
    public String getCaptchaSite() {
        return captchaSettings.getSite();
    }

    @Override
    public String getCaptchaSecret() {
        return captchaSettings.getSecret();
    }

}
