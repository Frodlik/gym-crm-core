package com.gym.crm.actuator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

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
        assertThat(actual.getDetails().get("threshold")).isEqualTo("100,00 MB");
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
}