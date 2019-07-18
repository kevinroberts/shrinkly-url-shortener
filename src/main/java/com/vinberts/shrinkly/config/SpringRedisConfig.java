package com.vinberts.shrinkly.config;

import com.google.common.primitives.Ints;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
@Slf4j
@Configuration
public class SpringRedisConfig {

    @Bean
    public LettuceConnectionFactory connectionFactory() {

        String activeProfile = System.getProperty("spring.profiles.active", "prod");
        String propertiesFilename = "application-" + activeProfile + ".properties";
        Properties prop = new Properties();

        try {
            prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFilename));
        } catch (IOException e) {
            log.error("could not load prop file", e);

        }
        log.info("loaded properties file " + propertiesFilename);

        String redisHost = prop.getProperty("spring.redis.host", "localhost");
        if (redisHost.startsWith("$")) {
            redisHost = resolveEnvVars(redisHost);
        }
        String redisPort = prop.getProperty("spring.redis.port", "6379");
        if (redisPort.startsWith("$")) {
            redisPort = resolveEnvVars(redisPort);
        }
        String redisPassword = prop.getProperty("spring.redis.password", StringUtils.EMPTY);
        if (redisPort.startsWith("$")) {
            redisPassword = resolveEnvVars(redisPassword);
        }

        log.info("Loaded Redis on hostname " + redisHost);

        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(redisHost);
        redisStandaloneConfiguration.setPort(Ints.tryParse(redisPort, 10));
        if (!redisPassword.equals(StringUtils.EMPTY)) {
            redisStandaloneConfiguration.setPassword(RedisPassword.of(prop.getProperty("spring.redis.password", StringUtils.EMPTY)));
        }

        redisStandaloneConfiguration.setDatabase(Ints.tryParse(prop.getProperty("spring.redis.database", "0"), 10));


        LettuceClientConfiguration.LettuceClientConfigurationBuilder lettuceClientConfiguration = LettuceClientConfiguration.builder();

        //lettuceClientConfiguration.useSsl();

        lettuceClientConfiguration.commandTimeout(Duration.ofSeconds(60));

        if (activeProfile.equals("prod")) {
            lettuceClientConfiguration.shutdownTimeout(Duration.ofSeconds(30));
        }

        LettuceConnectionFactory lettuceConFactory = new LettuceConnectionFactory(redisStandaloneConfiguration,
                lettuceClientConfiguration.build());

        return lettuceConFactory;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }


    private String resolveEnvVars(String input) {
        if (null == input) {
            return null;
        }
        // match ${ENV_VAR_NAME} or $ENV_VAR_NAME
        Pattern p = Pattern.compile("\\$\\{(\\w+)\\}|\\$(\\w+)");
        Matcher m = p.matcher(input); // get a matcher object
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String envVarName = null == m.group(1) ? m.group(2) : m.group(1);
            String envVarValue = System.getenv(envVarName);
            m.appendReplacement(sb, null == envVarValue ? "" : envVarValue);
        }
        m.appendTail(sb);
        return sb.toString();
    }

}
