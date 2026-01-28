package com.aeris2.config;

import com.aeris2.model.User;
import com.aeris2.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "aeris@gmail.com";
            if (userRepo.findByEmail(adminEmail).isEmpty()) {
                User admin = User.builder()
                        .name("Super Admin")
                        .email(adminEmail)
                        .password(passwordEncoder.encode("AerisJeba")) // 👈 hardcoded password
                        .role("ADMIN")
                        .build();
                userRepo.save(admin);
                System.out.println("✅ Default admin created: " + adminEmail + " / admin123");
            } else {
                System.out.println("ℹ️ Admin already exists, skipping creation.");
            }
        };
    }
}
