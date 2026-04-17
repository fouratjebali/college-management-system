package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.DTO.AnnonceRequestDTO;
import MiniProjet_Backend.Backend.DTO.AnnonceResponseDTO;
import MiniProjet_Backend.Backend.Model.Administrateur;
import MiniProjet_Backend.Backend.Model.Annonce;
import MiniProjet_Backend.Backend.Repository.AdministrateurRepository;
import MiniProjet_Backend.Backend.Repository.AnnonceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AnnonceService {
    @Autowired
    private AnnonceRepository annonceRepository;

    @Autowired
    private AdministrateurRepository administrateurRepository;

    public List<Annonce> getAllAnnonces() {
        return annonceRepository.findAll();
    }

    public Page<AnnonceResponseDTO> getAllAnnonces(Pageable pageable) {
        return annonceRepository.findAll(pageable).map(this::toResponse);
    }

    public Optional<Annonce> getAnnonceById(Integer id) {
        return annonceRepository.findById(id);
    }

    public Optional<AnnonceResponseDTO> getAnnonceResponseById(Integer id) {
        return annonceRepository.findById(id).map(this::toResponse);
    }

    public List<Annonce> getAnnoncesByAdministrateur(Integer administrateurId) {
        return annonceRepository.findByAdministrateurId(administrateurId);
    }

    public Page<AnnonceResponseDTO> getAnnoncesByAdministrateur(Integer administrateurId, Pageable pageable) {
        return annonceRepository.findByAdministrateurId(administrateurId, pageable).map(this::toResponse);
    }

    public List<Annonce> getGlobalAnnonces() {
        return annonceRepository.findByCibleGlobaleTrue();
    }

    public Page<AnnonceResponseDTO> getGlobalAnnonces(Pageable pageable) {
        return annonceRepository.findByCibleGlobaleTrue(pageable).map(this::toResponse);
    }

    public Page<AnnonceResponseDTO> getActiveAnnonces(Pageable pageable) {
        return annonceRepository.findActive(LocalDateTime.now(), pageable).map(this::toResponse);
    }

    public Page<AnnonceResponseDTO> getVisibleAnnonces(String role, boolean activeOnly, Pageable pageable) {
        return annonceRepository.findVisibleForRole(role, LocalDateTime.now(), activeOnly, pageable)
                .map(this::toResponse);
    }

    public Annonce saveAnnonce(Annonce annonce) {
        validateAnnonceDates(annonce.getDatePublication(), annonce.getDateExpiration());
        return annonceRepository.save(annonce);
    }

    public AnnonceResponseDTO createAnnonce(AnnonceRequestDTO request) {
        Annonce annonce = new Annonce();
        applyRequest(annonce, request);
        return toResponse(annonceRepository.save(annonce));
    }

    public AnnonceResponseDTO updateAnnonce(Integer id, AnnonceRequestDTO request) {
        return annonceRepository.findById(id).map(annonce -> {
            applyRequest(annonce, request);
            return toResponse(annonceRepository.save(annonce));
        }).orElseThrow(() -> new RuntimeException("Annonce not found"));
    }

    public Annonce updateAnnonce(Integer id, Annonce annonceDetails) {
        return annonceRepository.findById(id).map(annonce -> {
            validateAnnonceDates(annonceDetails.getDatePublication(), annonceDetails.getDateExpiration());
            annonce.setTitre(annonceDetails.getTitre());
            annonce.setContenu(annonceDetails.getContenu());
            annonce.setDatePublication(annonceDetails.getDatePublication());
            annonce.setDateExpiration(annonceDetails.getDateExpiration());
            annonce.setCibleGlobale(annonceDetails.getCibleGlobale());
            annonce.setCibleRole(annonceDetails.getCibleRole());
            annonce.setAdministrateur(annonceDetails.getAdministrateur());
            return annonceRepository.save(annonce);
        }).orElseThrow(() -> new RuntimeException("Annonce not found"));
    }

    public void deleteAnnonce(Integer id) {
        annonceRepository.deleteById(id);
    }

    private void applyRequest(Annonce annonce, AnnonceRequestDTO request) {
        validateAnnonceDates(request.getDatePublication(), request.getDateExpiration());

        Boolean cibleGlobale = request.getCibleGlobale() == null ? Boolean.TRUE : request.getCibleGlobale();
        String cibleRole = normalizeRole(request.getCibleRole());
        if (!cibleGlobale && cibleRole == null) {
            throw new IllegalArgumentException("cibleRole est obligatoire lorsque cibleGlobale=false");
        }

        Administrateur administrateur = administrateurRepository.findById(request.getAdministrateurId())
                .orElseThrow(() -> new RuntimeException("Administrateur not found"));

        annonce.setTitre(request.getTitre());
        annonce.setContenu(request.getContenu());
        annonce.setDatePublication(request.getDatePublication() == null ? LocalDateTime.now() : request.getDatePublication());
        annonce.setDateExpiration(request.getDateExpiration());
        annonce.setCibleGlobale(cibleGlobale);
        annonce.setCibleRole(cibleGlobale ? null : cibleRole);
        annonce.setAdministrateur(administrateur);
    }

    private void validateAnnonceDates(LocalDateTime datePublication, LocalDateTime dateExpiration) {
        if (dateExpiration == null) {
            throw new IllegalArgumentException("dateExpiration est obligatoire");
        }
        LocalDateTime publication = datePublication == null ? LocalDateTime.now() : datePublication;
        if (!dateExpiration.isAfter(publication)) {
            throw new IllegalArgumentException("dateExpiration doit être après datePublication");
        }
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        String normalized = role.trim().toUpperCase();
        if (!List.of("ADMIN", "ADMINISTRATEUR", "PROFESSOR", "PROFESSEUR", "STUDENT", "ETUDIANT").contains(normalized)) {
            throw new IllegalArgumentException("Rôle cible invalide: " + role);
        }
        return normalized;
    }

    private AnnonceResponseDTO toResponse(Annonce annonce) {
        AnnonceResponseDTO response = new AnnonceResponseDTO();
        response.setId(annonce.getId());
        response.setTitre(annonce.getTitre());
        response.setContenu(annonce.getContenu());
        response.setDatePublication(annonce.getDatePublication());
        response.setDateExpiration(annonce.getDateExpiration());
        response.setCibleGlobale(annonce.getCibleGlobale());
        response.setCibleRole(annonce.getCibleRole());
        if (annonce.getAdministrateur() != null) {
            response.setAdministrateurId(annonce.getAdministrateur().getId());
            response.setAdministrateurNom(annonce.getAdministrateur().getNomComplet());
        }
        LocalDateTime now = LocalDateTime.now();
        response.setActive(
                annonce.getDatePublication() != null &&
                        annonce.getDateExpiration() != null &&
                        !annonce.getDatePublication().isAfter(now) &&
                        !annonce.getDateExpiration().isBefore(now)
        );
        return response;
    }
}

