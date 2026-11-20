package com.ksafe.backend.infrastructure.persistence.adapter;

import com.ksafe.backend.domain.model.UserSession;
import com.ksafe.backend.domain.port.UserSessionRepositoryPort;
import com.ksafe.backend.infrastructure.persistence.entity.UserSessionEntity;
import com.ksafe.backend.infrastructure.persistence.repository.JpaUserSessionRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class UserSessionRepositoryAdapter implements UserSessionRepositoryPort {

    private final JpaUserSessionRepository jpaUserSessionRepository;

    public UserSessionRepositoryAdapter(JpaUserSessionRepository jpaUserSessionRepository) {
        this.jpaUserSessionRepository = jpaUserSessionRepository;
    }

    @Override
    public Optional<UserSession> findByDeviceId(String deviceId) {
        return jpaUserSessionRepository.findFirstByDeviceIdOrderByUpdatedAtDesc(deviceId)
                .map(this::toDomain);
    }

    @Override
    public Optional<UserSession> findByEnrollmentTokenAndDeviceId(String enrollmentToken, String deviceId) {
        return jpaUserSessionRepository.findByEnrollmentTokenAndDeviceId(enrollmentToken, deviceId)
                .map(this::toDomain);
    }

    @Override
    public UserSession save(UserSession userSession) {
        UserSessionEntity entity = toEntity(userSession);
        UserSessionEntity saved = jpaUserSessionRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void revokeAllByDeviceId(String deviceId) {
        List<UserSessionEntity> sessions = jpaUserSessionRepository.findAllByDeviceId(deviceId);
        LocalDateTime now = LocalDateTime.now();
        for (UserSessionEntity session : sessions) {
            session.setIsRevoked(true);
            session.setEnrollmentToken(null);
            session.setRefreshTokenHash(null);
            session.setUpdatedAt(now);
        }
        jpaUserSessionRepository.saveAll(sessions);
    }

    private UserSession toDomain(UserSessionEntity entity) {
        return new UserSession(
                entity.getId(),
                entity.getUserId(),
                entity.getDeviceId(),
                entity.getEnrollmentToken(),
                entity.getRefreshTokenHash(),
                entity.getRefreshTokenExpiresAt(),
                entity.getIsRevoked(),
                entity.getUpdatedAt()
        );
    }

    private UserSessionEntity toEntity(UserSession domain) {
        return new UserSessionEntity(
                domain.getId(),
                domain.getUserId(),
                domain.getDeviceId(),
                domain.getEnrollmentToken(),
                domain.getRefreshTokenHash(),
                domain.getRefreshTokenExpiresAt(),
                domain.getIsRevoked(),
                domain.getUpdatedAt()
        );
    }
}
