package com.turing.google_oauth.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OAuth2StateService {

    private final Map<String, Long> stateCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner =  Executors.newSingleThreadScheduledExecutor();
    private final Clock clock;
    int timeToLiveMillis = 600000; //TTL=10mins
    int cleanupIntervalMillis = 600_000; //Cleanup=10 mins

    {
        cleaner.scheduleAtFixedRate(this::cleanupCache, cleanupIntervalMillis, cleanupIntervalMillis, TimeUnit.MILLISECONDS);
    }

    public String generateOAuth2State() {
        String state = generateState();
        long expiryTime = clock.millis() + timeToLiveMillis;
        stateCache.put(state, expiryTime);
        return state;
    }

    public boolean isStateValid(String state) {
        Long expiryTime = stateCache.get(state);
        if (expiryTime == null) return false;
        if (expiryTime < clock.millis()) {
            stateCache.remove(state);
            return false;
        }
        return true;
    }

    private String generateState() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void cleanupCache() {
        long now = clock.millis();
        stateCache.entrySet().removeIf(entry -> entry.getValue() < now);
    }

}
