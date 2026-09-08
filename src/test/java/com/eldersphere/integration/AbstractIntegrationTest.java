package com.eldersphere.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Shared base for full-context ({@code @SpringBootTest}) integration tests: boots the app
 * against a real, throwaway Postgres container (Testcontainers) rather than H2, since the
 * app relies on Postgres-specific behavior (native queries, Flyway targeting
 * flyway-database-postgresql) that an in-memory substitute wouldn't faithfully exercise.
 *
 * <p>The container is a JVM-wide singleton (a single static field shared by every subclass,
 * started once by the {@code @Testcontainers} extension) so the whole integration suite pays
 * the Postgres startup cost only once instead of once per test class. Flyway runs against it
 * automatically on every context start (spring.flyway.enabled=true, baseline-on-migrate), so
 * every subclass also implicitly exercises "does the full migration set apply cleanly against
 * a brand-new database" - see {@link FlywayMigrationIntegrationTest} for an explicit assertion
 * of that.
 */
@Testcontainers
@ExtendWith(org.springframework.test.context.junit.jupiter.SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("eldersphere_test")
                    .withUsername("eldersphere_test")
                    .withPassword("eldersphere_test");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        // Any valid base64-encoded 32+ byte HMAC key - only used within this test JVM.
        registry.add("jwt.secret", () -> "dGVzdC1zZWNyZXQta2V5LWZvci1lbGRlcnNwaGVyZS1pbnRlZ3JhdGlvbi10ZXN0cyE=");
        registry.add("app.frontend.url", () -> "http://localhost:5173,http://localhost:3000");
    }

    @LocalServerPort
    protected int port;

    @org.springframework.beans.factory.annotation.Autowired
    protected TestRestTemplate restTemplate;

    @org.springframework.beans.factory.annotation.Autowired
    protected ObjectMapper objectMapper;

    protected String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    /**
     * {@code ResponseModel<T>} (the app's response envelope) has no no-arg/creator
     * constructor, so Jackson can't deserialize it directly on the test-client side (it
     * serializes fine going out - that only needs getters). Rather than adding a
     * deserialization-only constructor to production code purely for test convenience,
     * responses are read as raw JSON here and the "data" field is pulled out and converted
     * to whatever DTO type the test actually needs.
     */
    protected <T> T extractData(ResponseEntity<String> response, Class<T> dataType) {
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.get("data");
            if (data == null || data.isNull()) return null;
            return objectMapper.treeToValue(data, dataType);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to extract 'data' from response body: " + response.getBody(), e);
        }
    }
}
