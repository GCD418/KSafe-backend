package com.ksafe.backend.presentation.controller;

import com.ksafe.backend.application.usecase.InitialBiometricTokenUseCase;
import com.ksafe.backend.application.usecase.LoginUseCase;
import com.ksafe.backend.application.usecase.RefreshBiometricTokenUseCase;
import com.ksafe.backend.domain.model.TokenPair;
import com.ksafe.backend.presentation.dto.BiometricTokenResponse;
import com.ksafe.backend.presentation.dto.InitialBiometricTokenRequest;
import com.ksafe.backend.presentation.dto.LoginRequest;
import com.ksafe.backend.presentation.dto.LoginResponse;
import com.ksafe.backend.presentation.dto.RefreshBiometricTokenRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final InitialBiometricTokenUseCase initialBiometricTokenUseCase;
    private final RefreshBiometricTokenUseCase refreshBiometricTokenUseCase;

    public AuthController(
            LoginUseCase loginUseCase,
            InitialBiometricTokenUseCase initialBiometricTokenUseCase,
            RefreshBiometricTokenUseCase refreshBiometricTokenUseCase
    ) {
        this.loginUseCase = loginUseCase;
        this.initialBiometricTokenUseCase = initialBiometricTokenUseCase;
        this.refreshBiometricTokenUseCase = refreshBiometricTokenUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(value = "X-Device-Id", required = false) String headerDeviceId
    ) {
        String resolvedDeviceId = (request.deviceId() != null && !request.deviceId().isBlank())
                ? request.deviceId()
                : headerDeviceId;

        String enrollmentToken = loginUseCase.execute(
                request.email(),
                request.password(),
                resolvedDeviceId
        );

        return ResponseEntity.ok(new LoginResponse(enrollmentToken));
    }

    @PostMapping("/biometric/initial-token")
    public ResponseEntity<BiometricTokenResponse> initialBiometricToken(
            @Valid @RequestBody InitialBiometricTokenRequest request
    ) {
        TokenPair tokenPair = initialBiometricTokenUseCase.execute(
                request.enrollmentToken(),
                request.deviceId()
        );

        return ResponseEntity.ok(new BiometricTokenResponse(
                tokenPair.accessToken(),
                tokenPair.refreshToken()
        ));
    }

    @PostMapping("/biometric/refresh")
    public ResponseEntity<BiometricTokenResponse> refreshBiometricToken(
            @Valid @RequestBody RefreshBiometricTokenRequest request
    ) {
        TokenPair tokenPair = refreshBiometricTokenUseCase.execute(
                request.refreshToken(),
                request.deviceId()
        );

        return ResponseEntity.ok(new BiometricTokenResponse(
                tokenPair.accessToken(),
                tokenPair.refreshToken()
        ));
    }
}
