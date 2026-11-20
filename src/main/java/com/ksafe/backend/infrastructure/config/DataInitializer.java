package com.ksafe.backend.infrastructure.config;

import com.ksafe.backend.domain.model.User;
import com.ksafe.backend.domain.port.PasswordHasherPort;
import com.ksafe.backend.domain.port.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String TEST_USER_EMAIL = "test@example.com";
    private static final String TEST_USER_PASSWORD = "password123";

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;

    public DataInitializer(UserRepositoryPort userRepository, PasswordHasherPort passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail(TEST_USER_EMAIL)) {
            User testUser = new User(
                    null,
                    TEST_USER_EMAIL,
                    passwordHasher.encode(TEST_USER_PASSWORD),
                    LocalDateTime.now()
            );
            userRepository.save(testUser);
            log.info("Initialized default test user: {}", TEST_USER_EMAIL);
        } else {
            log.info("Default test user already exists: {}", TEST_USER_EMAIL);
        }
    }
}
