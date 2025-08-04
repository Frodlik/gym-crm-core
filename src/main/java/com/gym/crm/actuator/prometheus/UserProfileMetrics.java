package com.gym.crm.actuator.prometheus;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class UserProfileMetrics {
    private static final String DESCRIPTION = "description";

    private final Counter profileUpdates;
    private final Counter profileCreations;

    public UserProfileMetrics(MeterRegistry meterRegistry) {
        this.profileUpdates = meterRegistry.counter("gym.profile.updates.count", DESCRIPTION, "Number of profile updates");

        this.profileCreations = meterRegistry.counter("gym.profile.creations.count", DESCRIPTION, "Number of profile creations");
    }

    public void recordProfileUpdate() {
        profileUpdates.increment();
    }

    public void recordProfileCreation() {
        profileCreations.increment();
    }
}
