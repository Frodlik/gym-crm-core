package com.gym.crm.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
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
    private static final String EXPECTED_USERNAME = "john.doe";
    private static final String EXPECTED_USERNAME_WITH_SUFFIX = "john.doe1";

    private final SecureRandom secureRandom = new SecureRandom();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserCredentialsGenerator sut;

    @BeforeEach
    void setUp() {
        sut = new UserCredentialsGenerator(secureRandom, passwordEncoder);
    }

    @ParameterizedTest
    @MethodSource("usernameGenerationProvider")
    void generateUsername_ShouldGenerateExpectedUsername(String firstName, String lastName, List<String> existing, String expectedUsername) {
        String actual = sut.generateUsername(firstName, lastName, existing);

        assertEquals(expectedUsername, actual);
    }

    @Test
    void generateUsername_ShouldHandleLargeNumberOfConflicts() {
        List<String> existingUsernames = List.of(
                "Jane.Smith", "jane.smith1", "jane.smith2", "jane.smith3", "jane.smith4",
                "jane.smith5", "jane.smith6", "jane.smith7", "jane.smith8", "jane.smith9"
        );
        String expected = "jane.smith10";

        String actual = sut.generateUsername("Jane", "Smith", existingUsernames);

        assertEquals(expected, actual);
    }

    @Test
    void generateUsername_ShouldWorkWithSpecialCharactersInNames() {
        List<String> existingUsernames = Collections.emptyList();
        String expected = "jean-pierre.o'connor";

        String actual = sut.generateUsername("Jean-Pierre", "O'Connor", existingUsernames);

        assertEquals(expected, actual);
    }

    @Test
    void generateUsername_ShouldHandleEmptyExistingUsernames() {
        List<String> existingUsernames = Collections.emptyList();
        String expected = "test.user";

        String actual = sut.generateUsername("Test", "User", existingUsernames);

        assertEquals(expected, actual);
    }

    @Test
    void generateUsername_ShouldHandleNullValues() {
        assertThrows(NullPointerException.class, () ->
                sut.generateUsername("Test", "User", null));
    }

    @Test
    void generateUsername_ShouldHandleCaseInsensitiveComparison() {
        List<String> existingUsernames = List.of("JOHN.DOE", "John.Doe1");
        String expected = "john.doe2";

        String actual = sut.generateUsername(FIRST_NAME, LAST_NAME, existingUsernames);

        assertEquals(expected, actual);
    }

    @Test
    void generateRawPassword_ShouldReturnValidPassword() {
        String rawPassword = sut.generateRawPassword();

        assertNotNull(rawPassword);
        assertFalse(rawPassword.isBlank());
        assertEquals(10, rawPassword.length());
    }

    @Test
    void generateRawPassword_ShouldBeDifferentEachTime() {
        String password1 = sut.generateRawPassword();
        String password2 = sut.generateRawPassword();
        String password3 = sut.generateRawPassword();

        assertNotEquals(password1, password2);
        assertNotEquals(password2, password3);
        assertNotEquals(password1, password3);
    }

    @Test
    void generateRawPassword_ShouldNotReturnNullOrEmpty() {
        String actual = sut.generateRawPassword();

        assertNotNull(actual);
        assertFalse(actual.isEmpty());
        assertFalse(actual.isBlank());
    }

    @Test
    void generateRawPassword_ShouldContainRequiredCharacterTypes() {
        String actual = sut.generateRawPassword();

        assertAll("Password must contain required character types",
                () -> assertTrue(actual.chars().anyMatch(Character::isUpperCase), "Must contain uppercase letter"),
                () -> assertTrue(actual.chars().anyMatch(Character::isLowerCase), "Must contain lowercase letter"),
                () -> assertTrue(actual.chars().anyMatch(Character::isDigit), "Must contain digit"));
    }

    @Test
    void encodePassword_ShouldReturnValidBCryptHash() {
        String rawPassword = "testPassword123";
        String hashedPassword = sut.encodePassword(rawPassword);

        assertNotNull(hashedPassword);
        assertFalse(hashedPassword.isBlank());
        assertTrue(hashedPassword.startsWith("$2"));
        assertTrue(hashedPassword.length() >= 60);
    }

    @Test
    void encodePassword_ShouldBeDifferentForSamePassword() {
        String rawPassword = "testPassword123";
        String hash1 = sut.encodePassword(rawPassword);
        String hash2 = sut.encodePassword(rawPassword);
        String hash3 = sut.encodePassword(rawPassword);

        assertNotEquals(hash1, hash2);
        assertNotEquals(hash2, hash3);
        assertNotEquals(hash1, hash3);
    }

    @Test
    void matches_ShouldReturnTrueForCorrectPassword() {
        String rawPassword = "testPassword123";
        String encodedPassword = sut.encodePassword(rawPassword);

        assertTrue(sut.matches(rawPassword, encodedPassword));
    }

    @Test
    void matches_ShouldReturnFalseForIncorrectPassword() {
        String rawPassword = "testPassword123";
        String wrongPassword = "wrongPassword456";
        String encodedPassword = sut.encodePassword(rawPassword);

        assertFalse(sut.matches(wrongPassword, encodedPassword));
    }

    @Test
    void generateUsername_ShouldHandleVeryLongNames() {
        String firstName = "VeryLongFirstNameThatExceedsNormalLength";
        String lastName = "VeryLongLastNameThatExceedsNormalLength";
        List<String> existingUsernames = Collections.emptyList();
        String expected = (firstName + "." + lastName).toLowerCase();

        String actual = sut.generateUsername(firstName, lastName, existingUsernames);

        assertEquals(expected, actual);
    }

    @Test
    void generateUsername_ShouldBeConsistentForSameInputs() {
        List<String> existingUsernames = List.of("john.doe", "john.doe1");

        String result1 = sut.generateUsername(FIRST_NAME, LAST_NAME, existingUsernames);
        String result2 = sut.generateUsername(FIRST_NAME, LAST_NAME, existingUsernames);

        assertEquals(result1, result2);
        assertEquals("john.doe2", result1);
    }

    @Test
    void fullPasswordWorkflow_ShouldWorkCorrectly() {
        String rawPassword = sut.generateRawPassword();

        String encodedPassword = sut.encodePassword(rawPassword);
        String wrongPassword = sut.generateRawPassword();

        assertTrue(sut.matches(rawPassword, encodedPassword));
        assertFalse(sut.matches(wrongPassword, encodedPassword));
    }

    static Stream<Arguments> usernameGenerationProvider() {
        return Stream.of(
                Arguments.of(FIRST_NAME, LAST_NAME, List.of(), EXPECTED_USERNAME),
                Arguments.of(FIRST_NAME, LAST_NAME, List.of("john.doe"), EXPECTED_USERNAME_WITH_SUFFIX),
                Arguments.of(FIRST_NAME, LAST_NAME, List.of("john.doe", "john.doe1", "john.doe2"), "john.doe3"),
                Arguments.of("Test", "User", List.of("TEST.USER"), "test.user1")
        );
    }
}
