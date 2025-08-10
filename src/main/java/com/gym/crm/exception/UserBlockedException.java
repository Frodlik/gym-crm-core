package com.gym.crm.exception;

import java.time.LocalDateTime;

public class UserBlockedException extends RuntimeException {
    private final String username;
    private final LocalDateTime blockedUntil;

    public UserBlockedException(String username, LocalDateTime blockedUntil) {
        super(String.format("User %s is blocked until %s", username, blockedUntil));
        this.username = username;
        this.blockedUntil = blockedUntil;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getBlockedUntil() {
        return blockedUntil;
    }
}
