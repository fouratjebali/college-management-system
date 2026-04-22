package MiniProjet_Backend.Backend.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name="seance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String typeSeance;

    @Column(nullable = false)
    private String joursemaine;

    @Column(nullable = false)
    private LocalTime heureDebut;

    @Column(nullable = false)
    private LocalTime heureFin;

    @Column(nullable = false)
    private String salle;

    @Column(nullable = false)
    private String batiment;

    @Column
    private String attendanceStatus = "OUVERTE";

    @Column
    private LocalDateTime attendanceClosedAt;

    @Column
    private String collectiveAbsenceStatus = "AUCUNE";

    @Column
    private LocalDateTime collectiveAbsenceReportedAt;

    @Column
    private LocalDateTime collectiveAbsenceConfirmedAt;

    @ManyToOne
    @JoinColumn(name="enseignement_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Enseignement enseignement;

    @ManyToOne
    @JoinColumn(name="groupe_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Groupe groupe;

    @OneToMany(mappedBy = "seance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Presence> presences;

    @OneToMany(mappedBy = "seance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Evaluation> evaluations;
}

