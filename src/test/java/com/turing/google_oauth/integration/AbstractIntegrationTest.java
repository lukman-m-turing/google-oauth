package com.turing.google_oauth.integration;

import com.turing.google_oauth.integration.helpers.ObjectMother;
import com.turing.google_oauth.integration.helpers.WireMockInitializer;
import com.turing.google_oauth.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("integration-test")
@ContextConfiguration(initializers = WireMockInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    @Autowired protected WebTestClient webTestClient;
    @Autowired protected UserRepository userRepository;
    @Autowired protected ObjectMother mom;

}
