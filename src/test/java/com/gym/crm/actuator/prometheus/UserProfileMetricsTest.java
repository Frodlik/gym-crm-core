package com.gym.crm.actuator.prometheus;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileMetricsTest {
    @Mock
    private MeterRegistry meterRegistry;
    @Mock
    private Counter profileUpdates;
    @Mock
    private Counter profileCreations;

    private UserProfileMetrics userProfileMetrics;

    @BeforeEach
    void setUp() {
        when(meterRegistry.counter("gym.profile.updates.count",
                "description", "Number of profile updates"))
                .thenReturn(profileUpdates);
        when(meterRegistry.counter("gym.profile.creations.count",
                "description", "Number of profile creations"))
                .thenReturn(profileCreations);

        userProfileMetrics = new UserProfileMetrics(meterRegistry);
    }

    @Test
    void recordProfileUpdate_shouldIncrementUpdateCounter() {
        userProfileMetrics.recordProfileUpdate();

        verify(profileUpdates).increment();
    }

    @Test
    void recordProfileCreation_shouldIncrementCreationCounter() {
        userProfileMetrics.recordProfileCreation();

        verify(profileCreations).increment();
    }
}
