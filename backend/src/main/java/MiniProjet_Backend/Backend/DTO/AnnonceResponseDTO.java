package MiniProjet_Backend.Backend.DTO;

import java.time.LocalDateTime;

public class AnnonceResponseDTO {
    private Integer id;
    private String titre;
    private String contenu;
    private LocalDateTime datePublication;
    private LocalDateTime dateExpiration;
    private Boolean cibleGlobale;
    private String cibleRole;
    private Integer administrateurId;
    private String administrateurNom;
    private boolean active;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public String getAdministrateurNom() {
        return administrateurNom;
    }

    public void setAdministrateurNom(String administrateurNom) {
        this.administrateurNom = administrateurNom;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
