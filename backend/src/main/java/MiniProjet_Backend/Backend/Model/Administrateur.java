package MiniProjet_Backend.Backend.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name="administrateur")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Administrateur extends User {
    @Column(nullable = false, unique = true)
    private String matriculeAdmin;

    @Column(nullable = false)
    private String fonction;

    // Explicit getter and setter methods for inherited fields from User
    // These ensure Lombok generates the methods correctly
    @Override
    public String getNomComplet() {
        return super.getNomComplet();
    }

    @Override
    public void setNomComplet(String nomComplet) {
        super.setNomComplet(nomComplet);
    }

    @Override
    public String getEmail() {
        return super.getEmail();
    }

    @Override
    public void setEmail(String email) {
        super.setEmail(email);
    }

    @Override
    public String getMotDePasseHash() {
        return super.getMotDePasseHash();
    }

    @Override
    public void setMotDePasseHash(String motDePasseHash) {
        super.setMotDePasseHash(motDePasseHash);
    }

    @Override
    public boolean isActif() {
        return super.isActif();
    }

    @Override
    public void setActif(boolean actif) {
        super.setActif(actif);
    }

    // Explicit getter and setter methods for local fields
    public String getMatriculeAdmin() {
        return matriculeAdmin;
    }

    public void setMatriculeAdmin(String matriculeAdmin) {
        this.matriculeAdmin = matriculeAdmin;
    }

    public String getFonction() {
        return fonction;
    }

    public void setFonction(String fonction) {
        this.fonction = fonction;
    }
}
