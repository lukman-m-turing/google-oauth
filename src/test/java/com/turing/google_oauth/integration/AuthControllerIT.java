package com.turing.google_oauth.integration;

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

import static com.turing.google_oauth.util.Constants.PROFILE_URL;
import static org.awaitility.Awaitility.given;
import static org.junit.jupiter.api.Assertions.*;

public class AuthControllerIT extends AbstractIntegrationTest {

    @Autowired GoogleFederatedIdentityStub googleFederatedIdentityStub;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void testRegisterNewUser() {
        //Make sure no user exists at start
        assertEquals(0, userRepository.count());
        //Initialize authorization code flow
        //This step can be skipped as no real call to Google is made, and mock server always returns a premeditated result
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
        //This step can be skipped as no real call to Google is made and mock server always returns a premeditated result
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

        //Verify user exists on database
        assertEquals(1, userRepository.count());
        assertTrue(userRepository.existsByEmail(existingUser.email));

        //Initialize authorization code flow
        //This step can be skipped as no real call to Google is made, and mock server always returns a premeditated result
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

}
