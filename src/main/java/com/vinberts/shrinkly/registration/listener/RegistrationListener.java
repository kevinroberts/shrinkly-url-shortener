package com.vinberts.shrinkly.registration.listener;

import com.google.common.collect.Maps;
import com.vinberts.shrinkly.email.ShrinklyMailClient;
import com.vinberts.shrinkly.email.mailPojos.MailLink;
import com.vinberts.shrinkly.persistence.model.User;
import com.vinberts.shrinkly.registration.OnRegistrationCompleteEvent;
import com.vinberts.shrinkly.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;

/**
 *
 */
@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    @Autowired
    private IUserService service;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ShrinklyMailClient mailClient;

    @Autowired
    private Environment env;

    @Override
    public void onApplicationEvent(final OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    void confirmRegistration(final OnRegistrationCompleteEvent event) {
        final User user = event.getUser();
        final String token = new BigInteger(200, new SecureRandom()).toString(36);
        service.createVerificationTokenForUser(user, token);

        final SimpleMailMessage email = constructEmailMessage(event, user, token);

        // construct email context
        HashMap<String, Object> emailContext = Maps.newHashMap();

        emailContext.put("userGreeting", "Hi " + user.getUsername() + ", ");
        emailContext.put("mailBody1", "To complete your sign up, please verify your email:");
        emailContext.put("plainText", "Activate your Shrinkly.net account: " + email.getText());
        emailContext.put("mailBody3", "Or copy this link in your web browser<br><a href=\"" +
                email.getText() + "\">" + email.getText() + "</a><br>Thanks,<br>The Shrinkly Team" );
        emailContext.put("ctaLink", new MailLink(email.getText(), "Verify Email"));

        mailClient.prepareAndSend(email.getTo()[0], email.getSubject(), emailContext);
    }


    private SimpleMailMessage constructEmailMessage(final OnRegistrationCompleteEvent event, final User user, final String token) {
        final String recipientAddress = user.getEmail();
        final String subject = "Registration Confirmation";
        final String confirmationUrl = event.getAppUrl() + "/registrationConfirm?token=" + token;
        // final String message = messageSource.getMessage("message.regSucc", null, event.getLocale());
        final SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText(confirmationUrl);
        email.setFrom(env.getProperty("support.email"));
        return email;
    }
}
