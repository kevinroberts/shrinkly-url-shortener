package com.vinberts.shrinkly.service;

import com.blueconic.browscap.Capabilities;
import com.blueconic.browscap.ParseException;
import com.blueconic.browscap.UserAgentParser;
import com.blueconic.browscap.UserAgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 *
 */

@Slf4j
@Service
public class UserAgentParserService {

    private UserAgentParser parser;

    @PostConstruct
    public void init() throws IOException, ParseException {
        log.info("init user agent parsing service");
        parser = new UserAgentService().loadParser();
    }

    public Capabilities getCapabilities(String userAgent) {
        final Capabilities capabilities = parser.parse(userAgent);
        return capabilities;
    }
}
