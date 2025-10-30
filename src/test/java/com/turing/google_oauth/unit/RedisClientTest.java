package com.turing.google_oauth.unit;

import com.turing.google_oauth.auth.RedisClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RedisClientTest {

    @Mock RedisTemplate<String, String> redisTemplate;
    @Mock ValueOperations<String, String> valueOps;

    @InjectMocks RedisClient redisClient;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(redisClient, "timeToLive", 10);
    }

    @Test
    void testShouldCacheState() {
        String state = UUID.randomUUID().toString();
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        doNothing().when(valueOps).set(String.format("state:%s", state), state, 10L, TimeUnit.MINUTES);
        redisClient.cacheState(state);
        verify(valueOps).set(anyString(), eq(state), anyLong(), eq(TimeUnit.MINUTES));
    }

    @Test
    void testIsStateValid() {
        String state = UUID.randomUUID().toString();
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(String.format("state:%s", state))).thenReturn(state);
        redisClient.isStateValid(state);
        verify(valueOps).get(eq(String.format("state:%s", state)));
    }

    @Test
    void testDeleteState() {
        String state = UUID.randomUUID().toString();
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.getAndDelete(eq(String.format("state:%s", state)))).thenReturn(state);
        redisClient.deleteState(state);
        verify(valueOps).getAndDelete(eq(String.format("state:%s", state)));
    }

}
