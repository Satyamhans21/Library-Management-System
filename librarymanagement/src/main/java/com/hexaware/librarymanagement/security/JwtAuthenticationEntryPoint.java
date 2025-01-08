package com.hexaware.librarymanagement.security;

import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JwtAuthenticationEntryPoint handles unauthorized access attempts.
 * This class is invoked whenever an unauthenticated user tries to access a resource
 * that requires authentication.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * This method is triggered when an unauthenticated user tries to access
     * a secured resource. It sends a 401 Unauthorized response to the client.
     *
     * @param request       The HTTP request object.
     * @param response      The HTTP response object.
     * @param authException The exception thrown due to authentication failure.
     * @throws IOException      If an input or output error occurs while writing the response.
     * @throws ServletException If a servlet-specific error occurs.
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // Set the HTTP response status to 401 (Unauthorized).
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Specify that the response content type is JSON.
        response.setContentType("application/json");

        // Write a JSON response with the error message from the exception.
        response.getWriter().write("{\"message\": \"Authentication failed: " + authException.getMessage() + "\"}");
    }
}
