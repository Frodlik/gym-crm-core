package com.gym.crm.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BruteForceProtectionServiceTest {
    private static final String TEST_USERNAME = "gojo.satoru";
    private static final String ANOTHER_USERNAME = "zero.two";

    private BruteForceProtectionService service;

    @BeforeEach
    void setUp() {
        service = new BruteForceProtectionService();
        ReflectionTestUtils.setField(service, "maxAttempts", 3);
        ReflectionTestUtils.setField(service, "blockDurationMinutes", 5);
    }

    @Test
    void testRecordFailedAttempt_whenFirstFailure_shouldNotBlockUser() {
        service.recordFailedAttempt(TEST_USERNAME);

        assertFalse(service.isUserBlocked(TEST_USERNAME));
        assertNull(service.getBlockExpiration(TEST_USERNAME));
    }

    @Test
    void testRecordFailedAttempt_whenSecondFailure_shouldNotBlockUser() {
        service.recordFailedAttempt(TEST_USERNAME);

        service.recordFailedAttempt(TEST_USERNAME);

        assertFalse(service.isUserBlocked(TEST_USERNAME));
        assertNull(service.getBlockExpiration(TEST_USERNAME));
    }

    @Test
    void testRecordFailedAttempt_whenThirdFailure_shouldBlockUser() {
        service.recordFailedAttempt(TEST_USERNAME);
        service.recordFailedAttempt(TEST_USERNAME);

        service.recordFailedAttempt(TEST_USERNAME);

        assertTrue(service.isUserBlocked(TEST_USERNAME));
        LocalDateTime blockExpiration = service.getBlockExpiration(TEST_USERNAME);
        assertNotNull(blockExpiration);
        assertTrue(blockExpiration.isAfter(LocalDateTime.now()));
        assertTrue(blockExpiration.isBefore(LocalDateTime.now().plusMinutes(6)));
    }

    @Test
    void testRecordFailedAttempt_whenUserAlreadyBlocked_shouldNotIncrementAttempts() {
        service.recordFailedAttempt(TEST_USERNAME);
        service.recordFailedAttempt(TEST_USERNAME);
        service.recordFailedAttempt(TEST_USERNAME);

        assertTrue(service.isUserBlocked(TEST_USERNAME));
        service.recordFailedAttempt(TEST_USERNAME);
        assertTrue(service.isUserBlocked(TEST_USERNAME));
    }

    @Test
    void testRecordSuccessfulAttempt_whenUserHasFailedAttempts_shouldClearAttempts() {
        service.recordFailedAttempt(TEST_USERNAME);
        service.recordFailedAttempt(TEST_USERNAME);

        assertFalse(service.isUserBlocked(TEST_USERNAME));
        service.recordSuccessfulAttempt(TEST_USERNAME);
        assertFalse(service.isUserBlocked(TEST_USERNAME));
        assertNull(service.getBlockExpiration(TEST_USERNAME));
    }

    @Test
    void testRecordSuccessfulAttempt_whenUserIsBlocked_shouldClearBlock() {
        service.recordFailedAttempt(TEST_USERNAME);
        service.recordFailedAttempt(TEST_USERNAME);
        service.recordFailedAttempt(TEST_USERNAME);

        assertTrue(service.isUserBlocked(TEST_USERNAME));
        service.recordSuccessfulAttempt(TEST_USERNAME);
        assertFalse(service.isUserBlocked(TEST_USERNAME));
        assertNull(service.getBlockExpiration(TEST_USERNAME));
    }

    @Test
    void testRecordSuccessfulAttempt_whenUserHasNoAttempts_shouldNotThrowException() {
        assertDoesNotThrow(() -> service.recordSuccessfulAttempt(TEST_USERNAME));

        assertFalse(service.isUserBlocked(TEST_USERNAME));
        assertNull(service.getBlockExpiration(TEST_USERNAME));
    }

    @Test
    void testIsUserBlocked_whenUserNotInCache_shouldReturnFalse() {
        boolean isBlocked = service.isUserBlocked(TEST_USERNAME);

        assertFalse(isBlocked);
    }

    @Test
    void testIsUserBlocked_whenUserHasFailedAttemptsButNotBlocked_shouldReturnFalse() {
        service.recordFailedAttempt(TEST_USERNAME);
        service.recordFailedAttempt(TEST_USERNAME);

        boolean isBlocked = service.isUserBlocked(TEST_USERNAME);

        assertFalse(isBlocked);
    }

    @Test
    void testGetBlockExpiration_whenUserNotBlocked_shouldReturnNull() {
        service.recordFailedAttempt(TEST_USERNAME);

        LocalDateTime expiration = service.getBlockExpiration(TEST_USERNAME);

        assertNull(expiration);
    }

    @Test
    void testMultipleUsers_shouldTrackSeparately() {
        service.recordFailedAttempt(TEST_USERNAME);
        service.recordFailedAttempt(TEST_USERNAME);
        service.recordFailedAttempt(ANOTHER_USERNAME);

        service.recordFailedAttempt(TEST_USERNAME);

        assertTrue(service.isUserBlocked(TEST_USERNAME));
        assertFalse(service.isUserBlocked(ANOTHER_USERNAME));
    }

    @Test
    void testCleanupExpiredEntries_shouldRemoveExpiredNonBlockedEntries() {
        service.recordFailedAttempt(TEST_USERNAME);
        service.recordFailedAttempt(TEST_USERNAME);

        assertFalse(service.isUserBlocked(TEST_USERNAME));
        service.cleanupExpiredEntries();
        assertFalse(service.isUserBlocked(TEST_USERNAME));
        assertNull(service.getBlockExpiration(TEST_USERNAME));
    }

    @Test
    void testCleanupExpiredEntries_shouldKeepBlockedUsers() {
        service.recordFailedAttempt(TEST_USERNAME);
        service.recordFailedAttempt(TEST_USERNAME);
        service.recordFailedAttempt(TEST_USERNAME);

        assertTrue(service.isUserBlocked(TEST_USERNAME));
        service.cleanupExpiredEntries();
        assertTrue(service.isUserBlocked(TEST_USERNAME));
        assertNotNull(service.getBlockExpiration(TEST_USERNAME));
    }

    @Test
    void testIntegration_completeScenario_shouldWorkCorrectly() {
        service.recordFailedAttempt(TEST_USERNAME);
        service.recordFailedAttempt(TEST_USERNAME);

        assertFalse(service.isUserBlocked(TEST_USERNAME));

        service.recordFailedAttempt(TEST_USERNAME);

        assertTrue(service.isUserBlocked(TEST_USERNAME));
        LocalDateTime blockExpiration = service.getBlockExpiration(TEST_USERNAME);
        assertNotNull(blockExpiration);

        service.recordFailedAttempt(TEST_USERNAME);

        assertTrue(service.isUserBlocked(TEST_USERNAME));
        assertEquals(blockExpiration, service.getBlockExpiration(TEST_USERNAME));

        service.recordSuccessfulAttempt(TEST_USERNAME);

        assertFalse(service.isUserBlocked(TEST_USERNAME));
        assertNull(service.getBlockExpiration(TEST_USERNAME));
    }

    @Test
    void testCustomConfiguration_shouldRespectMaxAttemptsAndDuration() {
        BruteForceProtectionService customService = new BruteForceProtectionService();
        ReflectionTestUtils.setField(customService, "maxAttempts", 2);
        ReflectionTestUtils.setField(customService, "blockDurationMinutes", 10);

        customService.recordFailedAttempt(TEST_USERNAME);
        customService.recordFailedAttempt(TEST_USERNAME);

        assertTrue(customService.isUserBlocked(TEST_USERNAME));
        LocalDateTime blockExpiration = customService.getBlockExpiration(TEST_USERNAME);
        assertNotNull(blockExpiration);
        assertTrue(blockExpiration.isAfter(LocalDateTime.now().plusMinutes(9)));
        assertTrue(blockExpiration.isBefore(LocalDateTime.now().plusMinutes(11)));
    }
}
