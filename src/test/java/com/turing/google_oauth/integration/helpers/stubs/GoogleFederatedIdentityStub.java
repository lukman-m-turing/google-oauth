package com.turing.google_oauth.integration.helpers.stubs;

import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

@RequiredArgsConstructor
public class GoogleFederatedIdentityStub {

    private final WireMockServer googleServer;

    public void stubForExchangeAuthorizationCodeForToken(String expectedBody) {
        googleServer.stubFor(post("/google/token").withHeader(HttpHeaders.CONTENT_TYPE, containing(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(expectedBody)
        ));
    }

    public void stubForExchangeAuthorizationCodeForTokenWithDelayedResponse(String expectedBody) {
        String scenario = "Sorry! Google Service is Currently Down";
        googleServer.stubFor(post("/google/token")
                .inScenario(scenario)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .willSetStateTo("First Attempt"));

        googleServer.stubFor(post("/google/token").withHeader(HttpHeaders.CONTENT_TYPE, containing(MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                        .inScenario("First Attempt")
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(expectedBody)));
    }

    public int getServerPort() {
        return this.googleServer.port();
    }
}
