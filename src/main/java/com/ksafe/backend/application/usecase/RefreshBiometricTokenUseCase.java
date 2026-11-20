package com.ksafe.backend.application.usecase;

import com.ksafe.backend.domain.exception.InvalidRefreshTokenException;
import com.ksafe.backend.domain.model.TokenPair;
import com.ksafe.backend.domain.model.UserSession;
import com.ksafe.backend.domain.port.TokenServicePort;
import com.ksafe.backend.domain.port.UserSessionRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Service
public class RefreshBiometricTokenUseCase {

    private final UserSessionRepositoryPort userSessionRepository;
    private final TokenServicePort tokenService;
    private final long refreshTokenTtlDays;

    public RefreshBiometricTokenUseCase(
            UserSessionRepositoryPort userSessionRepository,
            TokenServicePort tokenService,
            @Value("${security.jwt.refresh-expiration-days:30}") long refreshTokenTtlDays
    ) {
        this.userSessionRepository = userSessionRepository;
        this.tokenService = tokenService;
        this.refreshTokenTtlDays = refreshTokenTtlDays;
    }

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public TokenPair execute(String refreshToken, String deviceId) {
        if (refreshToken == null || refreshToken.isBlank() || deviceId == null || deviceId.isBlank()) {
            throw new InvalidRefreshTokenException("Refresh token and deviceId are required");
        }

        UserSession session = userSessionRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new InvalidRefreshTokenException("No active session found for device"));

        LocalDateTime now = LocalDateTime.now();
        String incomingRefreshTokenHash = tokenService.hashToken(refreshToken);

        boolean hashMatches = session.getRefreshTokenHash() != null
                && MessageDigest.isEqual(
                        session.getRefreshTokenHash().getBytes(StandardCharsets.UTF_8),
                        incomingRefreshTokenHash.getBytes(StandardCharsets.UTF_8)
                );

        boolean isExpired = session.getRefreshTokenExpiresAt() == null
                || session.getRefreshTokenExpiresAt().isBefore(now)
                || !tokenService.isRefreshTokenJwtValid(refreshToken, deviceId);

        if (session.isRevoked() || !hashMatches || isExpired) {
            // Defensive security (Session Theft): revoke the entire device session immediately
            userSessionRepository.revokeAllByDeviceId(deviceId);
            throw new InvalidRefreshTokenException(
                    "Invalid, expired, or reused refresh token. Device session has been revoked for security."
            );
        }

        // Atomic operation: rotate refreshToken and issue a new accessToken
        String newAccessToken = tokenService.generateAccessToken(session.getUserId(), deviceId);
        String newRefreshToken = tokenService.generateRefreshToken(session.getUserId(), deviceId);
        String newRefreshTokenHash = tokenService.hashToken(newRefreshToken);

        session.setEnrollmentToken(null);
        session.setRefreshTokenHash(newRefreshTokenHash);
        session.setRefreshTokenExpiresAt(now.plusDays(refreshTokenTtlDays));
        session.setIsRevoked(false);
        session.setUpdatedAt(now);

        userSessionRepository.save(session);

        return new TokenPair(newAccessToken, newRefreshToken);
    }
}
