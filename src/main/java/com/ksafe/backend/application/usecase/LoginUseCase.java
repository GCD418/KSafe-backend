package com.ksafe.backend.application.usecase;

import com.ksafe.backend.domain.exception.UnauthorizedException;
import com.ksafe.backend.domain.model.User;
import com.ksafe.backend.domain.model.UserSession;
import com.ksafe.backend.domain.port.PasswordHasherPort;
import com.ksafe.backend.domain.port.TokenServicePort;
import com.ksafe.backend.domain.port.UserRepositoryPort;
import com.ksafe.backend.domain.port.UserSessionRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LoginUseCase {

    private final UserRepositoryPort userRepository;
    private final UserSessionRepositoryPort userSessionRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenServicePort tokenService;

    public LoginUseCase(
            UserRepositoryPort userRepository,
            UserSessionRepositoryPort userSessionRepository,
            PasswordHasherPort passwordHasher,
            TokenServicePort tokenService
    ) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
    }

    @Transactional
    public String execute(String email, String password, String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            throw new IllegalArgumentException("deviceId is required");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String enrollmentToken = tokenService.generateEnrollmentToken(user.getId(), deviceId);
        LocalDateTime now = LocalDateTime.now();

        UserSession session = userSessionRepository.findByDeviceId(deviceId)
                .orElseGet(UserSession::new);

        session.setUserId(user.getId());
        session.setDeviceId(deviceId);
        session.setEnrollmentToken(enrollmentToken);
        session.setRefreshTokenHash(null);
        session.setRefreshTokenExpiresAt(null);
        session.setIsRevoked(false);
        session.setUpdatedAt(now);

        userSessionRepository.save(session);

        return enrollmentToken;
    }
}
