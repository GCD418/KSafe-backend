package com.ksafe.backend.domain.port;

import com.ksafe.backend.domain.model.UserSession;

import java.util.Optional;

public interface UserSessionRepositoryPort {

    Optional<UserSession> findByDeviceId(String deviceId);

    Optional<UserSession> findByEnrollmentTokenAndDeviceId(String enrollmentToken, String deviceId);

    UserSession save(UserSession userSession);

    void revokeAllByDeviceId(String deviceId);
}
