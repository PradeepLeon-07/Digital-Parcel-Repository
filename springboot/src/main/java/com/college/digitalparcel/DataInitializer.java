package com.college.digitalparcel;

import com.college.digitalparcel.entity.Role;
import com.college.digitalparcel.entity.User;
import com.college.digitalparcel.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Default admin created — username: admin, password: admin123");
        }

        if (!userRepository.existsByUsername("security1")) {
            User security = new User();
            security.setUsername("security1");
            security.setPassword(passwordEncoder.encode("security123"));
            security.setRole(Role.SECURITY);
            userRepository.save(security);
            System.out.println("Default security user created — username: security1, password: security123");
        }
    }
}
