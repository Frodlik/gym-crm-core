package com.gym.crm.security;

import com.gym.crm.service.enums.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;

@Component
public class JwtTokenHandler {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.refresh.expiration}")
    private Long refreshExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);

            return Keys.hmacShaKeyFor(paddedKey);
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String username) {
        return createToken(username, jwtExpiration, "access");
    }

    public String generateRefreshToken(String username) {
        return createToken(username, refreshExpiration, "refresh");
    }

    private String createToken(String subject, Long expiration, String tokenType) {
        Instant now = Instant.now();
        Instant expirationTime = now.plusSeconds(expiration);

        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .subject(subject)
                .claim("type", tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public String getTokenType(String token) {
        return getClaimFromToken(token, claims -> claims.get("type", String.class));
    }

    public TokenType getTokenTypeEnum(String token) {
        String tokenTypeString = getTokenType(token);
        return TokenType.fromValue(tokenTypeString);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);

        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenExpired(String token) {
        try {
            final Date tokenExpiration = getExpirationDateFromToken(token);

            return tokenExpiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public boolean validateAccessToken(String token, String username) {
        try {
            String tokenUsername = getUsernameFromToken(token);
            TokenType tokenType = getTokenTypeEnum(token);

            return Objects.equals(username, tokenUsername) && TokenType.ACCESS == tokenType && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token, String username) {
        try {
            String tokenUsername = getUsernameFromToken(token);
            TokenType tokenType = getTokenTypeEnum(token);

            return Objects.equals(username, tokenUsername) && TokenType.REFRESH == tokenType && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
