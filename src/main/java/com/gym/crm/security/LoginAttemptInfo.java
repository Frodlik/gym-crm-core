package com.gym.crm.security;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LoginAttemptInfo {
    private int failedAttempts;
    private LocalDateTime blockedUntil;

    public void incrementFailedAttempts() {
        this.failedAttempts++;
    }

    public void blockUser(int blockDurationMinutes) {
        this.blockedUntil = LocalDateTime.now().plusMinutes(blockDurationMinutes);
    }

    public boolean isBlocked() {
        return blockedUntil != null && LocalDateTime.now().isBefore(blockedUntil);
    }
}
