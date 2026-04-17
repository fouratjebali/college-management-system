package MiniProjet_Backend.Backend.DTO;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class AnnonceRequestDTO {
    @NotBlank
    private String titre;

    @NotBlank
    private String contenu;

    private LocalDateTime datePublication;

    @NotNull
    @Future
    private LocalDateTime dateExpiration;

    private Boolean cibleGlobale = true;

    private String cibleRole;

    @NotNull
    private Integer administrateurId;

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDateTime getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(LocalDateTime datePublication) {
        this.datePublication = datePublication;
    }

    public LocalDateTime getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDateTime dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public Boolean getCibleGlobale() {
        return cibleGlobale;
    }

    public void setCibleGlobale(Boolean cibleGlobale) {
        this.cibleGlobale = cibleGlobale;
    }

    public String getCibleRole() {
        return cibleRole;
    }

    public void setCibleRole(String cibleRole) {
        this.cibleRole = cibleRole;
    }

    public Integer getAdministrateurId() {
        return administrateurId;
    }

    public void setAdministrateurId(Integer administrateurId) {
        this.administrateurId = administrateurId;
    }
}
