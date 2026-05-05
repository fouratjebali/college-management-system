package MiniProjet_Backend.Backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication response DTO matching frontend expectations
 * Provides both access token and refresh token along with user info
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;              // Access token (JWT)
    private String refreshToken;       // Refresh token for getting new access tokens
    private UserInfoDTO user;          // User information

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoDTO {
        private Integer id;           // User ID
        private String email;         // User email
        private String nomComplet;    // Full name
        private String role;          // Role: STUDENT, PROFESSOR, ADMIN
    }
}
