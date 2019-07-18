package com.vinberts.shrinkly.persistence.model;

import lombok.Data;

import java.io.Serializable;

/**
 *
 */
@Data
public class Referrers implements Serializable {

    private Long count;

    private String referrer;

    public Referrers(final Long count, final String referrer) {
        this.count = count;
        this.referrer = referrer;
    }
}
