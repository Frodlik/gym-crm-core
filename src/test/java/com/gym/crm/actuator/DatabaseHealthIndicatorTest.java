package com.gym.crm.actuator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseHealthIndicatorTest {
    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @InjectMocks
    private DatabaseHealthIndicator healthIndicator;

    @Test
    void testHealth_whenDatabaseAvailable_returnsUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(true);

        Health actual = healthIndicator.health();

        assertThat(actual.getStatus()).isEqualTo(Status.UP);
        assertThat(actual.getDetails()).hasSize(3)
                .containsEntry("database", "Available")
                .containsEntry("connectionTimeout", "5 seconds")
                .containsKey("timestamp");
        assertRecentTimestamp(actual.getDetails().get("timestamp"));
    }

    @Test
    void testHealth_whenConnectionInvalid_returnsDown() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(false);

        Health actual = healthIndicator.health();

        assertThat(actual.getStatus()).isEqualTo(Status.DOWN);
        assertThat(actual.getDetails()).hasSize(2)
                .containsEntry("database", "Connection invalid")
                .containsKey("timestamp")
                .doesNotContainKey("connectionTimeout");
        assertRecentTimestamp(actual.getDetails().get("timestamp"));
    }

    @Test
    void testHealth_whenSQLException_returnsDownWithValidAttributes() throws SQLException {
        SQLException exception = new SQLException("Database connection timeout");
        when(dataSource.getConnection()).thenThrow(exception);

        Health actual = healthIndicator.health();

        assertThat(actual.getStatus()).isEqualTo(Status.DOWN);
        assertThat(actual.getDetails()).hasSize(3)
                .containsEntry("database", "Connection failed")
                .containsEntry("error", "Database connection timeout")
                .containsKey("timestamp");
        assertRecentTimestamp(actual.getDetails().get("timestamp"));
    }

    @Test
    void testHealth_connectionTimeoutValueIsCorrect() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(true);

        Health actual = healthIndicator.health();
        String timeout = (String) actual.getDetails().get("connectionTimeout");

        assertThat(timeout).isEqualTo("5 seconds")
                .contains("5")
                .contains("seconds");
    }

    private void assertRecentTimestamp(Object value) {
        assertThat(value).isInstanceOf(Instant.class);
        assertThat((Instant) value).isCloseTo(Instant.now(), within(2, ChronoUnit.SECONDS));
    }
}
