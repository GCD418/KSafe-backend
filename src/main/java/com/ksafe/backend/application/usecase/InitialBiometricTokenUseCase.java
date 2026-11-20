package com.ksafe.backend.application.usecase;

import com.ksafe.backend.domain.exception.InvalidEnrollmentTokenException;
import com.ksafe.backend.domain.model.TokenPair;
import com.ksafe.backend.domain.model.UserSession;
import com.ksafe.backend.domain.port.TokenServicePort;
import com.ksafe.backend.domain.port.UserSessionRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class InitialBiometricTokenUseCase {

    private final UserSessionRepositoryPort userSessionRepository;
    private final TokenServicePort tokenService;
    private final long enrollmentTokenTtlMinutes;
    private final long refreshTokenTtlDays;

    public InitialBiometricTokenUseCase(
            UserSessionRepositoryPort userSessionRepository,
            TokenServicePort tokenService,
            @Value("${security.jwt.enrollment-expiration-minutes:5}") long enrollmentTokenTtlMinutes,
            @Value("${security.jwt.refresh-expiration-days:30}") long refreshTokenTtlDays
    ) {
        this.userSessionRepository = userSessionRepository;
        this.tokenService = tokenService;
        this.enrollmentTokenTtlMinutes = enrollmentTokenTtlMinutes;
        this.refreshTokenTtlDays = refreshTokenTtlDays;
    }

    @Transactional
    public TokenPair execute(String enrollmentToken, String deviceId) {
        if (enrollmentToken == null || enrollmentToken.isBlank() || deviceId == null || deviceId.isBlank()) {
            throw new InvalidEnrollmentTokenException("Enrollment token and deviceId are required");
        }

        UserSession session = userSessionRepository
                .findByEnrollmentTokenAndDeviceId(enrollmentToken, deviceId)
                .orElseThrow(() -> new InvalidEnrollmentTokenException("Enrollment token is invalid or has already been used"));

        LocalDateTime now = LocalDateTime.now();

        if (session.isRevoked() || session.getEnrollmentToken() == null) {
            throw new InvalidEnrollmentTokenException("Session is revoked or enrollment token was already consumed");
        }

        boolean expiredByTimestamp = session.getUpdatedAt() != null
                && Duration.between(session.getUpdatedAt(), now).toMinutes() >= enrollmentTokenTtlMinutes;

        if (expiredByTimestamp || !tokenService.isEnrollmentTokenValid(enrollmentToken, deviceId)) {
            throw new InvalidEnrollmentTokenException("Enrollment token has expired");
        }

        // Atomic operation: burn the single-use enrollmentToken and issue the first token pair
        session.setEnrollmentToken(null);

        String accessToken = tokenService.generateAccessToken(session.getUserId(), deviceId);
        String refreshToken = tokenService.generateRefreshToken(session.getUserId(), deviceId);
        String refreshTokenHash = tokenService.hashToken(refreshToken);

        session.setRefreshTokenHash(refreshTokenHash);
        session.setRefreshTokenExpiresAt(now.plusDays(refreshTokenTtlDays));
        session.setIsRevoked(false);
        session.setUpdatedAt(now);

        userSessionRepository.save(session);

        return new TokenPair(accessToken, refreshToken);
    }
}
