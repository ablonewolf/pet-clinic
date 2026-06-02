package org.arka99.support;

import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class PostgresIntegrationTest implements TestPropertyProvider {

    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
        .withDatabaseName("pet_clinic_test")
        .withUsername("postgres")
        .withPassword("postgres");

    @Override
    public Map<String, String> getProperties() {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
        return Map.of(
            "datasources.default.url", POSTGRES.getJdbcUrl(),
            "datasources.default.username", POSTGRES.getUsername(),
            "datasources.default.password", POSTGRES.getPassword(),
            "datasources.default.driver-class-name", POSTGRES.getDriverClassName(),
            "datasources.default.db-type", "postgres",
            "datasources.default.dialect", "POSTGRES",
            "jpa.default.properties.hibernate.hbm2ddl.auto", "create-drop",
            "micronaut.server.port", "-1"
        );
    }
}
