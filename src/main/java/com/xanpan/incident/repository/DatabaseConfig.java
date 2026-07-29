package com.xanpan.incident.repository;

import java.util.Map;

public record DatabaseConfig(String url, String user, String password) {

    public static DatabaseConfig fromEnvironment() {
        return fromEnvironment(System.getenv());
    }

    static DatabaseConfig fromEnvironment(Map<String, String> environment) {
        String host = environment.getOrDefault("POSTGRES_HOST", "localhost");
        String port = environment.getOrDefault("POSTGRES_PORT", "5432");
        String database = environment.getOrDefault("POSTGRES_DB", "xanpan");
        String defaultUrl = "jdbc:postgresql://%s:%s/%s".formatted(host, port, database);

        return new DatabaseConfig(
                environment.getOrDefault("DATABASE_URL", defaultUrl),
                environment.getOrDefault("POSTGRES_USER", "xanpan"),
                environment.getOrDefault("POSTGRES_PASSWORD", "xanpan_dev")
        );
    }
}
