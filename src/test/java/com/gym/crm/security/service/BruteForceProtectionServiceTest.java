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
    private BruteForceProtectionService bruteForceProtectionService;
    private static final String TEST_USERNAME = "gojo.satoru";
    private static final String ANOTHER_USERNAME = "zero.two";

    @BeforeEach
    void setUp() {
        bruteForceProtectionService = new BruteForceProtectionService();
        ReflectionTestUtils.setField(bruteForceProtectionService, "maxAttempts", 3);
        ReflectionTestUtils.setField(bruteForceProtectionService, "blockDurationMinutes", 5);
    }

    @Test
    void testRecordFailedAttempt_whenFirstFailure_shouldNotBlockUser() {
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);

        assertFalse(bruteForceProtectionService.isUserBlocked(TEST_USERNAME));
        assertNull(bruteForceProtectionService.getBlockExpiration(TEST_USERNAME));
    }

    @Test
    void testRecordFailedAttempt_whenSecondFailure_shouldNotBlockUser() {
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);

        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);

        assertFalse(bruteForceProtectionService.isUserBlocked(TEST_USERNAME));
        assertNull(bruteForceProtectionService.getBlockExpiration(TEST_USERNAME));
    }

    @Test
    void testRecordFailedAttempt_whenThirdFailure_shouldBlockUser() {
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);

        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);

        assertTrue(bruteForceProtectionService.isUserBlocked(TEST_USERNAME));
        LocalDateTime blockExpiration = bruteForceProtectionService.getBlockExpiration(TEST_USERNAME);
        assertNotNull(blockExpiration);
        assertTrue(blockExpiration.isAfter(LocalDateTime.now()));
        assertTrue(blockExpiration.isBefore(LocalDateTime.now().plusMinutes(6)));
    }

    @Test
    void testRecordFailedAttempt_whenUserAlreadyBlocked_shouldNotIncrementAttempts() {
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);

        assertTrue(bruteForceProtectionService.isUserBlocked(TEST_USERNAME));
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);
        assertTrue(bruteForceProtectionService.isUserBlocked(TEST_USERNAME));
    }

    @Test
    void testRecordSuccessfulAttempt_whenUserHasFailedAttempts_shouldClearAttempts() {
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);

        assertFalse(bruteForceProtectionService.isUserBlocked(TEST_USERNAME));
        bruteForceProtectionService.recordSuccessfulAttempt(TEST_USERNAME);
        assertFalse(bruteForceProtectionService.isUserBlocked(TEST_USERNAME));
        assertNull(bruteForceProtectionService.getBlockExpiration(TEST_USERNAME));
    }

    @Test
    void testRecordSuccessfulAttempt_whenUserIsBlocked_shouldClearBlock() {
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);

        assertTrue(bruteForceProtectionService.isUserBlocked(TEST_USERNAME));
        bruteForceProtectionService.recordSuccessfulAttempt(TEST_USERNAME);
        assertFalse(bruteForceProtectionService.isUserBlocked(TEST_USERNAME));
        assertNull(bruteForceProtectionService.getBlockExpiration(TEST_USERNAME));
    }

    @Test
    void testRecordSuccessfulAttempt_whenUserHasNoAttempts_shouldNotThrowException() {
        assertDoesNotThrow(() -> bruteForceProtectionService.recordSuccessfulAttempt(TEST_USERNAME));

        assertFalse(bruteForceProtectionService.isUserBlocked(TEST_USERNAME));
        assertNull(bruteForceProtectionService.getBlockExpiration(TEST_USERNAME));
    }

    @Test
    void testIsUserBlocked_whenUserNotInCache_shouldReturnFalse() {
        boolean isBlocked = bruteForceProtectionService.isUserBlocked(TEST_USERNAME);

        assertFalse(isBlocked);
    }

    @Test
    void testIsUserBlocked_whenUserHasFailedAttemptsButNotBlocked_shouldReturnFalse() {
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);

        boolean isBlocked = bruteForceProtectionService.isUserBlocked(TEST_USERNAME);

        assertFalse(isBlocked);
    }

    @Test
    void testGetBlockExpiration_whenUserNotBlocked_shouldReturnNull() {
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);

        LocalDateTime expiration = bruteForceProtectionService.getBlockExpiration(TEST_USERNAME);

        assertNull(expiration);
    }

    @Test
    void testMultipleUsers_shouldTrackSeparately() {
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);
        bruteForceProtectionService.recordFailedAttempt(ANOTHER_USERNAME);

        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);

        assertTrue(bruteForceProtectionService.isUserBlocked(TEST_USERNAME));
        assertFalse(bruteForceProtectionService.isUserBlocked(ANOTHER_USERNAME));
    }

    @Test
    void testCleanupExpiredEntries_shouldRemoveExpiredNonBlockedEntries() {
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);

        assertFalse(bruteForceProtectionService.isUserBlocked(TEST_USERNAME));
        bruteForceProtectionService.cleanupExpiredEntries();
        assertFalse(bruteForceProtectionService.isUserBlocked(TEST_USERNAME));
        assertNull(bruteForceProtectionService.getBlockExpiration(TEST_USERNAME));
    }

    @Test
    void testCleanupExpiredEntries_shouldKeepBlockedUsers() {
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);

        assertTrue(bruteForceProtectionService.isUserBlocked(TEST_USERNAME));
        bruteForceProtectionService.cleanupExpiredEntries();
        assertTrue(bruteForceProtectionService.isUserBlocked(TEST_USERNAME));
        assertNotNull(bruteForceProtectionService.getBlockExpiration(TEST_USERNAME));
    }

    @Test
    void testIntegration_completeScenario_shouldWorkCorrectly() {
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);
        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);

        assertFalse(bruteForceProtectionService.isUserBlocked(TEST_USERNAME));

        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);

        assertTrue(bruteForceProtectionService.isUserBlocked(TEST_USERNAME));
        LocalDateTime blockExpiration = bruteForceProtectionService.getBlockExpiration(TEST_USERNAME);
        assertNotNull(blockExpiration);

        bruteForceProtectionService.recordFailedAttempt(TEST_USERNAME);

        assertTrue(bruteForceProtectionService.isUserBlocked(TEST_USERNAME));
        assertEquals(blockExpiration, bruteForceProtectionService.getBlockExpiration(TEST_USERNAME));

        bruteForceProtectionService.recordSuccessfulAttempt(TEST_USERNAME);

        assertFalse(bruteForceProtectionService.isUserBlocked(TEST_USERNAME));
        assertNull(bruteForceProtectionService.getBlockExpiration(TEST_USERNAME));
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
