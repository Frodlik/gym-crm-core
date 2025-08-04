package com.gym.crm.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Instant;

@Component
@ConfigurationProperties(prefix = "gca.metrics")
public class DiskSpaceHealthIndicator implements HealthIndicator {
    private static final String STATUS = "status";
    private static final String TIMESTAMP = "timestamp";

    private final Long thresholdBytes  = 104857600L;

    @Override
    public Health health() {
        try {
            File rootDir = new File(".");
            long free = rootDir.getFreeSpace();
            long total = rootDir.getTotalSpace();
            long used = total - free;

            double usage = calculateUsagePercentage(used, total);

            return buildHealthStatus(free, total, used, usage);
        } catch (Exception e) {
            return Health.down()
                    .withDetail(STATUS, "Unable to check disk space")
                    .withDetail("error", e.getMessage())
                    .withDetail(TIMESTAMP, Instant.now())
                    .build();
        }
    }

    private double calculateUsagePercentage(long used, long total) {
        return (double) used / total * 100;
    }

    private Health buildHealthStatus(long free, long total, long used, double usage) {
        Health.Builder builder = free < thresholdBytes
                ? Health.down().withDetail(STATUS, "Low disk space")
                : Health.up().withDetail(STATUS, "Sufficient disk space");

        return builder
                .withDetail("freeSpace", formatBytes(free))
                .withDetail("totalSpace", formatBytes(total))
                .withDetail("usedSpace", formatBytes(used))
                .withDetail("usagePercentage", String.format("%.2f%%", usage))
                .withDetail("threshold", formatBytes(thresholdBytes))
                .withDetail(TIMESTAMP, Instant.now())
                .build();
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }

        if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        }

        if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }

        return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}
