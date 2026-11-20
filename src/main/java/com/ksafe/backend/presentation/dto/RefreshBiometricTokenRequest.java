package com.ksafe.backend.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshBiometricTokenRequest(
        @NotBlank(message = "refreshToken is required")
        String refreshToken,

        @NotBlank(message = "deviceId is required")
        String deviceId
) {
}
