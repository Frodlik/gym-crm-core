package com.gym.crm.security;

import com.gym.crm.security.service.CustomUserDetailsService;
import com.gym.crm.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthenticationContext {
    private final CustomUserDetailsService userDetailsService;
    private final TokenExtractor tokenExtractor;

    public Optional<String> getCurrentUsername() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName);
    }

    public Optional<String> getCurrentUserType() {
        return getCurrentUsername()
                .flatMap(this::getUserTypeByUsername);
    }

    public boolean isCurrentUserTrainee() {
        return getCurrentUserType()
                .map("TRAINEE"::equals)
                .orElse(false);
    }

    public boolean isCurrentUserTrainer() {
        return getCurrentUserType()
                .map("TRAINER"::equals)
                .orElse(false);
    }

    public Optional<String> extractTokenFromRequest() {
        return tokenExtractor.extractAccessTokenFromCurrentRequest();
    }

    private Optional<String> getUserTypeByUsername(String username) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);
            return Optional.of(userDetails.getRole());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
