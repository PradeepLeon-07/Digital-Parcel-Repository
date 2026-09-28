package com.college.digitalparcel.service;

import com.college.digitalparcel.entity.User;
import com.college.digitalparcel.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

/*
 * WHAT IS THIS CLASS?
 * Spring Security needs to know how to load a user from the database during login.
 * It does this by calling the loadUserByUsername() method.
 *
 * We implement the UserDetailsService interface and override loadUserByUsername()
 * to tell Spring Security: "look up the user in our MySQL database".
 *
 * HOW IT FITS IN THE LOGIN FLOW:
 * 1. User sends username + password to /api/auth/login
 * 2. Spring Security calls loadUserByUsername(username) — this method runs
 * 3. We find the user in the database
 * 4. Spring Security compares the password with the stored BCrypt hash
 * 5. If correct → login succeeds, JWT is generated
 * 6. If wrong   → login fails, 401 Unauthorized is returned
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    // We need the UserRepository to look up users from the database
    private final UserRepository userRepository;

    // Constructor injection — Spring automatically provides the UserRepository
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /*
     * This method is called by Spring Security during login.
     * It loads the user from the database by username.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Step 1: Find the user in the database
        // If not found, throw UsernameNotFoundException — Spring Security handles this as a 401
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        /*
         * Step 2: Convert our User entity into a Spring Security UserDetails object.
         *
         * Spring Security doesn't know about our User class directly.
         * It works with its own UserDetails interface.
         * So we convert our User into Spring's built-in User class.
         *
         * "ROLE_" + user.getRole().name() converts our enum to a Spring role string:
         *   Role.ADMIN    → "ROLE_ADMIN"
         *   Role.SECURITY → "ROLE_SECURITY"
         *   Role.STUDENT  → "ROLE_STUDENT"
         *
         * Spring Security requires this "ROLE_" prefix for hasRole() checks to work.
         */
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
