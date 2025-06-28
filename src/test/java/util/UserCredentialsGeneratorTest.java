package util;

import com.gym.crm.util.UserCredentialsGenerator;
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

    static Stream<Arguments> usernameGenerationProvider() {
        return Stream.of(
                Arguments.of("John", "Doe", List.of(), "John.Doe"),
                Arguments.of("John", "Doe", List.of("John.Doe"), "John.Doe1"),
                Arguments.of("John", "Doe", List.of("John.Doe", "John.Doe1", "John.Doe2"), "John.Doe3")
        );
    }

    @Test
    void generateUsername_ShouldHandleLargeNumberOfConflicts() {
        String firstName = "Jane";
        String lastName = "Smith";
        List<String> existingUsernames = Arrays.asList(
                "Jane.Smith", "Jane.Smith1", "Jane.Smith2", "Jane.Smith3", "Jane.Smith4",
                "Jane.Smith5", "Jane.Smith6", "Jane.Smith7", "Jane.Smith8", "Jane.Smith9"
        );

        String result = userCredentialsGenerator.generateUsername(firstName, lastName, existingUsernames);

        assertEquals("Jane.Smith10", result);
    }

    @Test
    void generateUsername_ShouldWorkWithSpecialCharactersInNames() {
        String firstName = "Jean-Pierre";
        String lastName = "O'Connor";
        List<String> existingUsernames = Collections.emptyList();

        String result = userCredentialsGenerator.generateUsername(firstName, lastName, existingUsernames);

        assertEquals("Jean-Pierre.O'Connor", result);
    }

    @Test
    void generateUsername_ShouldHandleEmptyExistingUsernames() {
        String firstName = "Test";
        String lastName = "User";
        List<String> existingUsernames = Collections.emptyList();

        String result = userCredentialsGenerator.generateUsername(firstName, lastName, existingUsernames);

        assertEquals("Test.User", result);
    }

    @Test
    void generateUsername_ShouldHandleNullValues() {
        String firstName = "Test";
        String lastName = "User";
        List<String> existingUsernames = null;

        assertThrows(NullPointerException.class, () ->
                userCredentialsGenerator.generateUsername(firstName, lastName, existingUsernames));
    }

    @Test
    void generatePassword_ShouldGeneratePasswordWithCorrectLength() {
        String password = userCredentialsGenerator.generatePassword();

        assertNotNull(password);
        assertEquals(10, password.length());
    }

    @Test
    void generatePassword_ShouldGeneratePasswordWithValidCharacters() {
        Pattern validCharactersPattern = Pattern.compile("^[A-Za-z0-9]+$");

        String password = userCredentialsGenerator.generatePassword();

        assertTrue(validCharactersPattern.matcher(password).matches());
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
        String firstName = "John";
        String lastName = "Doe";
        List<String> existingUsernames = Arrays.asList("John.Doe", "John.Doe1");

        String result1 = userCredentialsGenerator.generateUsername(firstName, lastName, existingUsernames);
        String result2 = userCredentialsGenerator.generateUsername(firstName, lastName, existingUsernames);

        assertEquals(result1, result2);
        assertEquals("John.Doe2", result1);
    }
}
