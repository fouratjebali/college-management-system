package MiniProjet_Backend.Backend.Service;

import MiniProjet_Backend.Backend.Model.Enseignement;
import MiniProjet_Backend.Backend.Model.Groupe;
import MiniProjet_Backend.Backend.Model.Professeur;
import MiniProjet_Backend.Backend.Model.Seance;
import MiniProjet_Backend.Backend.Repository.EnseignementRepository;
import MiniProjet_Backend.Backend.Repository.GroupeRepository;
import MiniProjet_Backend.Backend.Repository.SeanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeanceServiceTest {
    @Mock
    private SeanceRepository seanceRepository;

    @Mock
    private EnseignementRepository enseignementRepository;

    @Mock
    private GroupeRepository groupeRepository;

    @InjectMocks
    private SeanceService seanceService;

    @Test
    void saveSeanceRejectsInvalidTimeRange() {
        Seance seance = validSeance();
        seance.setHeureDebut(LocalTime.of(12, 0));
        seance.setHeureFin(LocalTime.of(10, 0));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> seanceService.saveSeance(seance)
        );

        assertEquals("heureDebut doit être avant heureFin", exception.getMessage());
        verify(seanceRepository, never()).save(any(Seance.class));
    }

    @Test
    void saveSeanceRejectsGroupConflict() {
        Seance seance = validSeance();
        Seance conflict = validSeance();
        conflict.setId(99);

        when(groupeRepository.findById(1)).thenReturn(Optional.of(seance.getGroupe()));
        when(enseignementRepository.findById(2)).thenReturn(Optional.of(seance.getEnseignement()));
        when(seanceRepository.findGroupeConflicts(1, "Lundi", seance.getHeureDebut(), seance.getHeureFin(), null))
                .thenReturn(List.of(conflict));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> seanceService.saveSeance(seance)
        );

        assertEquals("Le groupe a déjà une séance sur ce créneau", exception.getMessage());
        verify(seanceRepository, never()).save(any(Seance.class));
    }

    @Test
    void saveSeancePersistsWhenThereIsNoConflict() {
        Seance seance = validSeance();

        when(groupeRepository.findById(1)).thenReturn(Optional.of(seance.getGroupe()));
        when(enseignementRepository.findById(2)).thenReturn(Optional.of(seance.getEnseignement()));
        when(seanceRepository.save(seance)).thenReturn(seance);

        Seance saved = seanceService.saveSeance(seance);

        assertEquals(seance, saved);
        verify(seanceRepository).save(seance);
    }

    private Seance validSeance() {
        Groupe groupe = new Groupe();
        groupe.setId(1);

        Professeur professeur = new Professeur();
        professeur.setId(3);

        Enseignement enseignement = new Enseignement();
        enseignement.setId(2);
        enseignement.setProfesseur(professeur);

        Seance seance = new Seance();
        seance.setTypeSeance("Cours");
        seance.setJoursemaine("Lundi");
        seance.setHeureDebut(LocalTime.of(8, 30));
        seance.setHeureFin(LocalTime.of(10, 0));
        seance.setSalle("A1");
        seance.setBatiment("Bloc A");
        seance.setGroupe(groupe);
        seance.setEnseignement(enseignement);
        return seance;
    }
}
