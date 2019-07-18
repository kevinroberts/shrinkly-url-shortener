package com.vinberts.shrinkly.security.captcha;

import com.vinberts.shrinkly.web.errors.ReCaptchaInvalidException;

/**
 *
 */
public interface ICaptchaService {
    void processResponse(final String response) throws ReCaptchaInvalidException;

    String getReCaptchaSite();

    String getReCaptchaSecret();
}
