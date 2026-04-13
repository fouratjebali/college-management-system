package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.DTO.LoginRequest;
import MiniProjet_Backend.Backend.DTO.LoginResponse;
import MiniProjet_Backend.Backend.DTO.RegisterRequest;
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
}

