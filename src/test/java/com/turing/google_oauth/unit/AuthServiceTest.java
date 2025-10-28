package com.turing.google_oauth.unit;

import com.turing.google_oauth.auth.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URI;

import static com.turing.google_oauth.util.Constants.PROFILE_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    private final AuthService authService = new AuthService();
    private static final String AUTH_URL = "http://localhost:8080/authorize";
    private static final String CLIENT_ID = "test-client-id";
    private static final String REDIRECT_URL = "http://localhost:8080/callback";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "authorizationEndpoint", AUTH_URL);
        ReflectionTestUtils.setField(authService, "clientId", CLIENT_ID);
        ReflectionTestUtils.setField(authService, "redirectUrl", REDIRECT_URL);
    }

    @Test
    void testGetAuthorizationUrl() {
        assertNotNull(authService);
        URI expectedAuthorizationUrl = URI.create(AUTH_URL);
        URI actualAuthorizationUrl = authService.getCodeFlowInitializationUrl();
        UriComponents uriComponents = UriComponentsBuilder.fromUri(actualAuthorizationUrl).build();

        assertEquals(expectedAuthorizationUrl.getHost(), actualAuthorizationUrl.getHost());
        MultiValueMap<String, String> queryParams = uriComponents.getQueryParams();
        assertEquals(CLIENT_ID,  queryParams.get("client_id").get(0));
        assertEquals(REDIRECT_URL, queryParams.get("redirect_uri").get(0));
        assertEquals("code", queryParams.get("response_type").get(0));
        String expectedScope =
                "openid+https://www.googleapis.com/auth/userinfo.email+" +
                        "https://www.googleapis.com/auth/userinfo.profile";
        assertEquals(expectedScope, queryParams.get("scope").get(0));
    }
    
    @Test
    void testHandleAuthorizationCodeFlowCallback() throws MalformedURLException {
        assertEquals(PROFILE_URL, authService.handleAuthCodeFlowCallback("").toURL().toString());
    }

}
