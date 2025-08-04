package com.gym.crm.actuator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

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
                "status", "maxMemory", "totalMemory", "usedMemory",
                "freeMemory", "memoryUsage", "threshold", "timestamp"
        );
        assertThat(actual.getDetails()).hasSize(8);
    }

    @Test
    void testHealth_containsValidThreshold() {
        Health actual = healthIndicator.health();

        assertThat(actual.getDetails().get("threshold")).isEqualTo("85%");
    }

    @Test
    void testHealth_memoryValuesArePositive() {
        Health actual = healthIndicator.health();

        String maxMemory = (String) actual.getDetails().get("maxMemory");
        String totalMemory = (String) actual.getDetails().get("totalMemory");
        String usedMemory = (String) actual.getDetails().get("usedMemory");
        String freeMemory = (String) actual.getDetails().get("freeMemory");

        assertThat(maxMemory).doesNotContain("-");
        assertThat(totalMemory).doesNotContain("-");
        assertThat(usedMemory).doesNotContain("-");
        assertThat(freeMemory).doesNotContain("-");

        assertThat(maxMemory).containsAnyOf("KB", "MB", "GB");
        assertThat(totalMemory).containsAnyOf("KB", "MB", "GB");
        assertThat(usedMemory).containsAnyOf("KB", "MB", "GB");
        assertThat(freeMemory).containsAnyOf("KB", "MB", "GB");

        assertThat(extractNumericValue(maxMemory)).isGreaterThan(0.0);
        assertThat(extractNumericValue(totalMemory)).isGreaterThan(0.0);
        assertThat(extractNumericValue(usedMemory)).isGreaterThanOrEqualTo(0.0);
        assertThat(extractNumericValue(freeMemory)).isGreaterThanOrEqualTo(0.0);
    }

    @Test
    void testHealth_memoryUsageIsValidPercentage() {
        Health actual = healthIndicator.health();

        String memoryUsage = (String) actual.getDetails().get("memoryUsage");
        double usagePercent = extractPercentageValue(memoryUsage);

        assertThat(memoryUsage).endsWith("%");
        assertThat(usagePercent).isBetween(0.0, 100.0);
    }

    @Test
    void testHealth_statusConsistentWithUsage() {
        Health actual = healthIndicator.health();

        String memoryUsage = (String) actual.getDetails().get("memoryUsage");
        double usagePercent = extractPercentageValue(memoryUsage);
        String status = (String) actual.getDetails().get("status");

        if (usagePercent > 85.0) {
            assertThat(actual.getStatus()).isEqualTo(Status.DOWN);
            assertThat(status).isEqualTo("High memory usage");
        } else {
            assertThat(actual.getStatus()).isEqualTo(Status.UP);
            assertThat(status).isEqualTo("Memory usage normal");
        }
    }

    @Test
    void testHealth_timestampIsRecent() {
        Health actual = healthIndicator.health();

        assertThat(actual.getDetails().get("timestamp")).isInstanceOf(Instant.class);
        assertThat((Instant) actual.getDetails().get("timestamp")).isCloseTo(Instant.now(), within(2, ChronoUnit.SECONDS));
    }

    @Test
    void testHealth_thresholdValueIsCorrect() {
        Health actual = healthIndicator.health();

        String threshold = (String) actual.getDetails().get("threshold");
        double thresholdValue = extractPercentageValue(threshold);

        assertThat(threshold).isEqualTo("85%");
        assertThat(thresholdValue).isEqualTo(85.0);
    }

    @Test
    void testHealth_memoryLogicIsConsistent() {
        Health actual = healthIndicator.health();

        double maxMemory = extractNumericValue((String) actual.getDetails().get("maxMemory"));
        double totalMemory = extractNumericValue((String) actual.getDetails().get("totalMemory"));
        double usedMemory = extractNumericValue((String) actual.getDetails().get("usedMemory"));
        double freeMemory = extractNumericValue((String) actual.getDetails().get("freeMemory"));

        assertThat(totalMemory)
                .isGreaterThanOrEqualTo(usedMemory)
                .isGreaterThanOrEqualTo(freeMemory);
        assertThat(maxMemory).isLessThanOrEqualTo(totalMemory);
    }

    private double extractNumericValue(String formattedValue) {
        String numericPart = formattedValue.replaceAll("[^0-9,.]", "").replace(",", ".");

        return Double.parseDouble(numericPart);
    }

    private double extractPercentageValue(String percentageValue) {
        String numericPart = percentageValue.replace("%", "").replace(",", ".");

        return Double.parseDouble(numericPart);
    }
}
