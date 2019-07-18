package com.vinberts.shrinkly.service;

import com.maxmind.geoip2.record.Country;

import java.util.Optional;

/**
 *
 */
public interface IServerLocationService {

    Optional<Country> getLocation(String ipAddress);

}
