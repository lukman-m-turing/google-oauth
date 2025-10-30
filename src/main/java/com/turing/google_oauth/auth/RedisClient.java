package com.turing.google_oauth.auth;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisClient {

    @Value("${spring.data.redis.cache.ttl}")
    private Integer timeToLive;

    private final RedisTemplate<String, String> redisTemplate;

    public void cacheState(String state) {
        String key = String.format("state:%s", state);
        put(key, state);
    }

    public boolean isStateValid(String state) {
        String key = String.format("state:%s", state);
        String result = get(key);
        return StringUtils.isNotBlank(result);
    }

    public void deleteState(String state) {
        String key = String.format("state:%s", state);
        delete(key);
    }

    private void put(String key, String value) {
        redisTemplate.opsForValue().set(key, value, timeToLive, TimeUnit.MINUTES);
    }

    private String get(String value) {
        return redisTemplate.opsForValue().get(value);
    }

    private void delete(String key) {
        redisTemplate.opsForValue().getAndDelete(key);
    }

}
