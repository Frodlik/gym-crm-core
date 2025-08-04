package com.gym.crm.actuator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseHealthIndicatorTest {
    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;

    private DatabaseHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new DatabaseHealthIndicator(dataSource);
    }

    @Test
    void testHealth_whenDatabaseAvailable_returnsUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(true);

        Health actual = healthIndicator.health();

        assertThat(actual.getStatus()).isEqualTo(Status.UP);
        assertThat(actual.getDetails()).containsEntry("database", "Available");
        assertThat(actual.getDetails()).containsEntry("connectionTimeout", "5 seconds");
        assertThat(actual.getDetails()).containsKey("timestamp");
    }

    @Test
    void testHealth_whenConnectionInvalid_returnsDown() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(false);

        Health actual = healthIndicator.health();

        assertThat(actual.getStatus()).isEqualTo(Status.DOWN);
        assertThat(actual.getDetails()).containsEntry("database", "Connection invalid");
        assertThat(actual.getDetails()).containsKey("timestamp");
        assertThat(actual.getDetails()).doesNotContainKey("connectionTimeout");
    }

    @Test
    void testHealth_whenSQLExceptionWithDifferentMessage_returnsDown() throws SQLException {
        SQLException exception = new SQLException("Database unavailable");
        when(dataSource.getConnection()).thenThrow(exception);

        Health actual = healthIndicator.health();

        assertThat(actual.getStatus()).isEqualTo(Status.DOWN);
        assertThat(actual.getDetails()).containsEntry("database", "Connection failed");
        assertThat(actual.getDetails()).containsEntry("error", "Database unavailable");
        assertThat(actual.getDetails()).containsKey("timestamp");
    }

    @Test
    void testHealth_allRequiredFieldsPresent() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(true);

        Health actual = healthIndicator.health();

        assertThat(actual.getDetails()).containsKeys("database", "timestamp");
        assertThat(actual.getDetails().get("database")).isNotNull();
        assertThat(actual.getDetails().get("timestamp")).isNotNull();
    }

    @Test
    void testHealth_timestampIsAlwaysPresent() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("Test"));

        Health actual = healthIndicator.health();

        assertThat(actual.getDetails()).containsKey("timestamp");
        assertThat(actual.getDetails().get("timestamp")).isNotNull();
    }
}
