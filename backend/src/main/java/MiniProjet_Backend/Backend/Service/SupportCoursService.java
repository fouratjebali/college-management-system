package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.DTO.SupportCoursResponseDTO;
import MiniProjet_Backend.Backend.Model.Enseignement;
import MiniProjet_Backend.Backend.Model.SupportCours;
import MiniProjet_Backend.Backend.Repository.EnseignementRepository;
import MiniProjet_Backend.Backend.Repository.SupportCoursRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SupportCoursService {
    @Autowired
    private SupportCoursRepository supportCoursRepository;

    @Autowired
    private EnseignementRepository enseignementRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public List<SupportCours> getAllSupportCours() {
        return supportCoursRepository.findAll();
    }

    public Page<SupportCoursResponseDTO> getAllSupportCours(Pageable pageable) {
        return supportCoursRepository.findAll(pageable).map(this::toResponse);
    }

    public Optional<SupportCours> getSupportCoursById(Integer id) {
        return supportCoursRepository.findById(id);
    }

    public Optional<SupportCoursResponseDTO> getSupportCoursResponseById(Integer id) {
        return supportCoursRepository.findById(id).map(this::toResponse);
    }

    public List<SupportCours> getSupportCoursByEnseignement(Integer enseignementId) {
        return supportCoursRepository.findByEnseignementId(enseignementId);
    }

    public Page<SupportCoursResponseDTO> getSupportCoursByEnseignement(Integer enseignementId, Pageable pageable) {
        return supportCoursRepository.findByEnseignementId(enseignementId, pageable).map(this::toResponse);
    }

    public SupportCours saveSupportCours(SupportCours supportCours) {
        if (supportCours.getDateDepot() == null) {
            supportCours.setDateDepot(LocalDateTime.now());
        }
        if (supportCours.getNomFichierOriginal() == null) {
            supportCours.setNomFichierOriginal(supportCours.getCheminFichier());
        }
        if (supportCours.getTypeFichier() == null) {
            supportCours.setTypeFichier("application/octet-stream");
        }
        if (supportCours.getTailleOctets() == null) {
            supportCours.setTailleOctets(0L);
        }
        return supportCoursRepository.save(supportCours);
    }

    public SupportCoursResponseDTO uploadSupportCours(String titre, Integer enseignementId, MultipartFile file) {
        if (titre == null || titre.isBlank()) {
            throw new IllegalArgumentException("Le titre est obligatoire");
        }

        Enseignement enseignement = enseignementRepository.findById(enseignementId)
                .orElseThrow(() -> new RuntimeException("Enseignement not found"));
        FileStorageService.StoredFile storedFile = fileStorageService.store(file);

        SupportCours supportCours = new SupportCours();
        supportCours.setTitre(titre.trim());
        supportCours.setCheminFichier(storedFile.storedName());
        supportCours.setNomFichierOriginal(storedFile.originalName());
        supportCours.setTypeFichier(storedFile.contentType() == null ? "application/octet-stream" : storedFile.contentType());
        supportCours.setTailleOctets(storedFile.size());
        supportCours.setDateDepot(LocalDateTime.now());
        supportCours.setEnseignement(enseignement);

        return toResponse(supportCoursRepository.save(supportCours));
    }

    public Resource loadSupportFile(Integer id) {
        SupportCours supportCours = supportCoursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SupportCours not found"));
        return fileStorageService.loadAsResource(supportCours.getCheminFichier());
    }

    public SupportCours updateSupportCours(Integer id, SupportCours supportCoursDetails) {
        return supportCoursRepository.findById(id).map(supportCours -> {
            supportCours.setTitre(supportCoursDetails.getTitre());
            supportCours.setCheminFichier(supportCoursDetails.getCheminFichier());
            supportCours.setNomFichierOriginal(supportCoursDetails.getNomFichierOriginal());
            supportCours.setTypeFichier(supportCoursDetails.getTypeFichier());
            supportCours.setTailleOctets(supportCoursDetails.getTailleOctets());
            supportCours.setDateDepot(supportCoursDetails.getDateDepot());
            supportCours.setEnseignement(supportCoursDetails.getEnseignement());
            return supportCoursRepository.save(supportCours);
        }).orElseThrow(() -> new RuntimeException("SupportCours not found"));
    }

    public void deleteSupportCours(Integer id) {
        SupportCours supportCours = supportCoursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SupportCours not found"));
        supportCoursRepository.delete(supportCours);
        fileStorageService.delete(supportCours.getCheminFichier());
    }

    public SupportCoursResponseDTO toResponse(SupportCours supportCours) {
        SupportCoursResponseDTO response = new SupportCoursResponseDTO();
        response.setId(supportCours.getId());
        response.setTitre(supportCours.getTitre());
        response.setNomFichierOriginal(supportCours.getNomFichierOriginal());
        response.setTypeFichier(supportCours.getTypeFichier());
        response.setTailleOctets(supportCours.getTailleOctets());
        response.setDateDepot(supportCours.getDateDepot());
        if (supportCours.getEnseignement() != null) {
            response.setEnseignementId(supportCours.getEnseignement().getId());
        }
        response.setDownloadUrl("/api/course-materials/" + supportCours.getId() + "/download");
        return response;
    }
}

