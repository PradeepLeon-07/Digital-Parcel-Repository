package com.college.digitalparcel.service;

import com.college.digitalparcel.dto.LoginRequest;
import com.college.digitalparcel.dto.LoginResponse;
import com.college.digitalparcel.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/*
 * WHAT DOES THIS SERVICE DO?
 * It handles the login process.
 * When a user sends their username and password, this service:
 *   1. Verifies the credentials are correct
 *   2. Generates a JWT token
 *   3. Returns the token to the user
 *
 * After login, the user includes this token in every request
 * so the server knows who they are without asking for password again.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public AuthService(AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService,
                       JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {

        /*
         * Step 1: Verify the username and password.
         *
         * authenticationManager.authenticate() does the following internally:
         *   - Calls CustomUserDetailsService.loadUserByUsername() to get the user from DB
         *   - Compares the provided password with the stored BCrypt hash
         *   - If wrong password → throws BadCredentialsException → returns 401 Unauthorized
         *   - If correct → continues to next step
         */
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Step 2: Load the user details again (we need it to generate the token)
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        // Step 3: Generate a JWT token for this user
        String token = jwtUtil.generateToken(userDetails);

        // Step 4: Get the user's role to include in the response
        // getAuthorities() returns the list of roles — we only have one role per user
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        // Step 5: Return the token, username, and role to the client
        return new LoginResponse(token, userDetails.getUsername(), role);
    }
}
