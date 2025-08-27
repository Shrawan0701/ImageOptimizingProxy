
package com.example.backend.config;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

@Component
public class DataInitializer {

    private final DatabaseClient client;

    public DataInitializer(DatabaseClient client) {
        this.client = client;
    }

    @PostConstruct
    public void init() {
        String ddl = """
        CREATE TABLE IF NOT EXISTS image_request_log (
            id BIGSERIAL PRIMARY KEY,
            created_at TIMESTAMPTZ DEFAULT NOW(),
            url TEXT,
            source TEXT,
            params TEXT,
            cache_key TEXT,
            status INT,
            content_type TEXT,
            bytes INT,
            duration_ms BIGINT
        );
        """;
        client.sql(ddl).fetch().rowsUpdated().onErrorResume(e -> Mono.empty()).subscribe();
    }
}
