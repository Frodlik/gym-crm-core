package com.gym.crm.util;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserCredentialsGenerator {
    private static final Logger logger = LoggerFactory.getLogger(UserCredentialsGenerator.class);

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final int PASSWORD_LENGTH = 10;

    private final SecureRandom random;
    private final PasswordEncoder passwordEncoder;

    public String generateUsername(String firstName, String lastName, List<String> existingUsernames) {
        String baseUsername = buildBaseUsername(firstName, lastName);
        List<String> normalizedUsernames = normalizeUsernames(existingUsernames);
        return findUniqueUsername(baseUsername, normalizedUsernames);
    }

    public String generateRawPassword() {
        StringBuilder password = generatePasswordContent();
        List<Character> characters = password.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());

        Collections.shuffle(characters, random);

        return characters.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    private StringBuilder generatePasswordContent() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        String all = UPPER + LOWER + DIGITS;

        password.append(randomChar(UPPER));
        password.append(randomChar(LOWER));
        password.append(randomChar(DIGITS));

        for (int i = 3; i < PASSWORD_LENGTH; i++) {
            password.append(randomChar(all));
        }

        return password;
    }

    private char randomChar(String characters) {
        return characters.charAt(random.nextInt(characters.length()));
    }

    private String buildBaseUsername(String firstName, String lastName) {
        return String.format("%s.%s", firstName, lastName).toLowerCase();
    }

    private List<String> normalizeUsernames(List<String> usernames) {
        return usernames.stream()
                .map(String::toLowerCase)
                .toList();
    }

    private String findUniqueUsername(String baseUsername, List<String> normalizedUsernames) {
        String username = baseUsername;
        int counter = 1;

        while (normalizedUsernames.contains(username.toLowerCase())) {
            username = baseUsername + counter;
            counter++;
        }

        if (!baseUsername.equalsIgnoreCase(username)) {
            logger.warn("User with similar username exists ({}), adding suffix", username);
        }

        return username;
    }
}
