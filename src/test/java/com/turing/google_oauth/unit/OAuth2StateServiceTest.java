package com.turing.google_oauth.unit;

import com.turing.google_oauth.auth.OAuth2StateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class OAuth2StateServiceTest {

    OAuth2StateService stateService;

    @BeforeEach
    void setup() {
        Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        stateService = new OAuth2StateService(clock);
    }

    @Test
    void testGenerateOAuth2State() {
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
        String state = stateService.generateOAuth2State();
        assertTrue(stateService.isStateValid(state), "State was not cached");
    }

}
