package com.ksafe.backend.domain.model;

import java.time.LocalDateTime;

public class UserSession {

    private Long id;
    private Long userId;
    private String deviceId;
    private String enrollmentToken;
    private String refreshTokenHash;
    private LocalDateTime refreshTokenExpiresAt;
    private Boolean isRevoked;
    private LocalDateTime updatedAt;

    public UserSession() {
    }

    public UserSession(
            Long id,
            Long userId,
            String deviceId,
            String enrollmentToken,
            String refreshTokenHash,
            LocalDateTime refreshTokenExpiresAt,
            Boolean isRevoked,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.deviceId = deviceId;
        this.enrollmentToken = enrollmentToken;
        this.refreshTokenHash = refreshTokenHash;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
        this.isRevoked = isRevoked;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getEnrollmentToken() {
        return enrollmentToken;
    }

    public void setEnrollmentToken(String enrollmentToken) {
        this.enrollmentToken = enrollmentToken;
    }

    public String getRefreshTokenHash() {
        return refreshTokenHash;
    }

    public void setRefreshTokenHash(String refreshTokenHash) {
        this.refreshTokenHash = refreshTokenHash;
    }

    public LocalDateTime getRefreshTokenExpiresAt() {
        return refreshTokenExpiresAt;
    }

    public void setRefreshTokenExpiresAt(LocalDateTime refreshTokenExpiresAt) {
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    }

    public Boolean getIsRevoked() {
        return isRevoked;
    }

    public void setIsRevoked(Boolean isRevoked) {
        this.isRevoked = isRevoked;
    }

    public boolean isRevoked() {
        return Boolean.TRUE.equals(this.isRevoked);
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
