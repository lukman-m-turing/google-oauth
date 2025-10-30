package com.turing.google_oauth.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
@RequiredArgsConstructor
public class OAuth2StateService {

    private final RedisClient stateCache;
    private final ScheduledExecutorService cleaner =  Executors.newSingleThreadScheduledExecutor();

    public String generateOAuth2State() {
        String state = generateState();
        stateCache.cacheState(state);
        return state;
    }

    public boolean isStateValid(String state) {
        return stateCache.isStateValid(state);
    }

    public void destroyState(String state) {
        stateCache.deleteState(state);
    }

    private String generateState() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
