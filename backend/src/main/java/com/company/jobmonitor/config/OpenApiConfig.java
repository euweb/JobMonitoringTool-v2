package com.company.jobmonitor.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for the JobMonitor API.
 *
 * <p>This configuration provides comprehensive API documentation including: - API metadata and
 * versioning information - JWT Bearer token security scheme - Server definitions for different
 * environments - Contact and license information
 *
 * <p>The generated documentation is available at: - Swagger UI:
 * http://localhost:8080/swagger-ui/index.html - OpenAPI JSON: http://localhost:8080/v3/api-docs -
 * OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml
 *
 * @author JobMonitor Team
 * @version 1.0
 * @since 1.0
 */
@Configuration
@OpenAPIDefinition(
    info =
        @Info(
            title = "JobMonitor API",
            version = "1.0.0",
            description =
                """
                # JobMonitor API Documentation

                A comprehensive REST API for managing DWH job monitoring and user administration.

                ## Features
                - **JWT-based Authentication** - Secure token-based authentication
                - **Role-based Access Control** - ADMIN and USER roles with different permissions
                - **User Management** - Complete CRUD operations for user accounts
                - **Self-service Operations** - Users can manage their own profiles
                - **Admin Panel** - Administrative functions for user and system management
                - **Health Monitoring** - System health and metrics endpoints

                ## Authentication
                Most endpoints require authentication using a JWT Bearer token. Obtain a token by:
                1. POST to `/api/auth/login` with valid credentials
                2. Include the returned token in subsequent requests: `Authorization: Bearer <token>`

                ## User Roles
                - **ADMIN**: Full access to all endpoints including user management
                - **USER**: Access to own profile and standard application features
                """,
            contact =
                @Contact(
                    name = "JobMonitor Development Team",
                    email = "dev@jobmonitor.com",
                    url = "https://github.com/euweb/JobMonitoringTool-v2"),
            license = @License(name = "MIT License", url = "https://opensource.org/licenses/MIT")),
    servers = {
      @Server(url = "http://localhost:8080", description = "Development Server"),
      @Server(url = "https://jobmonitor.company.com", description = "Production Server")
    })
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description =
        """
        JWT Bearer token authentication.

        To authenticate:
        1. Login via POST /api/auth/login
        2. Use the returned JWT token in the Authorization header
        3. Format: Authorization: Bearer <your-jwt-token>

        Token expires after 1 hour by default.
        """)
public class OpenApiConfig {}
