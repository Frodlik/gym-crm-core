package com.gym.crm.actuator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@ExtendWith(MockitoExtension.class)
class DiskSpaceHealthIndicatorTest {
    private DiskSpaceHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new DiskSpaceHealthIndicator();
    }

    @Test
    void testHealth_returnsDiskSpaceInformation() {
        Health actual = healthIndicator.health();

        assertThat(actual.getStatus()).isIn(Status.UP, Status.DOWN);
        assertThat(actual.getDetails()).containsKeys(
                "status", "freeSpace", "totalSpace", "usedSpace",
                "usagePercentage", "threshold", "timestamp"
        );
        assertThat(actual.getDetails()).hasSize(7);
    }

    @Test
    void testHealth_diskSpaceValuesArePositive() {
        Health actual = healthIndicator.health();

        String freeSpace = (String) actual.getDetails().get("freeSpace");
        String totalSpace = (String) actual.getDetails().get("totalSpace");
        String usedSpace = (String) actual.getDetails().get("usedSpace");

        assertThat(freeSpace).doesNotContain("-");
        assertThat(totalSpace).doesNotContain("-");
        assertThat(usedSpace).doesNotContain("-");

        assertThat(freeSpace).containsAnyOf("B", "KB", "MB", "GB");
        assertThat(totalSpace).containsAnyOf("B", "KB", "MB", "GB");
        assertThat(usedSpace).containsAnyOf("B", "KB", "MB", "GB");

        assertThat(extractNumericValue(freeSpace)).isGreaterThanOrEqualTo(0.0);
        assertThat(extractNumericValue(totalSpace)).isGreaterThan(0.0);
        assertThat(extractNumericValue(usedSpace)).isGreaterThanOrEqualTo(0.0);
    }

    @Test
    void testHealth_thresholdIsCorrectValue() {
        Health actual = healthIndicator.health();

        String threshold = (String) actual.getDetails().get("threshold");
        double thresholdMB = extractNumericValue(threshold);

        assertThat(threshold).contains("MB").contains("100");
        assertThat(thresholdMB).isCloseTo(100.0, within(0.1));
    }

    @Test
    void testHealth_usagePercentageIsValid() {
        Health actual = healthIndicator.health();

        String usagePercentage = (String) actual.getDetails().get("usagePercentage");
        double usagePercent = extractPercentageValue(usagePercentage);

        assertThat(usagePercentage).endsWith("%");
        assertThat(usagePercent).isBetween(0.0, 100.0);
    }

    @Test
    void testHealth_diskSpaceLogicIsConsistent() {
        Health actual = healthIndicator.health();

        long freeBytes = convertToBytes((String) actual.getDetails().get("freeSpace"));
        long totalBytes = convertToBytes((String) actual.getDetails().get("totalSpace"));
        long usedBytes = convertToBytes((String) actual.getDetails().get("usedSpace"));

        assertThat(totalBytes).isGreaterThan(0);
        assertThat(freeBytes).isGreaterThanOrEqualTo(0);
        assertThat(usedBytes).isGreaterThanOrEqualTo(0);}

    @Test
    void testHealth_timestampIsRecent() {
        Health actual = healthIndicator.health();

        assertThat(actual.getDetails().get("timestamp")).isInstanceOf(Instant.class);
        assertThat((Instant) actual.getDetails().get("timestamp")).isCloseTo(Instant.now(), within(2, ChronoUnit.SECONDS));
    }

    @Test
    void testHealth_statusConsistentWithFreeSpace() {
        Health actual = healthIndicator.health();

        long freeBytes = convertToBytes((String) actual.getDetails().get("freeSpace"));
        String status = (String) actual.getDetails().get("status");
        long thresholdBytes = 104857600L;

        if (freeBytes < thresholdBytes) {
            assertThat(actual.getStatus()).isEqualTo(Status.DOWN);
            assertThat(status).isEqualTo("Low disk space");
        } else {
            assertThat(actual.getStatus()).isEqualTo(Status.UP);
            assertThat(status).isEqualTo("Sufficient disk space");
        }
    }

    @Test
    void testHealth_withCustomThreshold_detectsLowDiskSpace() {
        ReflectionTestUtils.setField(healthIndicator, "thresholdBytes", Long.MAX_VALUE);

        Health actual = healthIndicator.health();

        assertThat(actual.getStatus()).isEqualTo(Status.DOWN);
        assertThat(actual.getDetails()).containsEntry("status", "Low disk space");
    }

    private double extractNumericValue(String formattedValue) {
        String numericPart = formattedValue.replaceAll("[^0-9,.]", "").replace(",", ".");

        return Double.parseDouble(numericPart);
    }

    private double extractPercentageValue(String percentageValue) {
        String numericPart = percentageValue.replace("%", "").replace(",", ".");

        return Double.parseDouble(numericPart);
    }

    private long convertToBytes(String formattedValue) {
        double numericValue = extractNumericValue(formattedValue);
        String unit = formattedValue.replaceAll("[^A-Za-z]", "");

        return switch (unit.toUpperCase()) {
            case "KB" -> (long) (numericValue * 1024);
            case "MB" -> (long) (numericValue * 1024 * 1024);
            case "GB" -> (long) (numericValue * 1024 * 1024 * 1024);
            default -> (long) numericValue;
        };
    }
}