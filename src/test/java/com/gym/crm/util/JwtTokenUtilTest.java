package com.gym.crm.util;

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

    @InjectMocks
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void testGenerateToken_whenValidUsername_shouldGenerateValidToken() {
        setJwtProperties();
        String token = jwtTokenUtil.generateAccessToken(USERNAME);

        assertNotNull(token);
        assertTrue(token.contains("."));

        String extractedUsername = jwtTokenUtil.getUsernameFromToken(token);
        assertEquals(USERNAME, extractedUsername);
    }

    @Test
    void testGetUsernameFromToken_whenValidToken_shouldReturnUsername() {
        setJwtProperties();
        String token = jwtTokenUtil.generateAccessToken(USERNAME);

        String result = jwtTokenUtil.getUsernameFromToken(token);

        assertEquals(USERNAME, result);
    }

    @Test
    void testGetExpirationDateFromToken_whenValidToken_shouldReturnFutureDate() {
        setJwtProperties();
        String token = jwtTokenUtil.generateAccessToken(USERNAME);
        Date now = new Date();

        Date expirationDate = jwtTokenUtil.getExpirationDateFromToken(token);

        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(now));
    }

    @Test
    void testIsTokenExpired_whenValidToken_shouldReturnFalse() {
        setJwtProperties();
        String token = jwtTokenUtil.generateAccessToken(USERNAME);

        Boolean isExpired = jwtTokenUtil.isTokenExpired(token);

        assertFalse(isExpired);
    }

    @Test
    void testValidateToken_whenValidTokenAndMatchingUsername_shouldReturnTrue() {
        setJwtProperties();
        String token = jwtTokenUtil.generateAccessToken(USERNAME);

        Boolean isValid = jwtTokenUtil.validateToken(token, USERNAME);

        assertTrue(isValid);
    }

    @Test
    void testValidateToken_whenValidTokenButDifferentUsername_shouldReturnFalse() {
        setJwtProperties();
        String token = jwtTokenUtil.generateAccessToken(USERNAME);
        String differentUsername = "jane.smith";

        Boolean isValid = jwtTokenUtil.validateToken(token, differentUsername);

        assertFalse(isValid);
    }

    @Test
    void testValidateToken_whenExpiredToken_shouldReturnFalse() {
        setJwtPropertiesWithShortExpiration();
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
    void testGetUsernameFromToken_whenInvalidToken_shouldThrowJwtException() {
        setJwtProperties();
        String invalidToken = "invalid.jwt.token";

        assertThrows(JwtException.class, () -> jwtTokenUtil.getUsernameFromToken(invalidToken));
    }

    @Test
    void testGetUsernameFromToken_whenMalformedToken_shouldThrowJwtException() {
        setJwtProperties();
        String malformedToken = "not.a.jwt";

        assertThrows(JwtException.class, () -> jwtTokenUtil.getUsernameFromToken(malformedToken));
    }

    @Test
    void testGenerateToken_whenSecretTooShort_shouldPadSecretAndGenerateToken() {
        String shortSecret = "shortkey";
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", shortSecret);
        ReflectionTestUtils.setField(jwtTokenUtil, "jwtExpiration", JWT_EXPIRATION);

        String token = jwtTokenUtil.generateAccessToken(USERNAME);

        assertNotNull(token);
        String extractedUsername = jwtTokenUtil.getUsernameFromToken(token);
        assertEquals(USERNAME, extractedUsername);
    }

    @Test
    void testIsTokenExpired_whenExpiredToken_shouldReturnTrue() {
        setJwtPropertiesWithShortExpiration();
        String token = jwtTokenUtil.generateAccessToken(USERNAME);

        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Boolean isExpired = jwtTokenUtil.isTokenExpired(token);

        assertTrue(isExpired);
    }

    private void setJwtProperties() {
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", JWT_SECRET);
        ReflectionTestUtils.setField(jwtTokenUtil, "jwtExpiration", JWT_EXPIRATION);
    }

    private void setJwtPropertiesWithShortExpiration() {
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", JWT_SECRET);
        ReflectionTestUtils.setField(jwtTokenUtil, "jwtExpiration", 1L);
    }
}
