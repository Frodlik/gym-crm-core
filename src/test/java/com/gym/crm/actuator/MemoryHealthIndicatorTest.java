package com.gym.crm.actuator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MemoryHealthIndicatorTest {
    private MemoryHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new MemoryHealthIndicator();
    }

    @Test
    void testHealth_returnsMemoryInformation() {
        Health actual = healthIndicator.health();

        assertThat(actual.getStatus()).isIn(Status.UP, Status.DOWN);
        assertThat(actual.getDetails()).containsKeys(
                "maxMemory", "totalMemory", "usedMemory",
                "freeMemory", "memoryUsage", "threshold"
        );
    }

    @Test
    void testHealth_containsValidThreshold() {
        Health actual = healthIndicator.health();

        assertThat(actual.getDetails().get("threshold")).isEqualTo("85%");
    }

    @Test
    void testHealth_memoryValuesAreNotEmpty() {
        Health actual = healthIndicator.health();

        assertThat(actual.getDetails().get("maxMemory")).isNotNull();
        assertThat(actual.getDetails().get("totalMemory")).isNotNull();
        assertThat(actual.getDetails().get("usedMemory")).isNotNull();
        assertThat(actual.getDetails().get("freeMemory")).isNotNull();
    }
}
