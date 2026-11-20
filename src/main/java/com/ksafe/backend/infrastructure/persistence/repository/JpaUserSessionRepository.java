package com.ksafe.backend.infrastructure.persistence.repository;

import com.ksafe.backend.infrastructure.persistence.entity.UserSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaUserSessionRepository extends JpaRepository<UserSessionEntity, Long> {

    Optional<UserSessionEntity> findFirstByDeviceIdOrderByUpdatedAtDesc(String deviceId);

    Optional<UserSessionEntity> findByEnrollmentTokenAndDeviceId(String enrollmentToken, String deviceId);

    List<UserSessionEntity> findAllByDeviceId(String deviceId);
}
