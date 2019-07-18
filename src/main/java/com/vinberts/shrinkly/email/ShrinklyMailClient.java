package com.vinberts.shrinkly.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 *
 */
@Service
@Slf4j
public class ShrinklyMailClient {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MailContentBuilder mailContentBuilder;

    @Autowired
    private Environment env;

    @Async
    public void prepareAndSend(String recipient, String subject, HashMap<String, Object> mailVars) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            messageHelper.setFrom(env.getProperty("support.email", "noreply@shrinkly.net"));
            messageHelper.setTo(recipient);
            messageHelper.setSubject(subject);
            String mailHtml = mailContentBuilder.build(mailVars);
            if (mailVars.get("plainText") != null) {
                messageHelper.setText(mailVars.get("plainText").toString(), mailHtml);
            } else {
                messageHelper.setText(mailHtml, true);
            }
        };
        try {
            mailSender.send(messagePreparator);
        } catch (MailException e) {
            // runtime exception; compiler will not force you to handle it
            log.error("Mail exception occurred trying to send message for subject " + subject + " to " + recipient, e);
        }
    }
}
