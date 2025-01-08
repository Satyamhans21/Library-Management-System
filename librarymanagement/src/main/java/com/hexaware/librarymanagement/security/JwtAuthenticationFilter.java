package com.hexaware.librarymanagement.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JwtAuthenticationFilter intercepts incoming HTTP requests to validate
 * JWT tokens and authenticate users based on the token's content.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private JwtTokenProvider jwtTokenProvider;
    private UserDetailsService customUserDetails;

    /**
     * Constructor for dependency injection.
     *
     * @param jwtTokenProvider  Utility class for JWT token operations.
     * @param customUserDetails Service to load user details from the database.
     */
    @Autowired
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService customUserDetails) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetails = customUserDetails;
    }

    /**
     * Processes each request, validates the JWT token, and sets authentication
     * in the security context if the token is valid.
     *
     * @param request     The HTTP request object.
     * @param response    The HTTP response object.
     * @param filterChain The filter chain to pass the request to the next filter.
     * @throws ServletException If a servlet-specific error occurs.
     * @throws IOException      If an input or output error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip authentication for login or registration paths
        String path = request.getRequestURI();
        if (path.startsWith("/api/authenticate/")) {
            filterChain.doFilter(request, response); // Allow login/register requests to proceed
            return; // Skip further processing for authentication endpoints
        }

        // Get JWT token from the request header
        String token = getTokenFromRequest(request);

        // Validate the JWT token
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // Extract username from the token
            String username = jwtTokenProvider.getUsername(token);

            // Load user details using the username
            UserDetails userDetails = customUserDetails.loadUserByUsername(username);

            // Create an authentication token based on the user details
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            // Set additional authentication details based on the request
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Set the authentication in the SecurityContextHolder for this request
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        } else {
            // If the token is invalid or expired, respond with a 401 Unauthorized error
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        // Continue with the next filter in the chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the Authorization header of the HTTP request.
     *
     * @param request The HTTP request object.
     * @return The JWT token, or null if no token is present.
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // Get the Authorization header value
        String bearerToken = request.getHeader("Authorization");

        // Check if the header contains a Bearer token
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Return the token portion of the Bearer header
            return bearerToken.substring(7); // Skip "Bearer " to extract the token
        }

        // Return null if no valid token is found
        return null;
    }
}
