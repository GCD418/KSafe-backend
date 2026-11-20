package com.ksafe.backend.domain.port;

public interface TokenServicePort {

    String generateEnrollmentToken(Long userId, String deviceId);

    boolean isEnrollmentTokenValid(String enrollmentToken, String deviceId);

    String generateAccessToken(Long userId, String deviceId);

    String generateRefreshToken(Long userId, String deviceId);

    boolean isRefreshTokenJwtValid(String refreshToken, String deviceId);

    String hashToken(String rawToken);
}
