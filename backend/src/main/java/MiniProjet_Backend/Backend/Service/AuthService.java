package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.DTO.LoginRequest;
import MiniProjet_Backend.Backend.DTO.LoginResponse;
import MiniProjet_Backend.Backend.DTO.RegisterRequest;
import MiniProjet_Backend.Backend.DTO.AuthResponse;
import MiniProjet_Backend.Backend.Model.User;
import MiniProjet_Backend.Backend.Model.Etudiant;
import MiniProjet_Backend.Backend.Model.Administrateur;
import MiniProjet_Backend.Backend.Model.Professeur;
import MiniProjet_Backend.Backend.Repository.UserRepository;
import MiniProjet_Backend.Backend.Repository.EtudiantRepository;
import MiniProjet_Backend.Backend.Repository.AdministrateurRepository;
import MiniProjet_Backend.Backend.Repository.ProfesseurRepository;
import MiniProjet_Backend.Backend.Security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private ProfesseurRepository professeurRepository;

    @Autowired
    private AdministrateurRepository administrateurRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Login user and return JWT token
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getMotDePasseHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Check if user is active
        if (!user.isActif()) {
            throw new RuntimeException("User account is disabled");
        }

        // Determine user type
        String userType = getUserType(user);

        // Generate JWT token with custom claims
        String token = tokenProvider.generateTokenWithClaims(
                user.getEmail(),
                user.getId(),
                userType
        );

        logger.info("User logged in successfully: {}", user.getEmail());

        return new LoginResponse(token, user.getId(), user.getEmail(), user.getNomComplet(), userType);
    }

    /**
     * Register new user
     */
    public LoginResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Hash password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Create user based on type
        User user = null;
        String userType = request.getUserType().toUpperCase();

        if ("ETUDIANT".equals(userType)) {
            Etudiant etudiant = new Etudiant();
            etudiant.setNomComplet(request.getNomComplet());
            etudiant.setEmail(request.getEmail());
            etudiant.setMotDePasseHash(hashedPassword);
            etudiant.setActif(true);
            etudiant.setMatricule(request.getMatricule());
            etudiant.setNiveau(request.getNiveau());
            user = etudiantRepository.save(etudiant);
        } else if ("PROFESSEUR".equals(userType)) {
            Professeur professeur = new Professeur();
            professeur.setNomComplet(request.getNomComplet());
            professeur.setEmail(request.getEmail());
            professeur.setMotDePasseHash(hashedPassword);
            professeur.setActif(true);
            user = professeurRepository.save(professeur);
        } else if ("ADMINISTRATEUR".equals(userType)) {
            Administrateur admin = new Administrateur();
            admin.setNomComplet(request.getNomComplet());
            admin.setEmail(request.getEmail());
            admin.setMotDePasseHash(hashedPassword);
            admin.setActif(true);
            user = administrateurRepository.save(admin);
        } else {
            throw new RuntimeException("Invalid user type: " + userType);
        }

        // Generate token
        String token = tokenProvider.generateTokenWithClaims(
                user.getEmail(),
                user.getId(),
                userType
        );

        logger.info("User registered successfully: {} ({})", user.getEmail(), userType);

        return new LoginResponse(token, user.getId(), user.getEmail(), user.getNomComplet(), userType);
    }

    /**
     * Get new access token from refresh token
     */
    public AuthResponse refreshAccessToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        String email = tokenProvider.getEmailFromToken(refreshToken);
        Integer userId = tokenProvider.getUserIdFromToken(refreshToken);
        String userType = tokenProvider.getUserTypeFromToken(refreshToken);

        if (email == null || userId == null) {
            throw new RuntimeException("Cannot refresh token - invalid claims");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isActif()) {
            throw new RuntimeException("User account is disabled");
        }

        String newAccessToken = tokenProvider.generateTokenWithClaims(
                email,
                userId,
                userType
        );

        String newRefreshToken = tokenProvider.generateRefreshToken(email, userId);

        AuthResponse.UserInfoDTO userInfo = new AuthResponse.UserInfoDTO(
                userId,
                email,
                user.getNomComplet(),
                convertUserTypeToRole(userType)
        );

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(userInfo)
                .build();
    }

    /**
     * Get current user info from token
     */
    public AuthResponse.UserInfoDTO getCurrentUser(String token) {
        if (!tokenProvider.validateToken(token)) {
            throw new RuntimeException("Invalid or expired token");
        }

        String email = tokenProvider.getEmailFromToken(token);
        Integer userId = tokenProvider.getUserIdFromToken(token);
        String userType = tokenProvider.getUserTypeFromToken(token);

        if (email == null || userId == null) {
            throw new RuntimeException("Cannot get user - invalid token claims");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthResponse.UserInfoDTO(
                userId,
                email,
                user.getNomComplet(),
                convertUserTypeToRole(userType)
        );
    }

    /**
     * Get new AuthResponse with both tokens
     */
    public AuthResponse getAuthResponse(User user, String userType) {
        String token = tokenProvider.generateTokenWithClaims(
                user.getEmail(),
                user.getId(),
                userType
        );

        String refreshToken = tokenProvider.generateRefreshToken(
                user.getEmail(),
                user.getId()
        );

        AuthResponse.UserInfoDTO userInfo = new AuthResponse.UserInfoDTO(
                user.getId(),
                user.getEmail(),
                user.getNomComplet(),
                convertUserTypeToRole(userType)
        );

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(userInfo)
                .build();
    }

    /**
     * Determine user type based on instance
     */
    private String getUserType(User user) {
        if (user instanceof Etudiant) {
            return "ETUDIANT";
        } else if (user instanceof Professeur) {
            return "PROFESSEUR";
        } else if (user instanceof Administrateur) {
            return "ADMINISTRATEUR";
        }
        return "USER";
    }

    /**
     * Convert backend user type to frontend role format
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
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        return tokenProvider.validateToken(token);
    }

    /**
     * Get email from token
     */
    public String getEmailFromToken(String token) {
        return tokenProvider.getEmailFromToken(token);
    }

    /**
     * Generate refresh token for a user ID
     */
    public String generateRefreshToken(Integer userId) {
        // This is a backup method for controller level generation
        // Normally tokens are generated with user context
        // For now, returning token provider method
        return tokenProvider.generateRefreshToken("user@example.com", userId);
    }
}

