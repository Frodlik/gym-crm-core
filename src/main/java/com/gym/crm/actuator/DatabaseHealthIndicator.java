package com.gym.crm.actuator;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {
    private static final String DATABASE = "database";
    private static final String TIMESTAMP = "timestamp";

    private final DataSource dataSource;

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(5)) {
                return Health.down()
                        .withDetail(DATABASE, "Connection invalid")
                        .withDetail(TIMESTAMP, Instant.now())
                        .build();
            }

            return Health.up()
                    .withDetail(DATABASE, "Available")
                    .withDetail("connectionTimeout", "5 seconds")
                    .withDetail(TIMESTAMP, Instant.now())
                    .build();
        } catch (SQLException e) {
            return Health.down()
                    .withDetail(DATABASE, "Connection failed")
                    .withDetail("error", e.getMessage())
                    .withDetail(TIMESTAMP, Instant.now())
                    .build();
        }
    }
}
