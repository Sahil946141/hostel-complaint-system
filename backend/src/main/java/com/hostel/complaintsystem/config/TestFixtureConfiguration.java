package com.hostel.complaintsystem.config;

import com.hostel.complaintsystem.model.User;
import com.hostel.complaintsystem.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class TestFixtureConfiguration {

    @Bean
    @ConditionalOnProperty(name = "app.test-fixtures.enabled", havingValue = "true")
    CommandLineRunner createTestUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            createUserIfMissing(userRepository, passwordEncoder, "Test Student", "student@hostel.com",
                    "student123", User.Role.STUDENT, "101");
            createUserIfMissing(userRepository, passwordEncoder, "Test Admin", "admin@hostel.com",
                    "admin123", User.Role.ADMIN, null);
        };
    }

    private void createUserIfMissing(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String name,
            String email,
            String password,
            User.Role role,
            String roomNumber) {
        if (!userRepository.existsByEmail(email)) {
            userRepository.save(User.builder()
                    .name(name)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .role(role)
                    .roomNumber(roomNumber)
                    .build());
        }
    }
}
