package com.gym.crm.actuator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

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
    }

    @Test
    void testHealth_thresholdIsCorrect() {
        Health actual = healthIndicator.health();

        String threshold = (String) actual.getDetails().get("threshold");

        assertThat(threshold)
                .contains("MB")
                .contains("100");
    }

    @Test
    void testHealth_spaceValuesContainUnits() {
        Health actual = healthIndicator.health();

        String freeSpace = (String) actual.getDetails().get("freeSpace");
        String totalSpace = (String) actual.getDetails().get("totalSpace");
        String usedSpace = (String) actual.getDetails().get("usedSpace");

        assertThat(freeSpace).containsAnyOf("B", "KB", "MB", "GB");
        assertThat(totalSpace).containsAnyOf("B", "KB", "MB", "GB");
        assertThat(usedSpace).containsAnyOf("B", "KB", "MB", "GB");
    }

    @Test
    void testHealth_usagePercentageIsValid() {
        Health actual = healthIndicator.health();

        String usagePercentage = (String) actual.getDetails().get("usagePercentage");

        assertThat(usagePercentage)
                .contains("%")
                .isNotEmpty();
    }

    @Test
    void testHealth_timestampIsPresent() {
        Health actual = healthIndicator.health();

        assertThat(actual.getDetails().get("timestamp")).isNotNull();
    }

    @Test
    void testHealth_withCustomThreshold_detectsLowDiskSpace() {
        ReflectionTestUtils.setField(healthIndicator, "thresholdBytes", Long.MAX_VALUE);

        Health actual = healthIndicator.health();

        assertThat(actual.getStatus()).isEqualTo(Status.DOWN);
        assertThat(actual.getDetails()).containsEntry("status", "Low disk space");
    }
}