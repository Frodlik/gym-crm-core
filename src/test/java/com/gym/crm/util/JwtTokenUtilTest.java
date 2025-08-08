package com.gym.crm.util;

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
class JwtTokenUtilTest {
    private static final String USERNAME = "anti.mage";
    private static final String JWT_SECRET = "myVeryLongSecretKeyForJWTTokenGenerationThatIsAtLeast32CharactersLong";
    private static final Long JWT_EXPIRATION = 3600L;
    private static final Long REFRESH_EXPIRATION = 86400L;

    @InjectMocks
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void testGenerateAccessToken_whenValidUsername_shouldGenerateValidToken() {
        configureJwtProperties();

        String token = jwtTokenUtil.generateAccessToken(USERNAME);

        assertNotNull(token);
        assertTrue(token.contains("."));
        String extractedUsername = jwtTokenUtil.getUsernameFromToken(token);
        assertEquals(USERNAME, extractedUsername);
        assertEquals("access", jwtTokenUtil.getTokenType(token));
    }

    @Test
    void testGenerateRefreshToken_whenValidUsername_shouldGenerateValidRefreshToken() {
        configureJwtProperties();

        String token = jwtTokenUtil.generateRefreshToken(USERNAME);

        assertNotNull(token);
        assertTrue(token.contains("."));
        String extractedUsername = jwtTokenUtil.getUsernameFromToken(token);
        assertEquals(USERNAME, extractedUsername);
        assertEquals("refresh", jwtTokenUtil.getTokenType(token));
    }

    @Test
    void testGetUsernameFromToken_whenValidToken_shouldReturnUsername() {
        configureJwtProperties();
        String token = jwtTokenUtil.generateAccessToken(USERNAME);

        String result = jwtTokenUtil.getUsernameFromToken(token);

        assertEquals(USERNAME, result);
    }

    @Test
    void testGetUsernameFromToken_whenInvalidToken_shouldThrowJwtException() {
        configureJwtProperties();
        String invalidToken = "invalid.jwt.token";

        assertThrows(JwtException.class, () -> jwtTokenUtil.getUsernameFromToken(invalidToken));
    }

    @Test
    void getUsernameFromToken_whenMalformedToken_shouldThrowJwtException() {
        configureJwtProperties();
        String malformedToken = "not.a.jwt";

        assertThrows(JwtException.class, () -> jwtTokenUtil.getUsernameFromToken(malformedToken));
    }

    @Test
    void testGetTokenType_whenAccessToken_shouldReturnAccessType() {
        configureJwtProperties();
        String token = jwtTokenUtil.generateAccessToken(USERNAME);

        String tokenType = jwtTokenUtil.getTokenType(token);

        assertEquals("access", tokenType);
    }

    @Test
    void testGetTokenType_whenRefreshToken_shouldReturnRefreshType() {
        configureJwtProperties();
        String token = jwtTokenUtil.generateRefreshToken(USERNAME);

        String tokenType = jwtTokenUtil.getTokenType(token);

        assertEquals("refresh", tokenType);
    }

    @Test
    void testGetExpirationDateFromToken_whenValidToken_shouldReturnFutureDate() {
        configureJwtProperties();
        String token = jwtTokenUtil.generateAccessToken(USERNAME);
        Date now = new Date();

        Date expirationDate = jwtTokenUtil.getExpirationDateFromToken(token);

        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(now));
    }

    @Test
    void isTokenExpired_whenValidToken_shouldReturnFalse() {
        configureJwtProperties();
        String token = jwtTokenUtil.generateAccessToken(USERNAME);

        Boolean isExpired = jwtTokenUtil.isTokenExpired(token);

        assertFalse(isExpired);
    }

    @Test
    void testIsTokenExpired_whenExpiredToken_shouldReturnTrue() {
        configureJwtPropertiesWithShortExpiration();
        String token = jwtTokenUtil.generateAccessToken(USERNAME);

        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Boolean isExpired = jwtTokenUtil.isTokenExpired(token);

        assertTrue(isExpired);
    }

    @Test
    void testValidateAccessToken_whenValidTokenAndMatchingUsername_shouldReturnTrue() {
        configureJwtProperties();
        String token = jwtTokenUtil.generateAccessToken(USERNAME);

        Boolean isValid = jwtTokenUtil.validateAccessToken(token, USERNAME);

        assertTrue(isValid);
    }

    @Test
    void testValidateAccessToken_whenValidTokenButDifferentUsername_shouldReturnFalse() {
        configureJwtProperties();
        String token = jwtTokenUtil.generateAccessToken(USERNAME);
        String differentUsername = "different.user";

        Boolean isValid = jwtTokenUtil.validateAccessToken(token, differentUsername);

        assertFalse(isValid);
    }

    @Test
    void testValidateAccessToken_whenRefreshTokenProvided_shouldReturnFalse() {
        configureJwtProperties();
        String refreshToken = jwtTokenUtil.generateRefreshToken(USERNAME);

        Boolean isValid = jwtTokenUtil.validateAccessToken(refreshToken, USERNAME);

        assertFalse(isValid);
    }

    @Test
    void testValidateAccessToken_whenExpiredToken_shouldReturnFalse() {
        configureJwtPropertiesWithShortExpiration();
        String token = jwtTokenUtil.generateAccessToken(USERNAME);

        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Boolean isValid = jwtTokenUtil.validateAccessToken(token, USERNAME);

        assertFalse(isValid);
    }

    @Test
    void testValidateRefreshToken_whenValidRefreshTokenAndMatchingUsername_shouldReturnTrue() {
        configureJwtProperties();
        String token = jwtTokenUtil.generateRefreshToken(USERNAME);

        Boolean isValid = jwtTokenUtil.validateRefreshToken(token, USERNAME);

        assertTrue(isValid);
    }

    @Test
    void testValidateRefreshToken_whenValidTokenButDifferentUsername_shouldReturnFalse() {
        configureJwtProperties();
        String token = jwtTokenUtil.generateRefreshToken(USERNAME);
        String differentUsername = "different.user";

        Boolean isValid = jwtTokenUtil.validateRefreshToken(token, differentUsername);

        assertFalse(isValid);
    }

    @Test
    void testValidateRefreshToken_whenAccessTokenProvided_shouldReturnFalse() {
        configureJwtProperties();
        String accessToken = jwtTokenUtil.generateAccessToken(USERNAME);

        Boolean isValid = jwtTokenUtil.validateRefreshToken(accessToken, USERNAME);

        assertFalse(isValid);
    }

    @Test
    void testValidateRefreshToken_whenExpiredRefreshToken_shouldReturnFalse() throws Exception {
        configureRefreshTokenWithShortExpiration();
        String token = jwtTokenUtil.generateRefreshToken(USERNAME);

        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Boolean isValid = jwtTokenUtil.validateRefreshToken(token, USERNAME);

        assertFalse(isValid);
    }

    @Test
    void testValidateToken_whenValidTokenAndMatchingUsername_shouldReturnTrue() {
        configureJwtProperties();
        String token = jwtTokenUtil.generateAccessToken(USERNAME);

        Boolean isValid = jwtTokenUtil.validateToken(token, USERNAME);

        assertTrue(isValid);
    }

    @Test
    void testValidateToken_whenValidTokenButDifferentUsername_shouldReturnFalse() {
        configureJwtProperties();
        String token = jwtTokenUtil.generateAccessToken(USERNAME);
        String differentUsername = "jane.smith";

        Boolean isValid = jwtTokenUtil.validateToken(token, differentUsername);

        assertFalse(isValid);
    }

    @Test
    void testValidateToken_whenExpiredToken_shouldReturnFalse() {
        configureJwtPropertiesWithShortExpiration();
        String token = jwtTokenUtil.generateAccessToken(USERNAME);

        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Boolean isValid = jwtTokenUtil.validateToken(token, USERNAME);

        assertFalse(isValid);
    }

    @Test
    void testGenerateAccessToken_whenSecretTooShort_shouldPadSecretAndGenerateToken() {
        String shortSecret = "shortkey";
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", shortSecret);
        ReflectionTestUtils.setField(jwtTokenUtil, "jwtExpiration", JWT_EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenUtil, "refreshExpiration", REFRESH_EXPIRATION);

        String token = jwtTokenUtil.generateAccessToken(USERNAME);

        assertNotNull(token);
        String extractedUsername = jwtTokenUtil.getUsernameFromToken(token);
        assertEquals(USERNAME, extractedUsername);
    }

    @Test
    void testGetClaimFromToken_whenValidToken_shouldReturnSpecificClaim() {
        configureJwtProperties();
        String token = jwtTokenUtil.generateAccessToken(USERNAME);

        String subject = jwtTokenUtil.getClaimFromToken(token, Claims::getSubject);
        String tokenType = jwtTokenUtil.getClaimFromToken(token, claims -> claims.get("type", String.class));

        assertEquals(USERNAME, subject);
        assertEquals("access", tokenType);
    }

    private void configureJwtProperties() {
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", JWT_SECRET);
        ReflectionTestUtils.setField(jwtTokenUtil, "jwtExpiration", JWT_EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenUtil, "refreshExpiration", REFRESH_EXPIRATION);
    }

    private void configureJwtPropertiesWithShortExpiration() {
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", JWT_SECRET);
        ReflectionTestUtils.setField(jwtTokenUtil, "jwtExpiration", 1L);
        ReflectionTestUtils.setField(jwtTokenUtil, "refreshExpiration", REFRESH_EXPIRATION);
    }

    private void configureRefreshTokenWithShortExpiration() {
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", JWT_SECRET);
        ReflectionTestUtils.setField(jwtTokenUtil, "jwtExpiration", JWT_EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenUtil, "refreshExpiration", 1L);
    }
}
