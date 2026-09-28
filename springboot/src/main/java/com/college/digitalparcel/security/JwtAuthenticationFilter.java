package com.college.digitalparcel.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/*
 * WHAT IS A FILTER?
 * A filter intercepts every HTTP request BEFORE it reaches the controller.
 * Think of it like a security checkpoint at the entrance of a building.
 * Every person (request) must pass through the checkpoint first.
 *
 * WHAT DOES THIS FILTER DO?
 * It checks if the incoming request has a valid JWT token.
 * If yes → it tells Spring Security "this user is authenticated"
 * If no  → it lets the request continue without authentication
 *          (Spring Security will then block it if the endpoint requires login)
 *
 * OncePerRequestFilter ensures this filter runs exactly ONCE per request.
 *
 * HOW JWT AUTHENTICATION WORKS:
 * 1. User logs in → gets a JWT token
 * 2. User sends a request with the token in the header:
 *      Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
 * 3. This filter reads the token from the header
 * 4. Validates the token
 * 5. If valid → sets the user as authenticated in Spring Security
 * 6. Request proceeds to the controller
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Step 1: Read the Authorization header from the request
        // It should look like: "Bearer eyJhbGciOiJIUzUxMiJ9..."
        String authHeader = request.getHeader("Authorization");

        // Step 2: If there is no Authorization header, or it doesn't start with "Bearer ",
        // skip JWT processing and pass the request to the next filter
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Extract the token by removing the "Bearer " prefix (7 characters)
        String token = authHeader.substring(7);

        // Step 4: Extract the username from the token
        String username = jwtUtil.extractUsername(token);

        // Step 5: If we got a username AND the user is not already authenticated
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load the user's details from the database
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Step 6: Check if the token is valid (correct user + not expired)
            if (jwtUtil.isTokenValid(token, userDetails)) {

                // Step 7: Create an authentication object with the user's details and roles
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                          // no credentials needed (we already verified the token)
                                userDetails.getAuthorities()   // the user's roles (ROLE_ADMIN, etc.)
                        );

                // Add extra request details (like IP address) to the authentication
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Step 8: Store the authentication in the SecurityContext
                // This tells Spring Security: "this request is authenticated as this user"
                // Now Spring Security knows who is making the request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Step 9: Pass the request to the next filter or to the controller
        filterChain.doFilter(request, response);
    }
}
