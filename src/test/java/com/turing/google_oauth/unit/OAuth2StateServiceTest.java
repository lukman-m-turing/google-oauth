package com.turing.google_oauth.unit;

import com.turing.google_oauth.auth.OAuth2StateService;
import com.turing.google_oauth.auth.RedisClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OAuth2StateServiceTest {

    @Mock RedisClient redisClient;

    @InjectMocks
    OAuth2StateService stateService;

    @Test
    void testGenerateOAuth2State() {
        doNothing().when(redisClient).cacheState(anyString());
        String state = stateService.generateOAuth2State();

        // 2. Should be URL-safe: only contains [A-Za-z0-9-_]
        assertTrue(state.matches("^[A-Za-z0-9_-]+$"),
                "State contains non-URL-safe characters: " + state);

        // 3. Should have enough length for 128 bits (Base64URL: 16 bytes = 22 chars)
        assertTrue(state.length() >= 22, "State too short: " + state.length());

        // 4. Should decode back to 16 bytes
        byte[] decoded = Base64.getUrlDecoder().decode(state);
        assertEquals(16, decoded.length, "Decoded length should be 16 bytes (128 bits)");
    }

    @Test
    void testGenerateOAuth2StateUpdatesCache() {
        doNothing().when(redisClient).cacheState(anyString());
        when(redisClient.isStateValid(anyString())).thenReturn(true);
        String state = stateService.generateOAuth2State();
        assertTrue(stateService.isStateValid(state), "State was not cached");
    }

    @Test
    void testDestroyState() {
        String state = UUID.randomUUID().toString();
        doNothing().when(redisClient).deleteState(state);
        stateService.destroyState(state);
        verify(redisClient).deleteState(state);
    }

}
