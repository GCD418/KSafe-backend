package com.ksafe.backend.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api")
public class ProtectedResourceController {

    private static final List<String> FUN_MESSAGES = List.of(
            "Bóveda abierta: el código secreto del café hoy es ESPRESSO-404.",
            "Acceso VIP concedido: tus tokens están más seguros que la receta secreta de la abuela.",
            "¡Boom! JWT verificado. Dato curioso: los pulpos tienen tres corazones y cero refresh tokens.",
            "Misión cumplida, agente. El servidor aprobó tu huella/rostro sin despeinarse.",
            "Bóveda desbloqueada: nivel de seguridad = Ninja Biométrico.",
            "Acceso autorizado: si lees esto, tu arquitectura Clean + KSafe + JWT está en modo leyenda."
    );

    private final AtomicLong requestCounter = new AtomicLong(0);

    @GetMapping("/protected-resource")
    public ResponseEntity<Map<String, Object>> getProtectedResource(
            Authentication authentication,
            @RequestHeader(value = "X-Simulate-401", required = false) String simulate401
    ) {
        if ("true".equalsIgnoreCase(simulate401)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of(
                            "error", "Unauthorized",
                            "message", "401 simulado por el backend (X-Simulate-401: true). La app debe bloquearse."
                    )
            );
        }

        long visitNumber = requestCounter.incrementAndGet();
        String randomMessage = FUN_MESSAGES.get(ThreadLocalRandom.current().nextInt(FUN_MESSAGES.size()));
        String userId = authentication != null ? authentication.getName() : "unknown";

        return ResponseEntity.ok(
                Map.of(
                        "status", "UNLOCKED_ACCESS_GRANTED",
                        "visitNumber", visitNumber,
                        "userId", userId,
                        "secretMessage", randomMessage,
                        "serverTimestamp", Instant.now().toString()
                )
        );
    }
}
