package com.turing.google_oauth.integration.helpers;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.turing.google_oauth.integration.helpers.stubs.GoogleFederatedIdentityStub;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Order(Ordered.LOWEST_PRECEDENCE - 1000)
public class WireMockInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private final WireMockServer mockRemoteServer = new WireMockServer(new WireMockConfiguration().dynamicPort());

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        mockRemoteServer.start();

        GoogleFederatedIdentityStub googleFederatedIdentityStub = new GoogleFederatedIdentityStub(mockRemoteServer);

        applicationContext.getBeanFactory().registerSingleton("googleFederatedIdentityStub", googleFederatedIdentityStub);

        TestPropertyValues.of(
                "google.api.endpoint=" + getServerUrl("google"),
                "google.authorization.endpoint=" + getServerUrl("auth-google"),
                "google.oauth.client.id=" + ObjectMother.getClientId(),
                "google.oauth.client.secret=" + ObjectMother.getClientSecret()
                )
                .applyTo(applicationContext);

        applicationContext.addApplicationListener(applicationEvent -> {
            if (applicationEvent instanceof ContextClosedEvent) {
                mockRemoteServer.stop();
            }
        });
    }

    private String getServerUrl(String serviceName) {
        //url format: http://localhost:port/service-name i.e. http://localhost:3663/paystack
        return String.format("http://localhost:%d/%s", mockRemoteServer.port(), serviceName);
    }

}
