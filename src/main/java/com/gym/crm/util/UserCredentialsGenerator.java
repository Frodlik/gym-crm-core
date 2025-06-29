package com.gym.crm.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserCredentialsGenerator {
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final int PASSWORD_LENGTH = 10;

    private final SecureRandom random = new SecureRandom();

    public String generateUsername(String firstName, String lastName, List<String> existingUsernames) {
        String baseUsername = firstName + "." + lastName;
        String username = baseUsername;
        int counter = 1;

        while (existingUsernames.contains(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }

    public String generatePassword() {
        StringBuilder rawPassword = generateRawPassword();
        List<Character> characters = rawPassword.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());

        Collections.shuffle(characters, random);

        return characters.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    private StringBuilder generateRawPassword() {
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
}
