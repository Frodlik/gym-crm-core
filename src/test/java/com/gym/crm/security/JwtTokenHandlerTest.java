package com.gym.crm.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class JwtTokenHandlerTest {
    private static final String USERNAME = "anti.mage";
    private static final String JWT_SECRET = "myVeryLongSecretKeyForJWTTokenGenerationThatIsAtLeast32CharactersLong";
    private static final Long JWT_EXPIRATION = 3600L;
    private static final Long REFRESH_EXPIRATION = 86400L;

    @InjectMocks
    private JwtTokenHandler jwtTokenHandler;

    @Test
    void testGenerateAccessToken_whenValidUsername_shouldGenerateValidToken() {
        configureJwtProperties();

        String token = jwtTokenHandler.generateAccessToken(USERNAME);

        assertNotNull(token);
        assertTrue(token.contains("."));
        String extractedUsername = jwtTokenHandler.getUsernameFromToken(token);
        assertEquals(USERNAME, extractedUsername);
        assertEquals("access", jwtTokenHandler.getTokenType(token));
    }

    @Test
    void testGenerateRefreshToken_whenValidUsername_shouldGenerateValidRefreshToken() {
        configureJwtProperties();

        String token = jwtTokenHandler.generateRefreshToken(USERNAME);

        assertNotNull(token);
        assertTrue(token.contains("."));
        String extractedUsername = jwtTokenHandler.getUsernameFromToken(token);
        assertEquals(USERNAME, extractedUsername);
        assertEquals("refresh", jwtTokenHandler.getTokenType(token));
    }

    @Test
    void testGetUsernameFromToken_whenValidToken_shouldReturnUsername() {
        configureJwtProperties();
        String token = jwtTokenHandler.generateAccessToken(USERNAME);

        String result = jwtTokenHandler.getUsernameFromToken(token);

        assertEquals(USERNAME, result);
    }

    @Test
    void testGetUsernameFromToken_whenInvalidToken_shouldThrowJwtException() {
        configureJwtProperties();
        String invalidToken = "invalid.jwt.token";

        assertThrows(JwtException.class, () -> jwtTokenHandler.getUsernameFromToken(invalidToken));
    }

    @Test
    void getUsernameFromToken_whenMalformedToken_shouldThrowJwtException() {
        configureJwtProperties();
        String malformedToken = "not.a.jwt";

        assertThrows(JwtException.class, () -> jwtTokenHandler.getUsernameFromToken(malformedToken));
    }

    @Test
    void testGetTokenType_whenAccessToken_shouldReturnAccessType() {
        configureJwtProperties();
        String token = jwtTokenHandler.generateAccessToken(USERNAME);

        String tokenType = jwtTokenHandler.getTokenType(token);

        assertEquals("access", tokenType);
    }

    @Test
    void testGetTokenType_whenRefreshToken_shouldReturnRefreshType() {
        configureJwtProperties();
        String token = jwtTokenHandler.generateRefreshToken(USERNAME);

        String tokenType = jwtTokenHandler.getTokenType(token);

        assertEquals("refresh", tokenType);
    }

    @Test
    void testGetExpirationDateFromToken_whenValidToken_shouldReturnFutureDate() {
        configureJwtProperties();
        String token = jwtTokenHandler.generateAccessToken(USERNAME);
        Date now = new Date();

        Date expirationDate = jwtTokenHandler.getExpirationDateFromToken(token);

        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(now));
    }

    @Test
    void isTokenExpired_whenValidToken_shouldReturnFalse() {
        configureJwtProperties();
        String token = jwtTokenHandler.generateAccessToken(USERNAME);

        boolean isExpired = jwtTokenHandler.isTokenExpired(token);

        assertFalse(isExpired);
    }

    @Test
    void testIsTokenExpired_whenExpiredToken_shouldReturnTrue() {
        configureJwtPropertiesWithShortExpiration();
        String token = jwtTokenHandler.generateAccessToken(USERNAME);

        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean isExpired = jwtTokenHandler.isTokenExpired(token);

        assertTrue(isExpired);
    }

    @Test
    void testValidateAccessToken_whenValidTokenAndMatchingUsername_shouldReturnTrue() {
        configureJwtProperties();
        String token = jwtTokenHandler.generateAccessToken(USERNAME);

        boolean isValid = jwtTokenHandler.validateAccessToken(token, USERNAME);

        assertTrue(isValid);
    }

    @Test
    void testValidateAccessToken_whenValidTokenButDifferentUsername_shouldReturnFalse() {
        configureJwtProperties();
        String token = jwtTokenHandler.generateAccessToken(USERNAME);
        String differentUsername = "different.user";

        boolean isValid = jwtTokenHandler.validateAccessToken(token, differentUsername);

        assertFalse(isValid);
    }

    @Test
    void testValidateAccessToken_whenRefreshTokenProvided_shouldReturnFalse() {
        configureJwtProperties();
        String refreshToken = jwtTokenHandler.generateRefreshToken(USERNAME);

        boolean isValid = jwtTokenHandler.validateAccessToken(refreshToken, USERNAME);

        assertFalse(isValid);
    }

    @Test
    void testValidateAccessToken_whenExpiredToken_shouldReturnFalse() {
        configureJwtPropertiesWithShortExpiration();
        String token = jwtTokenHandler.generateAccessToken(USERNAME);

        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean isValid = jwtTokenHandler.validateAccessToken(token, USERNAME);

        assertFalse(isValid);
    }

    @Test
    void testValidateRefreshToken_whenValidRefreshTokenAndMatchingUsername_shouldReturnTrue() {
        configureJwtProperties();
        String token = jwtTokenHandler.generateRefreshToken(USERNAME);

        boolean isValid = jwtTokenHandler.validateRefreshToken(token, USERNAME);

        assertTrue(isValid);
    }

    @Test
    void testValidateRefreshToken_whenValidTokenButDifferentUsername_shouldReturnFalse() {
        configureJwtProperties();
        String token = jwtTokenHandler.generateRefreshToken(USERNAME);
        String differentUsername = "different.user";

        boolean isValid = jwtTokenHandler.validateRefreshToken(token, differentUsername);

        assertFalse(isValid);
    }

    @Test
    void testValidateRefreshToken_whenAccessTokenProvided_shouldReturnFalse() {
        configureJwtProperties();
        String accessToken = jwtTokenHandler.generateAccessToken(USERNAME);

        boolean isValid = jwtTokenHandler.validateRefreshToken(accessToken, USERNAME);

        assertFalse(isValid);
    }

    @Test
    void testValidateRefreshToken_whenExpiredRefreshToken_shouldReturnFalse() throws Exception {
        configureRefreshTokenWithShortExpiration();
        String token = jwtTokenHandler.generateRefreshToken(USERNAME);

        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean isValid = jwtTokenHandler.validateRefreshToken(token, USERNAME);

        assertFalse(isValid);
    }

    @Test
    void testGenerateAccessToken_whenSecretTooShort_shouldPadSecretAndGenerateToken() {
        String shortSecret = "shortkey";
        ReflectionTestUtils.setField(jwtTokenHandler, "secret", shortSecret);
        ReflectionTestUtils.setField(jwtTokenHandler, "jwtExpiration", JWT_EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenHandler, "refreshExpiration", REFRESH_EXPIRATION);

        String token = jwtTokenHandler.generateAccessToken(USERNAME);

        assertNotNull(token);
        String extractedUsername = jwtTokenHandler.getUsernameFromToken(token);
        assertEquals(USERNAME, extractedUsername);
    }

    @Test
    void testGetClaimFromToken_whenValidToken_shouldReturnSpecificClaim() {
        configureJwtProperties();
        String token = jwtTokenHandler.generateAccessToken(USERNAME);

        String subject = jwtTokenHandler.getClaimFromToken(token, Claims::getSubject);
        String tokenType = jwtTokenHandler.getClaimFromToken(token, claims -> claims.get("type", String.class));

        assertEquals(USERNAME, subject);
        assertEquals("access", tokenType);
    }

    private void configureJwtProperties() {
        ReflectionTestUtils.setField(jwtTokenHandler, "secret", JWT_SECRET);
        ReflectionTestUtils.setField(jwtTokenHandler, "jwtExpiration", JWT_EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenHandler, "refreshExpiration", REFRESH_EXPIRATION);
    }

    private void configureJwtPropertiesWithShortExpiration() {
        ReflectionTestUtils.setField(jwtTokenHandler, "secret", JWT_SECRET);
        ReflectionTestUtils.setField(jwtTokenHandler, "jwtExpiration", 1L);
        ReflectionTestUtils.setField(jwtTokenHandler, "refreshExpiration", REFRESH_EXPIRATION);
    }

    private void configureRefreshTokenWithShortExpiration() {
        ReflectionTestUtils.setField(jwtTokenHandler, "secret", JWT_SECRET);
        ReflectionTestUtils.setField(jwtTokenHandler, "jwtExpiration", JWT_EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenHandler, "refreshExpiration", 1L);
    }
}
