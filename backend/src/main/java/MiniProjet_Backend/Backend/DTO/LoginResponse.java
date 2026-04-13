package MiniProjet_Backend.Backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private Integer userId;
    private String email;
    private String nomComplet;
    private String userType;

    public LoginResponse(String token, Integer userId, String email, String nomComplet, String userType) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.nomComplet = nomComplet;
        this.userType = userType;
    }
}

