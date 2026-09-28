package com.college.digitalparcel.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/*
 * WHAT IS JWT?
 * JWT (JSON Web Token) is a way to securely pass information between client and server.
 *
 * After a user logs in, we create a JWT token and send it to them.
 * The token looks like this: xxxxx.yyyyy.zzzzz (three parts separated by dots)
 *   Part 1 (header)  : algorithm used to sign the token
 *   Part 2 (payload) : the actual data — username, role, expiry time
 *   Part 3 (signature): proves the token was created by us and not tampered with
 *
 * On every request after login, the user sends this token.
 * We verify the token and know who the user is — no need to check the database every time.
 *
 * WHY JWT INSTEAD OF SESSIONS?
 * Sessions store data on the server. JWT stores data in the token itself.
 * JWT is stateless — the server doesn't need to remember anything between requests.
 *
 * @Component tells Spring: "create one instance of this class and manage it"
 */
@Component
public class JwtUtil {

    // @Value reads the value from application.properties
    // app.jwt.secret is our secret key used to sign tokens
    @Value("${app.jwt.secret}")
    private String secret;

    // app.jwt.expiration is how long the token is valid (in milliseconds)
    // 86400000 ms = 24 hours
    @Value("${app.jwt.expiration}")
    private long expiration;

    // Convert the secret string into a cryptographic key object
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /*
     * GENERATE A JWT TOKEN for a logged-in user.
     *
     * The token contains:
     *   subject   = username (who this token belongs to)
     *   role      = the user's role (ROLE_ADMIN, ROLE_SECURITY, ROLE_STUDENT)
     *   issuedAt  = when the token was created
     *   expiration= when the token expires (24 hours from now)
     *
     * The token is signed with our secret key.
     * If anyone changes the token data, the signature becomes invalid.
     */
    public String generateToken(UserDetails userDetails) {
        // Get the user's role from their authorities list
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        return Jwts.builder()
                .subject(userDetails.getUsername())          // who this token is for
                .claim("role", role)                         // store the role inside the token
                .issuedAt(new Date())                        // current time
                .expiration(new Date(System.currentTimeMillis() + expiration)) // expiry time
                .signWith(getSigningKey())                   // sign with our secret key
                .compact();                                  // build the final token string
    }

    // Extract the username from a token
    // We use this to know which user is making the request
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /*
     * Check if a token is valid:
     * 1. Does the username in the token match the user we loaded from the database?
     * 2. Has the token expired?
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String usernameInToken = extractUsername(token);
        boolean usernameMatches = usernameInToken.equals(userDetails.getUsername());
        boolean tokenNotExpired = !isTokenExpired(token);
        return usernameMatches && tokenNotExpired;
    }

    // Check if the token's expiry date is before the current time
    private boolean isTokenExpired(String token) {
        Date expiryDate = extractAllClaims(token).getExpiration();
        return expiryDate.before(new Date());
    }

    // Parse the token and extract all the data (claims) stored inside it
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // verify the signature using our secret key
                .build()
                .parseSignedClaims(token)
                .getPayload();              // get the data part of the token
    }
}
