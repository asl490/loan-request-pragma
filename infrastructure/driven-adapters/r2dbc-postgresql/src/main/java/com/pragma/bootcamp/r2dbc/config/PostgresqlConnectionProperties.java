package com.pragma.bootcamp.r2dbc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;

@Profile("postgres")
@ConfigurationProperties(prefix = "adapters.r2dbc")
public record PostgresqlConnectionProperties(
                String host,
                Integer port,
                String database,
                String schema,
                String username,
                String password) {
}
