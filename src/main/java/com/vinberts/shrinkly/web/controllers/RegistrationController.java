package com.vinberts.shrinkly.web.controllers;

import com.blueconic.browscap.Capabilities;
import com.google.common.collect.Maps;
import com.vinberts.shrinkly.email.ShrinklyMailClient;
import com.vinberts.shrinkly.email.mailPojos.MailLink;
import com.vinberts.shrinkly.persistence.model.User;
import com.vinberts.shrinkly.persistence.model.VerificationToken;
import com.vinberts.shrinkly.registration.OnRegistrationCompleteEvent;
import com.vinberts.shrinkly.security.ISecurityUserService;
import com.vinberts.shrinkly.security.captcha.ICaptchaService;
import com.vinberts.shrinkly.service.IUserService;
import com.vinberts.shrinkly.service.UserAgentParserService;
import com.vinberts.shrinkly.service.impl.UserDetailsServiceImpl;
import com.vinberts.shrinkly.web.dto.PasswordDto;
import com.vinberts.shrinkly.web.dto.UserDto;
import com.vinberts.shrinkly.web.errors.InvalidOldPasswordException;
import com.vinberts.shrinkly.web.errors.UserNotAuthorizedException;
import com.vinberts.shrinkly.web.util.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 *
 */
@Controller
@Slf4j
public class RegistrationController {


    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private IUserService userService;

    @Autowired
    private ISecurityUserService securityUserService;

    @Autowired
    private Environment env;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ShrinklyMailClient mailClient;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ICaptchaService captchaService;

    @Autowired
    private UserAgentParserService userAgentParserService;

    @Value("${shrinkly.base.url}")
    private String serverUrl;

    // Registration

    @GetMapping("/registration")
    public ModelAndView registrationPage(Map<String, Object> model) {
        model.put("pageTitle", "Shrinkly - New User Sign up");

        ModelAndView mv = new ModelAndView("registration");
        mv.addAllObjects(model);

        return mv;
    }

    // Successful registration page

    @GetMapping("/successRegister")
    public ModelAndView registrationSuccessPage(Map<String, Object> model) {
        model.put("pageTitle", "Shrinkly - Thanks for signing up");

        ModelAndView mv = new ModelAndView("successRegister");
        mv.addAllObjects(model);

        return mv;
    }

    //badUserToken

    @GetMapping("/badUserToken")
    public ModelAndView registrationBadTokenPage(Map<String, Object> model) {
        model.put("pageTitle", "Shrinkly - Expired token");

        ModelAndView mv = new ModelAndView("badUserToken");
        mv.addAllObjects(model);

        return mv;
    }

    //reset password

    @GetMapping("/forgetPassword")
    public ModelAndView resetUserPasswordPage(Map<String, Object> model) {
        model.put("pageTitle", "Shrinkly - Forgotten Password");

        ModelAndView mv = new ModelAndView("forgetPassword");
        mv.addAllObjects(model);

        return mv;
    }

    @GetMapping("/updatePassword")
    public ModelAndView updatePasswordPage(Map<String, Object> model) {
        model.put("pageTitle", "Shrinkly - Update Password");

        ModelAndView mv = new ModelAndView("updatePassword");
        mv.addAllObjects(model);

        return mv;
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse registerUserAccount(@Valid final UserDto accountDto, final HttpServletRequest request) {
        log.debug("Registering user account with information: {}", accountDto);

        final String response = request.getParameter("g-recaptcha-response");

        captchaService.processResponse(response);

        final User registered = userService.registerNewUserAccount(accountDto);
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered, request.getLocale(), getAppUrl(request)));
        return new GenericResponse("success");
    }

    @RequestMapping(value = "/registrationConfirm", method = RequestMethod.GET)
    public ModelAndView confirmRegistration(final HttpServletRequest request, final Model model, @RequestParam("token") final String token) throws UnsupportedEncodingException {
        Locale locale = request.getLocale();
        final String result = userService.validateVerificationToken(token);
        if (result.equals("valid")) {
            final User user = userService.getUser(token);
            // if (user.isUsing2FA()) {
            // model.addAttribute("qr", userService.generateQRUrl(user));
            // return "redirect:/qrcode.html?lang=" + locale.getLanguage();
            // }
            authWithoutPassword(user);
            // remove the verification token now that it has been used successfully
            userService.expireVerificationToken(token);
            request.getSession().setAttribute("message", messageSource.getMessage("message.accountVerified", null, locale));

            ModelAndView mv = new ModelAndView("redirect:/?lang=" + locale.getLanguage());

            return mv;

        }

        model.addAttribute("message", messageSource.getMessage("auth.message." + result, null, locale));
        model.addAttribute("expired", "expired".equals(result));
        model.addAttribute("token", token);
        if (result.equals("validAndUsed")) {
            ModelAndView mv = new ModelAndView("redirect:/?lang=" + locale.getLanguage());

            return mv;
        }

        ModelAndView mv = new ModelAndView("redirect:/badUserToken?lang=" + locale.getLanguage());

        return mv;
    }

    @RequestMapping(value = "/user/resendRegistrationToken", method = RequestMethod.GET)
    @ResponseBody
    public GenericResponse resendRegistrationToken(final HttpServletRequest request, @RequestParam("token") final String existingToken) {
        final VerificationToken newToken = userService.generateNewVerificationToken(existingToken);
        final User user = userService.getUser(newToken.getToken());
        SimpleMailMessage newTokenEmail = constructResendVerificationTokenEmail(getAppUrl(request), request.getLocale(), newToken, user);

        // construct email context
        HashMap<String, Object> emailContext = Maps.newHashMap();

        emailContext.put("userGreeting", "Hi " + user.getUsername() + ", ");
        emailContext.put("mailBody1", "To complete your sign up, please verify your email:");
        emailContext.put("plainText", "Activate your Shrinkly.net account: " + newTokenEmail.getText());
        emailContext.put("mailBody3", "Or copy this link in your web browser<br><a href=\"" +
                newTokenEmail.getText() + "\">" + newTokenEmail.getText() + "</a><br>Thanks,<br>The Shrinkly Team" );
        emailContext.put("ctaLink", new MailLink(newTokenEmail.getText(), "Verify Email"));

        mailClient.prepareAndSend(newTokenEmail.getTo()[0], newTokenEmail.getSubject(), emailContext);
        return new GenericResponse(messageSource.getMessage("message.resendToken", null, request.getLocale()));
    }


    // Reset password
    @RequestMapping(value = "/user/resetPassword", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse resetPassword(final HttpServletRequest request, @RequestParam("email") final String userEmail) {
        final User user = userService.findUserByEmail(userEmail);
        if (user != null) {
            final String token = new BigInteger(200, new SecureRandom()).toString(36);
            userService.createPasswordResetTokenForUser(user, token);
            SimpleMailMessage resetEmail = constructResetTokenEmail(getAppUrl(request), request.getLocale(), token, user);
            String userAgentStr = request.getHeader("User-Agent");

            // construct resetEmail
            HashMap<String, Object> emailContext = Maps.newHashMap();
            emailContext.put("ctaLink", new MailLink(resetEmail.getText(), "Reset your Password"));
            emailContext.put("plainText", "Reset your Shrinkly.net password: " + resetEmail.getText());
            emailContext.put("userGreeting", "Hi " + user.getUsername() + ", ");
            emailContext.put("mailBody1", "You recently requested to reset your password for your Shrinkly.net account. Use the button below to reset it. <strong>This password reset is only valid for the next 24 hours.</strong>");

            final Capabilities capabilities = userAgentParserService.getCapabilities(userAgentStr);
            final String browser = capabilities.getBrowser();
            //final String browserType = capabilities.getBrowserType();
            //final String deviceType = capabilities.getDeviceType();
            final String platform = StringEscapeUtils.escapeEcmaScript(capabilities.getPlatform());
            final String platformVersion = capabilities.getPlatformVersion();

            emailContext.put("mailBody3", " For security, this request was received from a " + platform + " version " + platformVersion +  " device using " + browser + ". If you did not request a password reset, please ignore this email or <a href=\"mailto:help@shrinkly.net\">contact support</a> if you have questions.<br>\n" +
                        "                      <p>Thanks,\n" +
                        "                        <br>The Shrinkly.net Team</p>");



            mailClient.prepareAndSend(resetEmail.getTo()[0], resetEmail.getSubject(), emailContext);
        }
        return new GenericResponse(messageSource.getMessage("message.resetPasswordEmail", null, request.getLocale()));
    }

    // Reset password
    @RequestMapping(value = "/user/remove", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse deleteAccount(final HttpServletRequest request, final HttpServletResponse response) throws UnsupportedEncodingException {

        Authentication auth
                = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {
            final User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            SimpleMailMessage removalEmail = constructEmail("Your account has been removed", "", user);

            HashMap<String, Object> emailContext = Maps.newHashMap();
            emailContext.put("plainText", "This is just a confirmation email that your Shrinkly.net account for the username " + user.getUsername() + " has been deleted.");
            emailContext.put("userGreeting", "Hi " + user.getUsername() + ", ");
            emailContext.put("mailBody1", "You recently requested to remove your Shrinkly.net account. This email is just to confirm that your account has been successfully deleted.");

            userService.deleteUser(user);

            SecurityContextHolder.getContext().setAuthentication(null);
            mailClient.prepareAndSend(removalEmail.getTo()[0], removalEmail.getSubject(), emailContext);

            return new GenericResponse("Your account was successfully removed.");
        } else {
            throw new UserNotAuthorizedException();
        }
    }



    // update user password from old password
    @RequestMapping(value = "/user/updatePassword", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse changeUserPassword(final Locale locale, @Valid PasswordDto passwordDto) {
        final User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!userService.checkIfValidOldPassword(user, passwordDto.getOldPassword())) {
            throw new InvalidOldPasswordException();
        }
        userService.changeUserPassword(user, passwordDto.getNewPassword());
        return new GenericResponse(messageSource.getMessage("message.updatePasswordSuc", null, locale));
    }

    // change user password process from email reset
    @RequestMapping(value = "/user/changePassword", method = RequestMethod.GET)
    public ModelAndView showChangePasswordPage(final Locale locale, final Model model, @RequestParam("id") final long id, @RequestParam("token") final String token) {
        final Optional<String> result = securityUserService.validatePasswordResetToken(id, token);
        if (result.isPresent()) {
            model.addAttribute("message", messageSource.getMessage("auth.message." + result.get(), null, locale));

            ModelAndView mv = new ModelAndView("redirect:/?lang=" + locale.getLanguage());

            return mv;
        }

        // delete the token
        securityUserService.removePasswordResetToken(token);
        // return "redirect:/updatePassword?lang=" + locale.getLanguage();
        ModelAndView mv = new ModelAndView("redirect:/updatePassword?lang=" + locale.getLanguage());

        return mv;
    }

    @RequestMapping(value = "/user/savePassword", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse savePassword(final Locale locale, @Valid PasswordDto passwordDto) {
        final User userModel = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.changeUserPassword(userModel, passwordDto.getNewPassword());
        // unauth the user
        SecurityContextHolder.getContext().setAuthentication(null);
        return new GenericResponse(messageSource.getMessage("message.resetPasswordSuc", null, locale));
    }

    private String getAppUrl(HttpServletRequest request) {
        return serverUrl + request.getContextPath();
    }

    private SimpleMailMessage constructResendVerificationTokenEmail(final String contextPath, final Locale locale, final VerificationToken newToken, final User user) {
        final String confirmationUrl = contextPath + "/registrationConfirm?token=" + newToken.getToken();
        return constructEmail("New Shrinkly Registration Token", confirmationUrl, user);
    }

    private SimpleMailMessage constructResetTokenEmail(final String contextPath, final Locale locale, final String token, final User user) {
        final String url = contextPath + "/user/changePassword?id=" + user.getUserId() + "&token=" + token;
        return constructEmail("Reset your Shrinkly Password", url, user);
    }


    private SimpleMailMessage constructEmail(String subject, String body, User user) {
        final SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(user.getEmail());
        email.setFrom(env.getProperty("support.email"));
        return email;
    }

    private void authWithoutPassword(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
