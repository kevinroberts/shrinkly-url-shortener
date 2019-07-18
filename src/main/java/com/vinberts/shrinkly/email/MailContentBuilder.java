package com.vinberts.shrinkly.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@Service
public class MailContentBuilder {

    private TemplateEngine templateEngine;

    @Autowired
    public MailContentBuilder(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String build(HashMap<String, Object> emailContext) {
        Context context = new Context();
        for (Map.Entry<String, Object> entry : emailContext.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            context.setVariable(key, value);
        }
        return templateEngine.process("mail/basicMailTemplate", context);
    }
}
