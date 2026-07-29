package com.xanpan.incident.repository;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseConfigTest {

    @Test
    void usesDockerComposeDefaults() {
        DatabaseConfig config = DatabaseConfig.fromEnvironment(Map.of());

        assertEquals("jdbc:postgresql://localhost:5432/xanpan", config.url());
        assertEquals("xanpan", config.user());
        assertEquals("xanpan_dev", config.password());
    }

    @Test
    void acceptsEnvironmentOverrides() {
        DatabaseConfig config = DatabaseConfig.fromEnvironment(Map.of(
                "DATABASE_URL", "jdbc:postgresql://db:5433/helpdesk",
                "POSTGRES_USER", "app",
                "POSTGRES_PASSWORD", "secret"
        ));

        assertEquals("jdbc:postgresql://db:5433/helpdesk", config.url());
        assertEquals("app", config.user());
        assertEquals("secret", config.password());
    }
}
