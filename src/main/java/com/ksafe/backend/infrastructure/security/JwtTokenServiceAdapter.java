package com.ksafe.backend.infrastructure.security;

import com.ksafe.backend.domain.port.TokenServicePort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class JwtTokenServiceAdapter implements TokenServicePort {

    private static final String CLAIM_DEVICE_ID = "deviceId";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String TYPE_ENROLLMENT = "ENROLLMENT";
    private static final String TYPE_ACCESS = "ACCESS";
    private static final String TYPE_REFRESH = "REFRESH";

    private final SecretKey secretKey;
    private final Duration enrollmentExpiration;
    private final Duration accessExpiration;
    private final Duration refreshExpiration;

    public JwtTokenServiceAdapter(
            @Value("${security.jwt.secret}") String jwtSecret,
            @Value("${security.jwt.enrollment-expiration-minutes:5}") long enrollmentExpirationMinutes,
            @Value("${security.jwt.access-expiration-minutes:15}") long accessExpirationMinutes,
            @Value("${security.jwt.refresh-expiration-days:30}") long refreshExpirationDays
    ) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.enrollmentExpiration = Duration.ofMinutes(enrollmentExpirationMinutes);
        this.accessExpiration = Duration.ofMinutes(accessExpirationMinutes);
        this.refreshExpiration = Duration.ofDays(refreshExpirationDays);
    }

    @Override
    public String generateEnrollmentToken(Long userId, String deviceId) {
        return buildToken(userId, deviceId, TYPE_ENROLLMENT, enrollmentExpiration);
    }

    @Override
    public boolean isEnrollmentTokenValid(String enrollmentToken, String expectedDeviceId) {
        return validateTokenByTypeAndDevice(enrollmentToken, TYPE_ENROLLMENT, expectedDeviceId);
    }

    @Override
    public String generateAccessToken(Long userId, String deviceId) {
        return buildToken(userId, deviceId, TYPE_ACCESS, accessExpiration);
    }

    @Override
    public String generateRefreshToken(Long userId, String deviceId) {
        return buildToken(userId, deviceId, TYPE_REFRESH, refreshExpiration);
    }

    @Override
    public boolean isRefreshTokenJwtValid(String refreshToken, String expectedDeviceId) {
        return validateTokenByTypeAndDevice(refreshToken, TYPE_REFRESH, expectedDeviceId);
    }

    public Claims validateAccessTokenAndGetClaims(String accessToken) {
        Claims claims = parseClaims(accessToken);
        String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
        if (!TYPE_ACCESS.equals(tokenType)) {
            throw new JwtException("Token is not an access token");
        }
        return claims;
    }

    @Override
    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private String buildToken(Long userId, String deviceId, String tokenType, Duration ttl) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .claim(CLAIM_DEVICE_ID, deviceId)
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(secretKey)
                .compact();
    }

    private boolean validateTokenByTypeAndDevice(String token, String expectedType, String expectedDeviceId) {
        try {
            Claims claims = parseClaims(token);
            String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
            String deviceId = claims.get(CLAIM_DEVICE_ID, String.class);
            return expectedType.equals(tokenType) && expectedDeviceId.equals(deviceId);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
