package com.turing.google_oauth.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turing.google_oauth.auth.AuthService;
import com.turing.google_oauth.auth.OAuth2StateService;
import com.turing.google_oauth.auth.model.OpenIdUser;
import com.turing.google_oauth.exception.BadRequestException;
import com.turing.google_oauth.user.User;
import com.turing.google_oauth.user.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.UUID;

import static com.turing.google_oauth.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock UserService userService;
    @Mock HttpClient httpClient;
    @Mock OAuth2StateService oAuth2StateService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    AuthService authService;
    private static final String AUTH_URL = "http://localhost:8080/authorize";
    private static final String CLIENT_ID = "test-client-id";
    private static final String CLIENT_SECRET = "test-client-secret";
    private static final String REDIRECT_URL = "http://localhost:8080/callback";
    private static final String GOOGLE_TOKEN_API = "http://localhost:8080/";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(authService, "authorizationEndpoint", AUTH_URL);
        ReflectionTestUtils.setField(authService, "clientId", CLIENT_ID);
        ReflectionTestUtils.setField(authService, "clientSecret", CLIENT_SECRET);
        ReflectionTestUtils.setField(authService, "redirectUrl", REDIRECT_URL);
        ReflectionTestUtils.setField(authService, "googleTokenEndpoint", GOOGLE_TOKEN_API);
    }

    @Test
    void testGetAuthorizationUrl() {
        assertNotNull(authService);
        URI expectedAuthorizationUrl = URI.create(AUTH_URL);

        String dummyOAuth2State = RandomStringUtils.secure().nextAlphanumeric(30);
        when(oAuth2StateService.generateOAuth2State()).thenReturn(dummyOAuth2State);

        URI actualAuthorizationUrl = authService.getCodeFlowInitializationUrl();
        UriComponents uriComponents = UriComponentsBuilder.fromUri(actualAuthorizationUrl).build();

        assertEquals(expectedAuthorizationUrl.getHost(), actualAuthorizationUrl.getHost());
        MultiValueMap<String, String> queryParams = uriComponents.getQueryParams();
        assertEquals(CLIENT_ID,  queryParams.get("client_id").get(0));
        assertEquals(REDIRECT_URL, queryParams.get("redirect_uri").get(0));
        assertEquals("code", queryParams.get("response_type").get(0));
        String expectedScope = String.format("openid+%s+%s+%s+%s+%s", SCOPE_EMAIL, SCOPE_PROFILE,
                SCOPE_MANAGE_YOUTUBE_ACCOUNT, SCOPE_VIEW_YOUTUBE_ACCOUNT, SCOPE_MANAGE_YOUTUBE_VIDEOS);
        assertEquals(expectedScope, queryParams.get("scope").get(0));
    }
    
    @Test
    void testHandleAuthorizationCodeFlowCallback() throws IOException, InterruptedException {
        HttpResponse<String> clientResponse = defaultCodeExchangeResponse();
        when(httpClient.send(any(), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(clientResponse);
        String dummyOAuth2State = RandomStringUtils.secure().nextAlphanumeric(30);
        when(oAuth2StateService.isStateValid(dummyOAuth2State)).thenReturn(true);
        assertTrue(authService.handleAuthCodeFlowCallback("", dummyOAuth2State).toURL().toString().startsWith(PROFILE_URL));
    }

    @Test
    void testHandleAuthorizationCodeFlowCallbackFailsForInvalidState() {
        String dummyOAuth2State = RandomStringUtils.secure().nextAlphanumeric(30);
        when(oAuth2StateService.isStateValid(dummyOAuth2State)).thenReturn(false);
        BadRequestException badRequestException = assertThrows(BadRequestException.class, () ->
                authService.handleAuthCodeFlowCallback("", dummyOAuth2State));
        assertEquals("Invalid or missing state parameter", badRequestException.getMessage());
    }

    @Test
    void testLoginExistingUser() {
        OpenIdUser openIdUser = new OpenIdUser();
        openIdUser.email = "tester@tesmail.com";
        openIdUser.firstName = "Kaniel";
        openIdUser.lastName = "Outis";
        openIdUser.profilePicturePath = "https://no-dummy-image/files/1234.png";
        String weakToken = UUID.randomUUID().toString();

        User existingUser = new User();
        existingUser.email = openIdUser.email;
        existingUser.firstName = openIdUser.firstName;
        existingUser.lastName = openIdUser.lastName;
        existingUser.profilePictureUrl = openIdUser.profilePicturePath;

        when(userService.findByEmail(openIdUser.email)).thenReturn(existingUser);
        when(userService.updateToken(any(), anyString())).thenReturn(existingUser);

        User actualUser = authService.loginExistingUser(openIdUser, weakToken);

        assertEquals(openIdUser.email, actualUser.email);
        assertEquals(openIdUser.firstName, actualUser.firstName);
        assertEquals(openIdUser.lastName, actualUser.lastName);
        assertEquals(weakToken, actualUser.weakToken);
        assertEquals(openIdUser.profilePicturePath, actualUser.profilePictureUrl);
    }

    @Test
    void testRegisterNewUser() {
        OpenIdUser openIdUser = new OpenIdUser();
        openIdUser.email = "tester@tesmail.com";
        openIdUser.firstName = "Kaniel";
        openIdUser.lastName = "Outis";
        openIdUser.profilePicturePath = "https://no-dummy-image/files/1234.png";
        String weakToken = UUID.randomUUID().toString();

        User newUser = new User();
        newUser.email = openIdUser.email;
        newUser.firstName = openIdUser.firstName;
        newUser.lastName = openIdUser.lastName;
        newUser.profilePictureUrl = openIdUser.profilePicturePath;

        when(userService.createUser(any())).thenReturn(newUser);

        authService.registerNewUser(openIdUser, weakToken);

        assertEquals(openIdUser.email, newUser.email);
        assertEquals(openIdUser.firstName, newUser.firstName);
        assertEquals(openIdUser.lastName, newUser.lastName);
        assertEquals(openIdUser.profilePicturePath, newUser.profilePictureUrl);
    }

    private HttpResponse<String> defaultCodeExchangeResponse() {
        return new HttpResponse<>() {

            @Override
            public int statusCode() {
                return 200;
            }

            @Override
            public String body() {
                return """
                {
                    "id_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6Ijg4NDg5MjEyMmUyOTM5ZmQxZjMxMzc1YjJiMzYzZWM4MTU3MjNiYmIiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI0Nzk2Mzc4NTE1NzUtZmw0bWRkZ2RhamJhZjNxa2ZvcnE1bmFrcTc5N29kN20uYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI0Nzk2Mzc4NTE1NzUtZmw0bWRkZ2RhamJhZjNxa2ZvcnE1bmFrcTc5N29kN20uYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDczNzM2NjczMjQzMzQ4NzYzOTciLCJoZCI6InR1cmluZy5jb20iLCJlbWFpbCI6Imx1a21hbi5tQHR1cmluZy5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXRfaGFzaCI6IlBrY0xhOFVtTGNOV0lmdEdMRWFLbkEiLCJuYW1lIjoiTHVrbWFuIE11ZGkiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EvQUNnOG9jSTN2dnQza1FfY0hRUDhES3E4S1cyZHFIWU9YMUQzMUl6eEJHLUEzMU5lRERseUpqOD1zOTYtYyIsImdpdmVuX25hbWUiOiJMdWttYW4iLCJmYW1pbHlfbmFtZSI6Ik11ZGkiLCJpYXQiOjE3NjE3MjQ5ODAsImV4cCI6MTc2MTcyODU4MH0.uKnyv75NNrw7G_XoNeK8ZRk6FixdSP41jIc2tYkFwPgTAvise4k9A4wPSg38WkN4QZ8pObpg3QFEVmBVWh5x1-shW_wGaq0eEOSm8zq_Yhwh7FZtNjiUV8BndcX8TGSVHHOS7lHyD7GICB7YR7Lg6vLLuhAcwqAaDCg_eYtDe1B1c6yicwnLJ2P8K8sCfosScP9jXRLA_j63nRvnnboMoKsdOkjfkLAdDfg_cITarl7uOJ3yhtKS03EBeEWMW8xzhymXJ3w7AObxswCD9ExYR645RbPDBm_e-ChwsO9dHUUS6LTGDpr6PNWP7DEAwq7nXjFuEoDFZCZr_DTSWY_CcQ"
                }
                """;
            }

            //Unused overrides below
            @Override
            public HttpRequest request() {return null;}
            @Override
            public Optional<HttpResponse<String>> previousResponse() {return Optional.empty();}
            @Override
            public HttpHeaders headers() {return null;}
            @Override
            public Optional<SSLSession> sslSession() {return Optional.empty();}
            @Override
            public URI uri() {return null;}
            @Override
            public HttpClient.Version version() {return null;}
        };
    }

}
