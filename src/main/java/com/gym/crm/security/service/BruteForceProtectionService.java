package com.gym.crm.security.service;

import com.gym.crm.security.LoginAttemptInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BruteForceProtectionService {
    private static final Logger logger = LoggerFactory.getLogger(BruteForceProtectionService.class);

    private final Map<String, LoginAttemptInfo> attemptCache = new ConcurrentHashMap<>();

    @Value("${security.brute-force.max-attempts:3}")
    private int maxAttempts;

    @Value("${security.brute-force.block-duration-minutes:5}")
    private int blockDurationMinutes;

    public void recordFailedAttempt(String username) {
        LoginAttemptInfo info = attemptCache.computeIfAbsent(username, k -> new LoginAttemptInfo());

        if (info.isBlocked()) {
            return;
        }

        info.incrementFailedAttempts();

        if (info.getFailedAttempts() >= maxAttempts) {
            info.blockUser(blockDurationMinutes);
            logger.warn("User {} blocked for {} minutes after {} failed attempts", username, blockDurationMinutes, info.getFailedAttempts());
        }
    }

    public void recordSuccessfulAttempt(String username) {
        attemptCache.remove(username);
        logger.debug("Cleared failed attempts for user: {}", username);
    }

    public boolean isUserBlocked(String username) {
        LoginAttemptInfo info = attemptCache.get(username);
        if (info == null) {
            return false;
        }

        return info.isBlocked();
    }

    public LocalDateTime getBlockExpiration(String username) {
        LoginAttemptInfo info = attemptCache.get(username);

        return info != null ? info.getBlockedUntil() : null;
    }

    @Scheduled(fixedRate = 600000)
    public void cleanupExpiredEntries() {
        attemptCache.entrySet().removeIf(this::isExpiredEntry);
        logger.debug("Cleaned up expired brute force entries");
    }

    private boolean isExpiredEntry(Map.Entry<String, LoginAttemptInfo> entry) {
        LoginAttemptInfo info = entry.getValue();

        return !info.isBlocked() && info.getFailedAttempts() > 0;
    }
}
