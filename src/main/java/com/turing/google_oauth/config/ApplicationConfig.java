package com.turing.google_oauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.ZoneId;

import static com.turing.google_oauth.util.Constants.NIGERIA_TIME_ZONE_ID;

@Configuration
public class ApplicationConfig {

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of(NIGERIA_TIME_ZONE_ID));
    }

}
