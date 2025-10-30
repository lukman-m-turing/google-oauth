package com.turing.google_oauth.integration;

import com.turing.google_oauth.integration.helpers.ObjectMother;
import com.turing.google_oauth.integration.helpers.WireMockInitializer;
import com.turing.google_oauth.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("integration-test")
@ContextConfiguration(initializers = WireMockInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    @Autowired protected WebTestClient webTestClient;
    @Autowired protected UserRepository userRepository;
    @Autowired protected ObjectMother mom;

    private static final String REDIS_SERVER_PASSWORD = "5TR0NGP@55W0RD";

    static GenericContainer redisServer = new GenericContainer(DockerImageName.parse("redis:8.2.2"))
            .withExposedPorts(6379)
            .withCommand("redis-server", "--requirepass", REDIS_SERVER_PASSWORD);

    static {
        redisServer.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        //Redis config
        registry.add("spring.data.redis.url", redisServer::getHost);
        registry.add("spring.data.redis.port", redisServer::getFirstMappedPort);
        registry.add("spring.data.redis.password", () ->  REDIS_SERVER_PASSWORD);
    }

}
