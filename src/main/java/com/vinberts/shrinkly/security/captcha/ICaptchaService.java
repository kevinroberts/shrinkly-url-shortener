package com.vinberts.shrinkly.security.captcha;

import com.vinberts.shrinkly.web.errors.CaptchaInvalidException;

/**
 *
 */
public interface ICaptchaService {
    void processResponse(final String response) throws CaptchaInvalidException;

    String getCaptchaSite();

    String getCaptchaSecret();
}
