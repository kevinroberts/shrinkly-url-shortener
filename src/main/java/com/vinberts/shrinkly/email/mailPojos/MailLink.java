package com.vinberts.shrinkly.email.mailPojos;

import lombok.Data;

/**
 *
 */
@Data
public class MailLink {

    public MailLink(final String href, final String label) {
        this.href = href;
        this.label = label;
    }

    String href;

    String label;
}
