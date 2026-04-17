package MiniProjet_Backend.Backend.Config;

import MiniProjet_Backend.Backend.Model.Administrateur;
import MiniProjet_Backend.Backend.Model.Departement;
import MiniProjet_Backend.Backend.Model.Enseignement;
import MiniProjet_Backend.Backend.Model.Etudiant;
import MiniProjet_Backend.Backend.Model.Evaluation;
import MiniProjet_Backend.Backend.Model.Groupe;
import MiniProjet_Backend.Backend.Model.Matiere;
import MiniProjet_Backend.Backend.Model.Professeur;
import MiniProjet_Backend.Backend.Model.Seance;
import MiniProjet_Backend.Backend.Repository.DepartementRepository;
import MiniProjet_Backend.Backend.Repository.EnseignementRepository;
import MiniProjet_Backend.Backend.Repository.EtudiantRepository;
import MiniProjet_Backend.Backend.Repository.EvaluationRepository;
import MiniProjet_Backend.Backend.Repository.GroupeRepository;
import MiniProjet_Backend.Backend.Repository.MatiereRepository;
import MiniProjet_Backend.Backend.Repository.ProfesseurRepository;
import MiniProjet_Backend.Backend.Repository.SeanceRepository;
import MiniProjet_Backend.Backend.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Configuration
@Profile("!test")
public class SeedDataConfig {
    @Value("${app.seed.admin.email:admin@issatso.tn}")
    private String adminEmail;

    @Value("${app.seed.admin.password:Admin@12345}")
    private String adminPassword;

    @Bean
    CommandLineRunner seedAdminFlowData(
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            DepartementRepository departementRepository,
            MatiereRepository matiereRepository,
            GroupeRepository groupeRepository,
            ProfesseurRepository professeurRepository,
            EtudiantRepository etudiantRepository,
            EnseignementRepository enseignementRepository,
            SeanceRepository seanceRepository,
            EvaluationRepository evaluationRepository
    ) {
        return args -> {
            seedAdmin(passwordEncoder, userRepository);
            SeedAcademicData academicData = seedAcademicStructure(
                    departementRepository,
                    matiereRepository,
                    groupeRepository
            );
            Professeur professeur = seedProfessor(passwordEncoder, userRepository, professeurRepository);
            seedStudent(passwordEncoder, userRepository, etudiantRepository, academicData.groupe());
            seedEvaluationFlow(
                    enseignementRepository,
                    seanceRepository,
                    evaluationRepository,
                    professeur,
                    academicData.matiere(),
                    academicData.groupe()
            );
        };
    }

    private void seedAdmin(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            return;
        }

        Administrateur admin = new Administrateur();
        admin.setNomComplet("Administrateur Principal");
        admin.setEmail(adminEmail);
        admin.setMotDePasseHash(passwordEncoder.encode(adminPassword));
        admin.setActif(true);
        admin.setMatriculeAdmin("ADM-0001");
        admin.setFonction("Super Administrateur");
        userRepository.save(admin);
    }

    private SeedAcademicData seedAcademicStructure(
            DepartementRepository departementRepository,
            MatiereRepository matiereRepository,
            GroupeRepository groupeRepository
    ) {
        if (departementRepository.count() == 0) {
            List.of("Genie Informatique", "Genie Electrique", "Maintenance Industrielle")
                    .forEach(name -> {
                        Departement departement = new Departement();
                        departement.setNom(name);
                        departementRepository.save(departement);
                    });
        }

        Departement departement = departementRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No departement available for seed data"));

        if (matiereRepository.count() == 0) {
            Matiere architecture = new Matiere();
            architecture.setCode("ARCH-LOG");
            architecture.setLibelle("Architecture Logicielle");
            architecture.setCoefficient(2.0F);
            architecture.setDepartement(departement);
            matiereRepository.save(architecture);

            Matiere databases = new Matiere();
            databases.setCode("BD-2");
            databases.setLibelle("Bases de Donnees");
            databases.setCoefficient(2.0F);
            databases.setDepartement(departement);
            matiereRepository.save(databases);
        }

        if (groupeRepository.count() == 0) {
            Groupe groupe = new Groupe();
            groupe.setLibelle("GI-3A");
            groupe.setNiveau("3eme annee");
            groupe.setAnneeUniversitaire("2025-2026");
            groupe.setDepartement(departement);
            groupeRepository.save(groupe);
        }

        Matiere matiere = matiereRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No matiere available for seed data"));
        Groupe groupe = groupeRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No groupe available for seed data"));

        return new SeedAcademicData(matiere, groupe);
    }

    private Professeur seedProfessor(
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            ProfesseurRepository professeurRepository
    ) {
        return userRepository.findByEmail("prof.demo@issatso.tn")
                .map(user -> (Professeur) user)
                .orElseGet(() -> {
                    Professeur professeur = new Professeur();
                    professeur.setNomComplet("Nour Ben Ali");
                    professeur.setEmail("prof.demo@issatso.tn");
                    professeur.setMotDePasseHash(passwordEncoder.encode("Prof@12345"));
                    professeur.setActif(true);
                    professeur.setMatriculePro("PRO-0001");
                    professeur.setGrade("Assistant");
                    return professeurRepository.save(professeur);
                });
    }

    private void seedStudent(
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            EtudiantRepository etudiantRepository,
            Groupe groupe
    ) {
        if (userRepository.findByEmail("student.demo@issatso.tn").isPresent()) {
            return;
        }

        Etudiant etudiant = new Etudiant();
        etudiant.setNomComplet("Yassine Mansouri");
        etudiant.setEmail("student.demo@issatso.tn");
        etudiant.setMotDePasseHash(passwordEncoder.encode("Student@12345"));
        etudiant.setActif(true);
        etudiant.setMatricule("ETU-0001");
        etudiant.setNiveau("3eme annee");
        etudiant.setGroupe(groupe);
        etudiantRepository.save(etudiant);
    }

    private void seedEvaluationFlow(
            EnseignementRepository enseignementRepository,
            SeanceRepository seanceRepository,
            EvaluationRepository evaluationRepository,
            Professeur professeur,
            Matiere matiere,
            Groupe groupe
    ) {
        if (evaluationRepository.count() > 0) {
            return;
        }

        Enseignement enseignement = new Enseignement();
        enseignement.setMatiere(matiere);
        enseignement.setProfesseur(professeur);
        enseignement.setSemestre(2);
        enseignement.setAnneeUniversitaire("2025-2026");
        enseignement = enseignementRepository.save(enseignement);

        Seance seance = new Seance();
        seance.setTypeSeance("Cours");
        seance.setJoursemaine("Mardi");
        seance.setHeureDebut(LocalTime.of(9, 0));
        seance.setHeureFin(LocalTime.of(10, 30));
        seance.setSalle("Salle 204");
        seance.setBatiment("Bloc B");
        seance.setEnseignement(enseignement);
        seance.setGroupe(groupe);
        seance = seanceRepository.save(seance);

        Evaluation evaluation = new Evaluation();
        evaluation.setLibelle("DS Architecture Logicielle");
        evaluation.setTypeEvaluation("Devoir Surveille");
        evaluation.setDateEvaluation(LocalDateTime.now().plusDays(7).withHour(9).withMinute(0));
        evaluation.setCoefficient(1.0F);
        evaluation.setSeance(seance);
        evaluationRepository.save(evaluation);
    }

    private record SeedAcademicData(Matiere matiere, Groupe groupe) {
    }
}
