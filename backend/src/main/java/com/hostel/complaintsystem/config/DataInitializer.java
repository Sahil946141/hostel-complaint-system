package com.hostel.complaintsystem.config;

import com.hostel.complaintsystem.model.Complaint;
import com.hostel.complaintsystem.model.User;
import com.hostel.complaintsystem.repository.ComplaintRepository;
import com.hostel.complaintsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds default test accounts on startup so that Selenium integration tests
 * (Task 9) always have known credentials available.
 *
 * Seeded users:
 *   student@hostel.com / student123  (STUDENT, room A-101)
 *   admin@hostel.com   / admin123    (ADMIN)
 *   staff@hostel.com   / staff123    (STAFF)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ComplaintRepository complaintRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        User student = createUser("Test Student", "student@hostel.com", "student123",
                User.Role.STUDENT, "A-101");
        createUser("Admin User", "admin@hostel.com", "admin123",
                User.Role.ADMIN, null);
        User staff = createUser("Maintenance Staff", "staff@hostel.com", "staff123",
                User.Role.STAFF, null);

        // Seed a sample complaint so the admin dashboard always has data
        if (student != null) {
            seedComplaint(student, staff);
        }

        log.info("Test data initialization complete");
    }

    private User createUser(String name, String email, String rawPassword,
                            User.Role role, String roomNumber) {
        if (userRepository.existsByEmail(email)) {
            log.debug("User already exists, skipping: {}", email);
            return userRepository.findByEmail(email).orElse(null);
        }
        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .roomNumber(roomNumber)
                .build();
        userRepository.save(user);
        log.info("Seeded {} user: {}", role, email);
        return user;
    }

    private void seedComplaint(User student, User staff) {
        if (!complaintRepository.findAll().isEmpty()) {
            log.debug("Complaints already exist, skipping seed");
            return;
        }
        Complaint complaint = Complaint.builder()
                .title("Leaking tap in washroom")
                .description("The washroom tap on the first floor has been leaking for two days, wasting water.")
                .category(Complaint.Category.PLUMBING)
                .priority(Complaint.Priority.HIGH)
                .student(student)
                .build();
        complaintRepository.save(complaint);
        log.info("Seeded sample complaint for admin dashboard");
    }
}
