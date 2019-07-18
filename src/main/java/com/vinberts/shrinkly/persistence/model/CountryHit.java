package com.vinberts.shrinkly.persistence.model;

import lombok.Data;

import java.io.Serializable;

/**
 *
 */
@Data
public class CountryHit implements Serializable {

    private Long count;

    private String country;

    public CountryHit(final Long count, final String country) {
        this.count = count;
        this.country = country;
    }
}
