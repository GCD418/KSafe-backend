package com.ksafe.backend.domain.model;

public record TokenPair(
        String accessToken,
        String refreshToken
) {
}
