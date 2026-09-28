package com.college.digitalparcel.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/*
 * WHAT IS THIS CLASS?
 * This is the main Spring Security configuration file.
 * It defines:
 *   1. Which URLs are public (no login needed)
 *   2. Which URLs require specific roles
 *   3. How passwords are hashed
 *   4. How authentication works
 *   5. Where our JWT filter fits in
 *
 * @Configuration tells Spring: "this class contains bean definitions"
 * @EnableWebSecurity activates Spring Security for this application
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    /*
     * @Bean tells Spring: "create this object and make it available for injection"
     *
     * SecurityFilterChain defines the security rules for HTTP requests.
     * Every incoming request is checked against these rules.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF protection — not needed for REST APIs that use JWT
            // CSRF is only needed for browser-based form submissions with sessions
            .csrf(AbstractHttpConfigurer::disable)

            // Define which URLs are allowed and which require authentication
            .authorizeHttpRequests(auth -> auth

                // These URLs are PUBLIC — no login needed
                .requestMatchers("/", "/index.html", "/style.css", "/app.js").permitAll()
                .requestMatchers("/api/auth/**").permitAll()  // login endpoint is public

                // These URLs require ADMIN or SECURITY role
                .requestMatchers("/api/students/**").hasAnyRole("ADMIN", "SECURITY")
                .requestMatchers("/api/dashboard/**").hasAnyRole("ADMIN", "SECURITY")

                // Students may view only their own parcel endpoint.
                .requestMatchers(HttpMethod.GET, "/api/parcels/student/**").hasRole("STUDENT")

                // Parcel listing, search, and status endpoints are staff-only.
                .requestMatchers(HttpMethod.GET, "/api/parcels/**").hasAnyRole("ADMIN", "SECURITY")

                // Creating and updating parcels requires ADMIN or SECURITY role
                .requestMatchers(HttpMethod.POST, "/api/parcels/**").hasAnyRole("ADMIN", "SECURITY")
                .requestMatchers(HttpMethod.PUT, "/api/parcels/**").hasAnyRole("ADMIN", "SECURITY")

                // Only ADMIN can delete parcels
                .requestMatchers(HttpMethod.DELETE, "/api/parcels/**").hasRole("ADMIN")

                // All other requests just need to be logged in (any role)
                .anyRequest().authenticated()
            )

            // Use STATELESS sessions — the server does NOT store session data
            // Every request must carry a JWT token to prove who they are
            // This is the correct approach for REST APIs
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Register our authentication provider (tells Spring how to verify passwords)
            .authenticationProvider(authenticationProvider())

            // Add our JWT filter BEFORE Spring's default login filter
            // This way, JWT is checked first on every request
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /*
     * PasswordEncoder defines how passwords are hashed.
     * BCrypt is a strong, slow hashing algorithm — slow by design to prevent brute-force attacks.
     *
     * When a user registers: passwordEncoder.encode("mypassword") → stores the hash
     * When a user logs in:   passwordEncoder.matches("mypassword", storedHash) → true/false
     *
     * NEVER store plain text passwords in the database.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * AuthenticationProvider connects Spring Security to our database.
     * It uses:
     *   - UserDetailsService to load the user from the database
     *   - PasswordEncoder to compare the provided password with the stored hash
     *
     * When login is attempted, this provider does the actual verification.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /*
     * AuthenticationManager is the entry point for authentication.
     * We use it in AuthService to trigger the login process.
     * Spring provides the implementation — we just expose it as a bean.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
