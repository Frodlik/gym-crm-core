package com.gym.crm.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class MemoryHealthIndicator implements HealthIndicator {
    private static final double MEMORY_THRESHOLD = 0.85;

    @Override
    public Health health() {
        Runtime runtime = Runtime.getRuntime();

        long max = runtime.maxMemory();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long used = total - free;

        double usageFraction = (double) used / max;

        return buildHealthReport(used, free, total, max, usageFraction);
    }

    private Health buildHealthReport(long used, long free, long total, long max, double usage) {
        Health.Builder builder = usage > MEMORY_THRESHOLD
                ? Health.down().withDetail("status", "High memory usage")
                : Health.up().withDetail("status", "Memory usage normal");

        return builder
                .withDetail("maxMemory", formatBytes(max))
                .withDetail("totalMemory", formatBytes(total))
                .withDetail("usedMemory", formatBytes(used))
                .withDetail("freeMemory", formatBytes(free))
                .withDetail("memoryUsage", String.format("%.2f%%", usage * 100))
                .withDetail("threshold", String.format("%.0f%%", MEMORY_THRESHOLD * 100))
                .withDetail("timestamp", Instant.now())
                .build();
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        }

        if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }

        return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}
