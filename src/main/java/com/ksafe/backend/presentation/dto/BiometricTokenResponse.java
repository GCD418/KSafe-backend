package com.ksafe.backend.presentation.dto;

public record BiometricTokenResponse(
        String accessToken,
        String refreshToken
) {
}
