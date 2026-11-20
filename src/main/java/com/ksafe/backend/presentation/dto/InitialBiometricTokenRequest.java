package com.ksafe.backend.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record InitialBiometricTokenRequest(
        @NotBlank(message = "enrollmentToken is required")
        String enrollmentToken,

        @NotBlank(message = "deviceId is required")
        String deviceId
) {
}
