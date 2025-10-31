package com.turing.google_oauth.integration;

import com.turing.google_oauth.auth.OAuth2StateService;
import com.turing.google_oauth.integration.helpers.stubs.GoogleFederatedIdentityStub;
import com.turing.google_oauth.user.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;

import static com.turing.google_oauth.util.Constants.*;
import static org.awaitility.Awaitility.given;
import static org.junit.jupiter.api.Assertions.*;

public class AuthControllerIT extends AbstractIntegrationTest {

    @Autowired GoogleFederatedIdentityStub googleFederatedIdentityStub;
    @Autowired OAuth2StateService stateService;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void testRegisterNewUser() {
        //Make sure no user exists at start
        assertEquals(0, userRepository.count());
        //Initialize authorization code flow
        String authCodeFlowInitializationUrl = webTestClient.get()
                .uri("/auth/initialize-flow")
                .exchange()
                .expectStatus()
                .isSeeOther()
                .returnResult(String.class)
                .getResponseHeaders().getFirst(HttpHeaders.LOCATION);
        assertNotNull(authCodeFlowInitializationUrl);

        //Handle callback from Google Federated Identity
        String bodyFromGoogleServer = mom.getOIDCIDToken();
        googleFederatedIdentityStub.stubForExchangeAuthorizationCodeForToken(bodyFromGoogleServer);

        String authorizationCode = RandomStringUtils.secure().nextAlphanumeric(20);
        String username = "lukman.m@turing.com";
        String state = UriComponentsBuilder.fromUri(URI.create(authCodeFlowInitializationUrl))
                .build()
                .getQueryParams()
                .getFirst("state");
        String profileUrl = webTestClient.get()
                .uri("/auth/callback?code=" + authorizationCode + "&state=" + state)
                .exchange()
                .expectStatus().isFound()
                .returnResult(String.class)
                .getResponseHeaders()
                .getFirst(HttpHeaders.LOCATION);

        assertTrue(profileUrl.startsWith(PROFILE_URL));
        assertEquals(1, userRepository.count());
        assertTrue(userRepository.existsByEmail(username));
    }

    @Test
    void testLoginExistingUser() {
        User existingUser = mom.getUserFor("lukman.m@turing.com");
        userRepository.saveAndFlush(existingUser);

        //Verify user exists on database
        assertEquals(1, userRepository.count());
        assertTrue(userRepository.existsByEmail(existingUser.email));

        //Initialize authorization code flow
        String authCodeFlowInitializationUrl = webTestClient.get()
                .uri("/auth/initialize-flow")
                .exchange()
                .expectStatus()
                .isSeeOther()
                .returnResult(String.class)
                .getResponseHeaders().getFirst(HttpHeaders.LOCATION);
        assertNotNull(authCodeFlowInitializationUrl);

        //Handle callback from Google Federate Identity
        String bodyFromGoogleServer = mom.getOIDCIDToken();
        googleFederatedIdentityStub.stubForExchangeAuthorizationCodeForToken(bodyFromGoogleServer);

        String authorizationCode = RandomStringUtils.secure().nextAlphanumeric(20);
        String state = UriComponentsBuilder.fromUri(URI.create(authCodeFlowInitializationUrl))
                .build()
                .getQueryParams()
                .getFirst("state");
        String profileUrl = webTestClient.get()
                .uri("/auth/callback?code=" + authorizationCode + "&state=" + state)
                .exchange()
                .expectStatus().isFound()
                .returnResult(String.class)
                .getResponseHeaders()
                .getFirst(HttpHeaders.LOCATION);

        assertTrue(profileUrl.startsWith(PROFILE_URL));
        assertEquals(1, userRepository.count());
        assertTrue(userRepository.existsByEmail(existingUser.email));
    }

    @Test
    void testLoginExistingUserWithReadinessCheckOnGoogleServer() {
        User existingUser = mom.getUserFor("lukman.m@turing.com");
        userRepository.saveAndFlush(existingUser);

        //Initialize authorization code flow
        String authCodeFlowInitializationUrl = webTestClient.get()
                .uri("/auth/initialize-flow")
                .exchange()
                .expectStatus()
                .isSeeOther()
                .returnResult(String.class)
                .getResponseHeaders().getFirst(HttpHeaders.LOCATION);
        assertNotNull(authCodeFlowInitializationUrl);

        //Handle callback from Google Federate Identity
        String bodyFromGoogleServer = mom.getOIDCIDToken();
        googleFederatedIdentityStub.stubForExchangeAuthorizationCodeForTokenWithDelayedResponse(bodyFromGoogleServer);

        String state = UriComponentsBuilder.fromUri(URI.create(authCodeFlowInitializationUrl))
                .build()
                .getQueryParams()
                .getFirst("state");

        //Using awaitility, make two attempts on same API. First one should fail, second should succeed and get result
        given()
                .atMost(Duration.ofSeconds(3))
                .await()
                .untilAsserted(() -> {
                    String authorizationCode = RandomStringUtils.secure().nextAlphanumeric(20);
                    String profileUrl = webTestClient.get()
                            .uri("/auth/callback?code=" + authorizationCode + "&state=" + state)
                            .exchange()
                            .expectStatus().isFound()
                            .returnResult(String.class)
                            .getResponseHeaders()
                            .getFirst(HttpHeaders.LOCATION);

                    assertTrue(profileUrl.startsWith(PROFILE_URL));
                    assertEquals(1, userRepository.count());
                    assertTrue(userRepository.existsByEmail(existingUser.email));
                });
    }

    @Test
    void testThrowsBadRequestExceptionForInvalidOrExpiredState() {
        String state = RandomStringUtils.secure().nextAlphanumeric(20);
        webTestClient.get()
                .uri("/auth/callback?code=12345&state=" + state)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid or missing state parameter");
    }

    @Test
    void testOAuthStateParamIsDestroyedAfterCallback() {
        //Initialize authorization code flow
        String authCodeFlowInitializationUrl = webTestClient.get()
                .uri("/auth/initialize-flow")
                .exchange()
                .expectStatus()
                .isSeeOther()
                .returnResult(String.class)
                .getResponseHeaders().getFirst(HttpHeaders.LOCATION);
        assertNotNull(authCodeFlowInitializationUrl);

        //Handle callback from Google Federated Identity
        String bodyFromGoogleServer = mom.getOIDCIDToken();
        googleFederatedIdentityStub.stubForExchangeAuthorizationCodeForToken(bodyFromGoogleServer);

        String authorizationCode = RandomStringUtils.secure().nextAlphanumeric(20);
        String state = UriComponentsBuilder.fromUri(URI.create(authCodeFlowInitializationUrl))
                .build()
                .getQueryParams()
                .getFirst("state");

        webTestClient.get()
                .uri("/auth/callback?code=" + authorizationCode + "&state=" + state)
                .exchange()
                .expectStatus().isFound();
        //Verify state param is destroyed after callback
        assertFalse(stateService.isStateValid(state), "state param should be destroyed after callback but wasn't");
    }

    @Test
    void testOAuth2FlowIncludesYoutubeScopesAndOAuth2AccessTokenIsIssued() {
        //Initialize authorization code flow
        String authCodeFlowInitializationUrl = webTestClient.get()
                .uri("/auth/initialize-flow")
                .exchange()
                .expectStatus()
                .isSeeOther()
                .returnResult(String.class)
                .getResponseHeaders().getFirst(HttpHeaders.LOCATION);
        assertNotNull(authCodeFlowInitializationUrl);
        
        //verify youtube scopes are present
        assertTrue(authCodeFlowInitializationUrl.contains(SCOPE_MANAGE_YOUTUBE_ACCOUNT));
        assertTrue(authCodeFlowInitializationUrl.contains(SCOPE_VIEW_YOUTUBE_ACCOUNT));
        assertTrue(authCodeFlowInitializationUrl.contains(SCOPE_MANAGE_YOUTUBE_VIDEOS));

        //Handle callback from Google Federated Identity
        String bodyFromGoogleServer = mom.getOAuthAccessToken();
        googleFederatedIdentityStub.stubForExchangeAuthorizationCodeForToken(bodyFromGoogleServer);

        String authorizationCode = RandomStringUtils.secure().nextAlphanumeric(20);
        String username = "lukman.m@turing.com";
        String state = UriComponentsBuilder.fromUri(URI.create(authCodeFlowInitializationUrl))
                .build()
                .getQueryParams()
                .getFirst("state");
        webTestClient.get()
                .uri("/auth/callback?code=" + authorizationCode + "&state=" + state)
                .exchange()
                .expectStatus().isFound();

        //Verify oauth 2 access token is retrieved from Google federated identity and stored
        User user = userRepository.findByEmail(username);
        assertNotNull(user.oauthAccessToken);
    }

    @Test
    void testOAuth2FlowIncludesOAuth2RefreshToken() {
        //Initialize authorization code flow
        String authCodeFlowInitializationUrl = webTestClient.get()
                .uri("/auth/initialize-flow")
                .exchange()
                .expectStatus()
                .isSeeOther()
                .returnResult(String.class)
                .getResponseHeaders().getFirst(HttpHeaders.LOCATION);
        assertNotNull(authCodeFlowInitializationUrl);

        String refresh_token_request = UriComponentsBuilder.fromUri(URI.create(authCodeFlowInitializationUrl))
                .build()
                .getQueryParams()
                .getFirst("access_type");

        //Verify authorization code flow initialization request contains refresh token request 'access_type=offline'
        assertEquals("offline", refresh_token_request);

        //Handle callback from Google Federated Identity
        String bodyFromGoogleServer = mom.getOAuthAccessAndRefreshTokens();
        googleFederatedIdentityStub.stubForExchangeAuthorizationCodeForToken(bodyFromGoogleServer);

        String authorizationCode = RandomStringUtils.secure().nextAlphanumeric(20);
        String username = "lukman.m@turing.com";
        String state = UriComponentsBuilder.fromUri(URI.create(authCodeFlowInitializationUrl))
                .build()
                .getQueryParams()
                .getFirst("state");
        webTestClient.get()
                .uri("/auth/callback?code=" + authorizationCode + "&state=" + state)
                .exchange()
                .expectStatus().isFound();

        //Verify oauth 2 refresh token is retrieved from Google federated identity and stored
        User user = userRepository.findByEmail(username);
        assertNotNull(user.oauthRefreshToken);
    }

    @Test
    void testOAuth2FlowDoesNotOverrideRefreshTokenIfNoNewOneIsIssued() {
        User existingUser = mom.getUserFor("lukman.m@turing.com");
        String storedRefreshToken = "dummy-refresh-token";
        existingUser.oauthRefreshToken = storedRefreshToken;
        userRepository.saveAndFlush(existingUser);

        //Initialize authorization code flow
        String authCodeFlowInitializationUrl = webTestClient.get()
                .uri("/auth/initialize-flow")
                .exchange()
                .expectStatus()
                .isSeeOther()
                .returnResult(String.class)
                .getResponseHeaders().getFirst(HttpHeaders.LOCATION);
        assertNotNull(authCodeFlowInitializationUrl);

        String refresh_token_request = UriComponentsBuilder.fromUri(URI.create(authCodeFlowInitializationUrl))
                .build()
                .getQueryParams()
                .getFirst("access_type");

        //Verify authorization code flow initialization request contains refresh token request 'access_type=offline'
        assertEquals("offline", refresh_token_request);

        //Handle callback from Google Federated Identity
        String bodyFromGoogleServer = mom.getOAuthAccessToken(); //Does not include refresh token
        googleFederatedIdentityStub.stubForExchangeAuthorizationCodeForToken(bodyFromGoogleServer);

        String authorizationCode = RandomStringUtils.secure().nextAlphanumeric(20);
        String username = "lukman.m@turing.com";
        String state = UriComponentsBuilder.fromUri(URI.create(authCodeFlowInitializationUrl))
                .build()
                .getQueryParams()
                .getFirst("state");
        webTestClient.get()
                .uri("/auth/callback?code=" + authorizationCode + "&state=" + state)
                .exchange()
                .expectStatus().isFound();

        //Verify stored oauth 2.0 refresh token is not overwritten
        User user = userRepository.findByEmail(username);
        assertEquals(storedRefreshToken, user.oauthRefreshToken);
    }

}
