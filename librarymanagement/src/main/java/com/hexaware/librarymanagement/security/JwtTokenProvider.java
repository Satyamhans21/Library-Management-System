package com.hexaware.librarymanagement.security;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.hexaware.librarymanagement.exception.BadRequestException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * JwtTokenProvider is responsible for generating, parsing, and validating JWT tokens.
 */
@Component
public class JwtTokenProvider {

    // Secret key for signing the JWT token, injected from application properties
    @Value("${app.jwt-secret}")
    private String jwtSecret;

    // JWT token expiration time in milliseconds, injected from application properties
    @Value("${app-jwt-expiration-milliseconds}")
    private int jwtExpirationDate;

    /**
     * Generates a JWT token for the authenticated user.
     *
     * @param authentication The authentication object containing user details.
     * @return A signed JWT token as a string.
     */
    public String generateToken(Authentication authentication) {
        // Get the username (subject) from the authentication object
        String username = authentication.getName();

        // Set the current date and calculate the token's expiration date
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        // Extract roles/authorities from the authentication object
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")); // Concatenate roles with a comma

        // Build the JWT token with claims, subject, expiration, and signing key
        String jwtToken = Jwts.builder()
                .setSubject(username) // Set the subject (username)
                .claim("role", roles) // Add a custom claim for roles
                .setIssuedAt(currentDate) // Set token issue date
                .setExpiration(expireDate) // Set token expiration date
                .signWith(key()) // Sign the token with the secret key
                .compact();

        return jwtToken; // Return the generated token
    }

    /**
     * Creates a Key object from the secret key for signing and validating JWT tokens.
     *
     * @return A Key object generated from the secret key.
     */
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Extracts the username from the JWT token.
     *
     * @param token The JWT token.
     * @return The username (subject) stored in the token.
     */
    public String getUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key()) // Use the secret key to parse the token
                .build()
                .parseClaimsJws(token)
                .getBody(); // Extract claims from the token

        return claims.getSubject(); // Return the subject (username)
    }

    /**
     * Validates the provided JWT token.
     *
     * @param token The JWT token to validate.
     * @return True if the token is valid, false otherwise.
     * @throws BadRequestException If the token is invalid, expired, or unsupported.
     */
    public boolean validateToken(String token) {
        try {
            // Parse the token to ensure it's valid
            Jwts.parserBuilder()
                    .setSigningKey(key()) // Use the secret key to validate the token
                    .build()
                    .parse(token);
            return true; // Return true if validation succeeds
        } catch (MalformedJwtException e) {
            // Handle invalid JWT token format
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "Invalid JWT Token");
        } catch (ExpiredJwtException e) {
            // Handle expired token
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "Expired JWT Token");
        } catch (UnsupportedJwtException e) {
            // Handle unsupported token
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "Unsupported JWT Token");
        } catch (IllegalArgumentException e) {
            // Handle empty or null claims
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "JWT claims string is empty");
        }
    }
}
