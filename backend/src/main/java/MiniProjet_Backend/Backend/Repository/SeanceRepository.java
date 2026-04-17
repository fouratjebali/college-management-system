package MiniProjet_Backend.Backend.Repository;

import MiniProjet_Backend.Backend.Model.Seance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface SeanceRepository extends JpaRepository<Seance, Integer> {
    List<Seance> findByEnseignementId(Integer enseignementId);
    List<Seance> findByGroupeId(Integer groupeId);

    Page<Seance> findByGroupeId(Integer groupeId, Pageable pageable);

    Page<Seance> findByEnseignementProfesseurId(Integer professeurId, Pageable pageable);

    Page<Seance> findByBatimentIgnoreCaseAndSalleIgnoreCase(String batiment, String salle, Pageable pageable);

    @Query("""
            select s from Seance s
            where s.groupe.id = :groupeId
              and lower(s.joursemaine) = lower(:jourSemaine)
              and s.heureDebut < :heureFin
              and s.heureFin > :heureDebut
              and (:excludeId is null or s.id <> :excludeId)
            """)
    List<Seance> findGroupeConflicts(
            @Param("groupeId") Integer groupeId,
            @Param("jourSemaine") String jourSemaine,
            @Param("heureDebut") LocalTime heureDebut,
            @Param("heureFin") LocalTime heureFin,
            @Param("excludeId") Integer excludeId
    );

    @Query("""
            select s from Seance s
            where lower(s.batiment) = lower(:batiment)
              and lower(s.salle) = lower(:salle)
              and lower(s.joursemaine) = lower(:jourSemaine)
              and s.heureDebut < :heureFin
              and s.heureFin > :heureDebut
              and (:excludeId is null or s.id <> :excludeId)
            """)
    List<Seance> findSalleConflicts(
            @Param("batiment") String batiment,
            @Param("salle") String salle,
            @Param("jourSemaine") String jourSemaine,
            @Param("heureDebut") LocalTime heureDebut,
            @Param("heureFin") LocalTime heureFin,
            @Param("excludeId") Integer excludeId
    );

    @Query("""
            select s from Seance s
            where s.enseignement.professeur.id = :professeurId
              and lower(s.joursemaine) = lower(:jourSemaine)
              and s.heureDebut < :heureFin
              and s.heureFin > :heureDebut
              and (:excludeId is null or s.id <> :excludeId)
            """)
    List<Seance> findProfesseurConflicts(
            @Param("professeurId") Integer professeurId,
            @Param("jourSemaine") String jourSemaine,
            @Param("heureDebut") LocalTime heureDebut,
            @Param("heureFin") LocalTime heureFin,
            @Param("excludeId") Integer excludeId
    );
}

