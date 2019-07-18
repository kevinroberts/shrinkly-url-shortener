package com.vinberts.shrinkly.service.impl;

import com.blueconic.browscap.ParseException;
import com.maxmind.db.Reader;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Country;
import com.vinberts.shrinkly.service.IServerLocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Optional;

/**
 *
 */
@Slf4j
@Service
public class ServerLocationServiceImpl implements IServerLocationService {

    private DatabaseReader reader;

    @Autowired
    private ResourceLoader resourceLoader;

    @PostConstruct
    public void init() throws IOException, ParseException {
        log.info("init maxmind location services");
        Resource resource = resourceLoader.getResource("classpath:maxmind/GeoLite2-Country.mmdb");
        InputStream dbAsStream = resource.getInputStream();

        reader = new DatabaseReader
                .Builder(dbAsStream)
                .fileMode(Reader.FileMode.MEMORY)
                .build();
    }

    @Override
    public Optional<Country> getLocation(final String ipAddressString) {
        try {
            InetAddress ipAddress = InetAddress.getByName(ipAddressString);
            CountryResponse response = reader.country(ipAddress);

            return Optional.of(response.getCountry());

        } catch (IOException e) {
            log.info("IoException reached ", e);
        } catch (GeoIp2Exception e) {
            log.info("GeoIp Exception reached ", e);
        }
        return Optional.empty();
    }
}
