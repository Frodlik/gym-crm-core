package com.gym.crm.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class UserCredentialsGeneratorTest {
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String EXPECTED_USERNAME = "John.Doe";
    private static final String EXPECTED_USERNAME_WITH_SUFFIX = "John.Doe1";
    private static final int PASSWORD_LENGTH = 10;
    private static final Pattern VALID_CHARACTERS_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");

    private UserCredentialsGenerator userCredentialsGenerator;

    @BeforeEach
    void setUp() {
        userCredentialsGenerator = new UserCredentialsGenerator();
    }

    @ParameterizedTest
    @MethodSource("usernameGenerationProvider")
    void generateUsername_ShouldGenerateExpectedUsername(String firstName, String lastName, List<String> existing, String expectedUsername) {
        String actualUsername = userCredentialsGenerator.generateUsername(firstName, lastName, existing);

        assertEquals(expectedUsername, actualUsername);
    }

    @Test
    void generateUsername_ShouldHandleLargeNumberOfConflicts() {
        List<String> existingUsernames = Arrays.asList(
                "Jane.Smith", "Jane.Smith1", "Jane.Smith2", "Jane.Smith3", "Jane.Smith4",
                "Jane.Smith5", "Jane.Smith6", "Jane.Smith7", "Jane.Smith8", "Jane.Smith9"
        );

        String result = userCredentialsGenerator.generateUsername("Jane", "Smith", existingUsernames);

        assertEquals("Jane.Smith10", result);
    }

    @Test
    void generateUsername_ShouldWorkWithSpecialCharactersInNames() {
        List<String> existingUsernames = Collections.emptyList();

        String result = userCredentialsGenerator.generateUsername("Jean-Pierre", "O'Connor", existingUsernames);

        assertEquals("Jean-Pierre.O'Connor", result);
    }

    @Test
    void generateUsername_ShouldHandleEmptyExistingUsernames() {
        List<String> existingUsernames = Collections.emptyList();

        String result = userCredentialsGenerator.generateUsername("Test", "User", existingUsernames);

        assertEquals("Test.User", result);
    }

    @Test
    void generateUsername_ShouldHandleNullValues() {
        assertThrows(NullPointerException.class, () ->
                userCredentialsGenerator.generateUsername("Test", "User", null));
    }

    @Test
    void generatePassword_ShouldGeneratePasswordWithCorrectLength() {
        String password = userCredentialsGenerator.generatePassword();

        assertNotNull(password);
        assertEquals(PASSWORD_LENGTH, password.length());
    }

    @Test
    void generatePassword_ShouldGeneratePasswordWithValidCharacters() {
        String password = userCredentialsGenerator.generatePassword();

        assertTrue(VALID_CHARACTERS_PATTERN.matcher(password).matches());
    }

    @Test
    void generatePassword_ShouldGenerateDifferentPasswords() {
        String password1 = userCredentialsGenerator.generatePassword();
        String password2 = userCredentialsGenerator.generatePassword();
        String password3 = userCredentialsGenerator.generatePassword();

        assertNotEquals(password1, password2);
        assertNotEquals(password2, password3);
        assertNotEquals(password1, password3);
    }

    @Test
    void generatePassword_ShouldNotReturnNullOrEmpty() {
        String password = userCredentialsGenerator.generatePassword();

        assertNotNull(password);
        assertFalse(password.isEmpty());
        assertFalse(password.isBlank());
    }

    @Test
    void generatePassword_ShouldUseAllAvailableCharacters() {
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;

        for (int i = 0; i < 100; i++) {
            String password = userCredentialsGenerator.generatePassword();

            for (char c : password.toCharArray()) {
                if (Character.isUpperCase(c)) hasUppercase = true;
                if (Character.isLowerCase(c)) hasLowercase = true;
                if (Character.isDigit(c)) hasDigit = true;
            }

            if (hasUppercase && hasLowercase && hasDigit) break;
        }

        assertTrue(hasUppercase, "Generated passwords should contain uppercase letters");
        assertTrue(hasLowercase, "Generated passwords should contain lowercase letters");
        assertTrue(hasDigit, "Generated passwords should contain digits");
    }

    @Test
    void generateUsername_ShouldHandleVeryLongNames() {
        String firstName = "VeryLongFirstNameThatExceedsNormalLength";
        String lastName = "VeryLongLastNameThatExceedsNormalLength";
        List<String> existingUsernames = Collections.emptyList();

        String result = userCredentialsGenerator.generateUsername(firstName, lastName, existingUsernames);

        assertEquals(firstName + "." + lastName, result);
    }

    @Test
    void generateUsername_ShouldBeConsistentForSameInputs() {
        List<String> existingUsernames = List.of("John.Doe", "John.Doe1");

        String result1 = userCredentialsGenerator.generateUsername(FIRST_NAME, LAST_NAME, existingUsernames);
        String result2 = userCredentialsGenerator.generateUsername(FIRST_NAME, LAST_NAME, existingUsernames);

        assertEquals(result1, result2);
        assertEquals("John.Doe2", result1);
    }

    static Stream<Arguments> usernameGenerationProvider() {
        return Stream.of(
                Arguments.of(FIRST_NAME, LAST_NAME, List.of(), EXPECTED_USERNAME),
                Arguments.of(FIRST_NAME, LAST_NAME, List.of(EXPECTED_USERNAME), EXPECTED_USERNAME_WITH_SUFFIX),
                Arguments.of(FIRST_NAME, LAST_NAME, List.of(EXPECTED_USERNAME, EXPECTED_USERNAME_WITH_SUFFIX, "John.Doe2"), "John.Doe3")
        );
    }
}
