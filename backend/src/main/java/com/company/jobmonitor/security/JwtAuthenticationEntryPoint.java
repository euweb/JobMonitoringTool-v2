package com.company.jobmonitor.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {

    System.err.println("Unauthorized error: " + authException.getMessage());

    response.setContentType("application/json");
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    String body =
        """
            {
                "status": 401,
                "error": "Unauthorized",
                "message": "Access denied. Please login to access this resource.",
                "path": "%s"
            }
            """
            .formatted(request.getRequestURI());

    response.getWriter().write(body);
  }
}
