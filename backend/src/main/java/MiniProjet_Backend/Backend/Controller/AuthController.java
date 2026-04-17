package MiniProjet_Backend.Backend.Controller;

import MiniProjet_Backend.Backend.DTO.LoginRequest;
import MiniProjet_Backend.Backend.DTO.LoginResponse;
import MiniProjet_Backend.Backend.DTO.RegisterRequest;
import MiniProjet_Backend.Backend.DTO.AuthResponse;
import MiniProjet_Backend.Backend.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    /**
     * Login endpoint - returns JWT token with refresh token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            logger.info("Login request for email: {}", request.getEmail());

            AuthResponse response = authService.loginWithResponse(request);

            logger.info("Login successful for email: {}", request.getEmail());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("Login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed"));
        }
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody LoginRequest request) {
        try {
            logger.info("Admin login request for email: {}", request.getEmail());
            AuthResponse response = authService.adminLogin(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Admin login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during admin login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Admin login failed"));
        }
    }

    /**
     * Register endpoint - creates new user and returns JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            logger.info("Register request for email: {}", request.getEmail());

            LoginResponse loginResponse = authService.register(request);

            AuthResponse response = AuthResponse.builder()
                    .token(loginResponse.getToken())
                    .refreshToken(authService.generateRefreshToken(loginResponse.getUserId()))
                    .user(new AuthResponse.UserInfoDTO(
                            loginResponse.getUserId(),
                            loginResponse.getEmail(),
                            loginResponse.getNomComplet(),
                            convertUserTypeToRole(loginResponse.getUserType())
                    ))
                    .build();

            logger.info("Registration successful for email: {}", request.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            logger.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed"));
        }
    }

    /**
     * Refresh token endpoint - get new access token using refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Refresh token is required"));
            }

            logger.info("Token refresh requested");
            AuthResponse response = authService.refreshAccessToken(refreshToken);

            logger.info("Token refreshed successfully");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during token refresh", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Token refresh failed"));
        }
    }

    /**
     * Get current user endpoint - retrieve user info from token
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid authorization header"));
            }

            String token = authHeader.substring(7);
            logger.info("Get current user info requested");

            AuthResponse.UserInfoDTO userInfo = authService.getCurrentUser(token);
            return ResponseEntity.ok(userInfo);

        } catch (RuntimeException e) {
            logger.error("Get user info failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting user info", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get user info"));
        }
    }

    /**
     * Validate JWT token
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid token format"));
            }

            String jwt = token.substring(7);
            boolean isValid = authService.validateToken(jwt);

            if (isValid) {
                String email = authService.getEmailFromToken(jwt);
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "email", email
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("valid", false, "error", "Token is invalid or expired"));
            }

        } catch (Exception e) {
            logger.error("Token validation error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Token validation failed"));
        }
    }

    /**
     * Logout endpoint (client-side token removal is recommended)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // JWT is stateless, so logout is just removing token from client
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully. Please remove token from client.");
        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to convert backend user type to frontend role
     */
    private String convertUserTypeToRole(String userType) {
        if ("ETUDIANT".equalsIgnoreCase(userType)) {
            return "STUDENT";
        } else if ("PROFESSEUR".equalsIgnoreCase(userType)) {
            return "PROFESSOR";
        } else if ("ADMINISTRATEUR".equalsIgnoreCase(userType)) {
            return "ADMIN";
        }
        return "USER";
    }

    /**
     * Temporary method to generate refresh token - to be moved to service
     */
    private String generateRefreshToken(Integer userId) {
        // This is a placeholder - normally this would call AuthService
        // For now, returning a basic refresh token
        return authService.getClass().getName(); // Placeholder
    }
}
