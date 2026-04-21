package MiniProjet_Backend.Backend.Config;

import MiniProjet_Backend.Backend.Model.Administrateur;
import MiniProjet_Backend.Backend.Model.AcademicYear;
import MiniProjet_Backend.Backend.Model.Annonce;
import MiniProjet_Backend.Backend.Model.Departement;
import MiniProjet_Backend.Backend.Model.Enseignement;
import MiniProjet_Backend.Backend.Model.Etudiant;
import MiniProjet_Backend.Backend.Model.Evaluation;
import MiniProjet_Backend.Backend.Model.Groupe;
import MiniProjet_Backend.Backend.Model.Matiere;
import MiniProjet_Backend.Backend.Model.Note;
import MiniProjet_Backend.Backend.Model.Presence;
import MiniProjet_Backend.Backend.Model.Professeur;
import MiniProjet_Backend.Backend.Model.Seance;
import MiniProjet_Backend.Backend.Model.Semester;
import MiniProjet_Backend.Backend.Model.SupportCours;
import MiniProjet_Backend.Backend.Model.User;
import MiniProjet_Backend.Backend.Repository.AcademicYearRepository;
import MiniProjet_Backend.Backend.Repository.AnnonceRepository;
import MiniProjet_Backend.Backend.Repository.DepartementRepository;
import MiniProjet_Backend.Backend.Repository.EnseignementRepository;
import MiniProjet_Backend.Backend.Repository.EtudiantRepository;
import MiniProjet_Backend.Backend.Repository.EvaluationRepository;
import MiniProjet_Backend.Backend.Repository.GroupeRepository;
import MiniProjet_Backend.Backend.Repository.MatiereRepository;
import MiniProjet_Backend.Backend.Repository.NoteRepository;
import MiniProjet_Backend.Backend.Repository.PresenceRepository;
import MiniProjet_Backend.Backend.Repository.ProfesseurRepository;
import MiniProjet_Backend.Backend.Repository.SeanceRepository;
import MiniProjet_Backend.Backend.Repository.SemesterRepository;
import MiniProjet_Backend.Backend.Repository.SupportCoursRepository;
import MiniProjet_Backend.Backend.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Configuration
@Profile("!test")
public class SeedDataConfig {
    private static final String UNIVERSITY_YEAR = "2025-2026";
    private static final String PROFESSOR_EMAIL = "prof.demo@issatso.tn";
    private static final String STUDENT_PASSWORD = "Student@12345";

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
            EvaluationRepository evaluationRepository,
            NoteRepository noteRepository,
            PresenceRepository presenceRepository,
            SupportCoursRepository supportCoursRepository,
            AnnonceRepository annonceRepository,
            AcademicYearRepository academicYearRepository,
            SemesterRepository semesterRepository
    ) {
        return args -> {
            seedAdmin(passwordEncoder, userRepository);
            seedAcademicCalendar(academicYearRepository, semesterRepository);
            SeedAcademicData academicData = seedAcademicStructure(
                    departementRepository,
                    matiereRepository,
                    groupeRepository
            );
            Professeur professeur = seedProfessor(passwordEncoder, userRepository, professeurRepository);
            seedStudents(
                    passwordEncoder,
                    userRepository,
                    etudiantRepository,
                    academicData.gi3a(),
                    academicData.groups()
            );
            seedProfessorWeeklySchedule(
                    enseignementRepository,
                    seanceRepository,
                    evaluationRepository,
                    professeur,
                    academicData.subjects(),
                    academicData.groups()
            );
            seedStudentDashboardDemoData(
                    passwordEncoder,
                    userRepository,
                    professeurRepository,
                    etudiantRepository,
                    enseignementRepository,
                    seanceRepository,
                    evaluationRepository,
                    noteRepository,
                    presenceRepository,
                    supportCoursRepository,
                    annonceRepository,
                    academicData.subjects(),
                    academicData.gi3a()
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

    private void seedAcademicCalendar(
            AcademicYearRepository academicYearRepository,
            SemesterRepository semesterRepository
    ) {
        AcademicYear academicYear = academicYearRepository.findByLabel(UNIVERSITY_YEAR).orElseGet(() -> {
            AcademicYear year = new AcademicYear();
            year.setLabel(UNIVERSITY_YEAR);
            year.setStartDate(LocalDate.of(2025, 9, 1));
            year.setEndDate(LocalDate.of(2026, 6, 30));
            year.setActive(true);
            year.setLocked(false);
            return academicYearRepository.save(year);
        });

        academicYear.setActive(true);
        academicYearRepository.save(academicYear);

        ensureSemester(
                semesterRepository,
                academicYear,
                "S1",
                "Semestre 1",
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2026, 1, 31),
                true,
                false
        );
        ensureSemester(
                semesterRepository,
                academicYear,
                "S2",
                "Semestre 2",
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 6, 30),
                false,
                false
        );
    }

    private Semester ensureSemester(
            SemesterRepository semesterRepository,
            AcademicYear academicYear,
            String code,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            boolean active,
            boolean locked
    ) {
        return semesterRepository.findByAcademicYearIdOrderByStartDateAsc(academicYear.getId()).stream()
                .filter(semester -> semester.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseGet(() -> {
                    Semester semester = new Semester();
                    semester.setAcademicYear(academicYear);
                    semester.setCode(code);
                    semester.setName(name);
                    semester.setStartDate(startDate);
                    semester.setEndDate(endDate);
                    semester.setActive(active);
                    semester.setLocked(locked);
                    return semesterRepository.save(semester);
                });
    }

    private SeedAcademicData seedAcademicStructure(
            DepartementRepository departementRepository,
            MatiereRepository matiereRepository,
            GroupeRepository groupeRepository
    ) {
        List.of("Genie Informatique", "Genie Electrique", "Maintenance Industrielle")
                .forEach(name -> ensureDepartment(departementRepository, name));

        Departement informatique = departementRepository.findByNom("Genie Informatique")
                .orElseThrow(() -> new IllegalStateException("Genie Informatique department missing"));

        List<Matiere> subjects = List.of(
                ensureSubject(matiereRepository, informatique, "ARCH-LOG", "Architecture Logicielle", 2.0F),
                ensureSubject(matiereRepository, informatique, "BD-2", "Bases de Donnees", 2.0F),
                ensureSubject(matiereRepository, informatique, "WEB-ANG", "Developpement Web Angular", 1.5F),
                ensureSubject(matiereRepository, informatique, "SPRING-API", "APIs REST Spring Boot", 2.0F),
                ensureSubject(matiereRepository, informatique, "DEVOPS", "DevOps et Docker", 1.5F)
        );

        Groupe gi3a = ensureGroup(groupeRepository, informatique, "GI-3A", "3eme annee");
        List<Groupe> groups = List.of(
                gi3a,
                ensureGroup(groupeRepository, informatique, "GI-1A", "1ere annee"),
                ensureGroup(groupeRepository, informatique, "GI-1B", "1ere annee"),
                ensureGroup(groupeRepository, informatique, "GI-2A", "2eme annee"),
                ensureGroup(groupeRepository, informatique, "GI-2B", "2eme annee"),
                ensureGroup(groupeRepository, informatique, "GI-3B", "3eme annee"),
                ensureGroup(groupeRepository, informatique, "GI-3C", "3eme annee"),
                ensureGroup(groupeRepository, informatique, "GI-4A", "4eme annee"),
                ensureGroup(groupeRepository, informatique, "GI-4B", "4eme annee"),
                ensureGroup(groupeRepository, informatique, "GI-5A", "5eme annee"),
                ensureGroup(groupeRepository, informatique, "GI-5B", "5eme annee")
        );

        return new SeedAcademicData(subjects, groups, gi3a);
    }

    private Departement ensureDepartment(DepartementRepository departementRepository, String name) {
        return departementRepository.findByNom(name).orElseGet(() -> {
            Departement departement = new Departement();
            departement.setNom(name);
            return departementRepository.save(departement);
        });
    }

    private Matiere ensureSubject(
            MatiereRepository matiereRepository,
            Departement departement,
            String code,
            String label,
            Float coefficient
    ) {
        return matiereRepository.findByCode(code).orElseGet(() -> {
            Matiere matiere = new Matiere();
            matiere.setCode(code);
            matiere.setLibelle(label);
            matiere.setCoefficient(coefficient);
            matiere.setDepartement(departement);
            return matiereRepository.save(matiere);
        });
    }

    private Groupe ensureGroup(
            GroupeRepository groupeRepository,
            Departement departement,
            String label,
            String level
    ) {
        return groupeRepository.findByLibelle(label).orElseGet(() -> {
            Groupe groupe = new Groupe();
            groupe.setLibelle(label);
            groupe.setNiveau(level);
            groupe.setAnneeUniversitaire(UNIVERSITY_YEAR);
            groupe.setDepartement(departement);
            return groupeRepository.save(groupe);
        });
    }

    private Professeur seedProfessor(
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            ProfesseurRepository professeurRepository
    ) {
        return userRepository.findByEmail(PROFESSOR_EMAIL)
                .map(user -> (Professeur) user)
                .orElseGet(() -> {
                    Professeur professeur = new Professeur();
                    professeur.setNomComplet("Nour Ben Ali");
                    professeur.setEmail(PROFESSOR_EMAIL);
                    professeur.setMotDePasseHash(passwordEncoder.encode("Prof@12345"));
                    professeur.setActif(true);
                    professeur.setMatriculePro("PRO-0001");
                    professeur.setGrade("Assistant");
                    return professeurRepository.save(professeur);
                });
    }

    private List<Professeur> seedSubjectProfessors(
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            ProfesseurRepository professeurRepository
    ) {
        return List.of(
                ensureProfessor(
                        passwordEncoder,
                        userRepository,
                        professeurRepository,
                        "prof.arch@issatso.tn",
                        "Mouna Kallel",
                        "PRO-ARCH"
                ),
                ensureProfessor(
                        passwordEncoder,
                        userRepository,
                        professeurRepository,
                        "prof.bd@issatso.tn",
                        "Hatem Trabelsi",
                        "PRO-BD"
                ),
                ensureProfessor(
                        passwordEncoder,
                        userRepository,
                        professeurRepository,
                        "prof.angular@issatso.tn",
                        "Sarra Mejri",
                        "PRO-ANG"
                ),
                ensureProfessor(
                        passwordEncoder,
                        userRepository,
                        professeurRepository,
                        "prof.spring@issatso.tn",
                        "Walid Gharbi",
                        "PRO-SPR"
                ),
                ensureProfessor(
                        passwordEncoder,
                        userRepository,
                        professeurRepository,
                        "prof.devops@issatso.tn",
                        "Nesrine Ayari",
                        "PRO-DEV"
                )
        );
    }

    private Professeur ensureProfessor(
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            ProfesseurRepository professeurRepository,
            String email,
            String name,
            String matricule
    ) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user instanceof Professeur existingProfessor) {
            existingProfessor.setNomComplet(name);
            existingProfessor.setMatriculePro(matricule);
            existingProfessor.setGrade("Assistant");
            existingProfessor.setActif(true);
            return professeurRepository.save(existingProfessor);
        }

        if (user != null) {
            throw new IllegalStateException("Email already used by another account: " + email);
        }

        Professeur professeur = new Professeur();
        professeur.setNomComplet(name);
        professeur.setEmail(email);
        professeur.setMotDePasseHash(passwordEncoder.encode("Prof@12345"));
        professeur.setActif(true);
        professeur.setMatriculePro(matricule);
        professeur.setGrade("Assistant");
        return professeurRepository.save(professeur);
    }

    private void seedStudents(
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            EtudiantRepository etudiantRepository,
            Groupe gi3a,
            List<Groupe> groups
    ) {
        seedDemoStudent(passwordEncoder, userRepository, etudiantRepository, gi3a);
        seedGeneratedStudentsForGroup(
                passwordEncoder,
                userRepository,
                etudiantRepository,
                gi3a,
                19
        );

        groups.stream()
                .filter(group -> !"GI-3A".equals(group.getLibelle()))
                .forEach(group -> seedGeneratedStudentsForGroup(
                        passwordEncoder,
                        userRepository,
                        etudiantRepository,
                        group,
                        20
                ));
    }

    private void seedDemoStudent(
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            EtudiantRepository etudiantRepository,
            Groupe group
    ) {
        User user = userRepository.findByEmail("student.demo@issatso.tn").orElse(null);

        if (user instanceof Etudiant existingStudent) {
            existingStudent.setGroupe(group);
            existingStudent.setNiveau(group.getNiveau());
            etudiantRepository.save(existingStudent);
            return;
        }

        if (user != null) {
            return;
        }

        Etudiant etudiant = new Etudiant();
        etudiant.setNomComplet("Yassine Mansouri");
        etudiant.setEmail("student.demo@issatso.tn");
        etudiant.setMotDePasseHash(passwordEncoder.encode(STUDENT_PASSWORD));
        etudiant.setActif(true);
        etudiant.setMatricule("ETU-0001");
        etudiant.setNiveau(group.getNiveau());
        etudiant.setGroupe(group);
        etudiantRepository.save(etudiant);
    }

    private void seedGeneratedStudentsForGroup(
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            EtudiantRepository etudiantRepository,
            Groupe group,
            int count
    ) {
        String slug = group.getLibelle().toLowerCase().replaceAll("[^a-z0-9]", "");

        for (int index = 1; index <= count; index++) {
            String paddedIndex = String.format("%02d", index);
            String email = "student." + slug + "." + paddedIndex + "@issatso.tn";

            User user = userRepository.findByEmail(email).orElse(null);
            if (user instanceof Etudiant existingStudent) {
                existingStudent.setNomComplet(buildStudentName(group.getLibelle(), index));
                existingStudent.setMatricule(slug.toUpperCase() + "-" + paddedIndex);
                existingStudent.setNiveau(group.getNiveau());
                existingStudent.setGroupe(group);
                etudiantRepository.save(existingStudent);
                continue;
            }

            if (user != null) {
                continue;
            }

            Etudiant etudiant = new Etudiant();
            etudiant.setNomComplet(buildStudentName(group.getLibelle(), index));
            etudiant.setEmail(email);
            etudiant.setMotDePasseHash(passwordEncoder.encode(STUDENT_PASSWORD));
            etudiant.setActif(true);
            etudiant.setMatricule(slug.toUpperCase() + "-" + paddedIndex);
            etudiant.setNiveau(group.getNiveau());
            etudiant.setGroupe(group);
            etudiantRepository.save(etudiant);
        }
    }

    private String buildStudentName(String groupLabel, int index) {
        List<String> firstNames = List.of(
                "Amine", "Sarra", "Youssef", "Meriem", "Karim",
                "Ines", "Omar", "Nour", "Aziz", "Rania",
                "Malek", "Aya", "Mehdi", "Lina", "Sami",
                "Hiba", "Fares", "Eya", "Walid", "Salma",
                "Anis", "Molka", "Taha", "Yosra", "Bilel",
                "Amani", "Skander", "Nesrine", "Houssem", "Rim",
                "Iheb", "Marwa", "Rayen", "Sirine", "Ghassen",
                "Hadil", "Khalil", "Emna", "Seif", "Ons"
        );
        List<String> lastNames = List.of(
                "Ben Ali", "Mansouri", "Trabelsi", "Gharbi", "Jebali",
                "Saidi", "Mejri", "Kacem", "Ayari", "Haddad",
                "Bouzid", "Cherif", "Zouari", "Mbarek", "Lahmar",
                "Brahmi", "Nafti", "Dridi", "Mokhtar", "Ferchichi",
                "Hamdi", "Ksouri", "Bennour", "Rezgui", "Tlili",
                "Mahfoudh", "Baccar", "Jaziri", "Ammar", "Chaabane",
                "Dhaouadi", "Kallel", "Sellami", "Riahi", "Zribi",
                "Guesmi", "Nasri", "Abidi", "Belhadj", "Chtourou"
        );

        int groupOffset = Math.abs(groupLabel.hashCode());
        int firstNameIndex = (groupOffset + index - 1) % firstNames.size();
        int lastNameIndex = ((groupOffset / firstNames.size()) + (index * 3) - 3) % lastNames.size();

        return firstNames.get(firstNameIndex) + " " + lastNames.get(lastNameIndex);
    }

    private void seedProfessorWeeklySchedule(
            EnseignementRepository enseignementRepository,
            SeanceRepository seanceRepository,
            EvaluationRepository evaluationRepository,
            Professeur professeur,
            List<Matiere> subjects,
            List<Groupe> groups
    ) {
        List<Enseignement> teachings = subjects.stream()
                .map(subject -> ensureTeaching(enseignementRepository, professeur, subject))
                .toList();
        List<String> days = List.of("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi");
        List<TimeSlot> slots = List.of(
                new TimeSlot(LocalTime.of(8, 30), LocalTime.of(10, 0), "Cours"),
                new TimeSlot(LocalTime.of(10, 15), LocalTime.of(11, 45), "TD"),
                new TimeSlot(LocalTime.of(13, 30), LocalTime.of(15, 0), "TP"),
                new TimeSlot(LocalTime.of(15, 15), LocalTime.of(16, 45), "Cours")
        );

        int scheduleIndex = 0;
        for (String day : days) {
            for (TimeSlot slot : slots) {
                Enseignement teaching = teachings.get(scheduleIndex % teachings.size());
                Groupe group = groups.get(scheduleIndex % groups.size());
                Seance seance = ensureSession(
                        seanceRepository,
                        teaching,
                        group,
                        day,
                        slot,
                        "Bloc " + (char) ('A' + (scheduleIndex % 4)),
                        "Salle " + (101 + scheduleIndex)
                );
                ensureEvaluation(evaluationRepository, seance, scheduleIndex);
                scheduleIndex++;
            }
        }
    }

    private Enseignement ensureTeaching(
            EnseignementRepository enseignementRepository,
            Professeur professeur,
            Matiere subject
    ) {
        return enseignementRepository.findByProfesseurId(professeur.getId()).stream()
                .filter(enseignement -> enseignement.getMatiere().getId().equals(subject.getId()))
                .findFirst()
                .orElseGet(() -> {
                    Enseignement enseignement = new Enseignement();
                    enseignement.setMatiere(subject);
                    enseignement.setProfesseur(professeur);
                    enseignement.setSemestre(2);
                    enseignement.setAnneeUniversitaire(UNIVERSITY_YEAR);
                    return enseignementRepository.save(enseignement);
                });
    }

    private Seance ensureSession(
            SeanceRepository seanceRepository,
            Enseignement teaching,
            Groupe group,
            String day,
            TimeSlot slot,
            String building,
            String room
    ) {
        return seanceRepository.findByEnseignementId(teaching.getId()).stream()
                .filter(seance -> seance.getGroupe().getId().equals(group.getId()))
                .filter(seance -> seance.getJoursemaine().equalsIgnoreCase(day))
                .filter(seance -> seance.getHeureDebut().equals(slot.start()))
                .findFirst()
                .orElseGet(() -> {
                    Seance seance = new Seance();
                    seance.setTypeSeance(slot.type());
                    seance.setJoursemaine(day);
                    seance.setHeureDebut(slot.start());
                    seance.setHeureFin(slot.end());
                    seance.setSalle(room);
                    seance.setBatiment(building);
                    seance.setEnseignement(teaching);
                    seance.setGroupe(group);
                    return seanceRepository.save(seance);
                });
    }

    private void ensureEvaluation(
            EvaluationRepository evaluationRepository,
            Seance seance,
            int scheduleIndex
    ) {
        if (!evaluationRepository.findBySeanceId(seance.getId()).isEmpty()) {
            return;
        }

        Evaluation evaluation = new Evaluation();
        evaluation.setLibelle("DS " + seance.getEnseignement().getMatiere().getLibelle()
                + " - " + seance.getGroupe().getLibelle());
        evaluation.setTypeEvaluation("DS");
        evaluation.setDateEvaluation(LocalDateTime.now()
                .plusDays(7L + scheduleIndex)
                .withHour(seance.getHeureDebut().getHour())
                .withMinute(seance.getHeureDebut().getMinute())
                .withSecond(0)
                .withNano(0));
        evaluation.setCoefficient(2.0F);
        evaluation.setSeance(seance);
        evaluationRepository.save(evaluation);
    }

    private void seedStudentDashboardDemoData(
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            ProfesseurRepository professeurRepository,
            EtudiantRepository etudiantRepository,
            EnseignementRepository enseignementRepository,
            SeanceRepository seanceRepository,
            EvaluationRepository evaluationRepository,
            NoteRepository noteRepository,
            PresenceRepository presenceRepository,
            SupportCoursRepository supportCoursRepository,
            AnnonceRepository annonceRepository,
            List<Matiere> subjects,
            Groupe group
    ) throws IOException {
        Etudiant student = etudiantRepository.findByEmail("student.demo@issatso.tn")
                .orElseThrow(() -> new IllegalStateException("Demo student missing"));
        Administrateur admin = userRepository.findByEmail(adminEmail)
                .filter(Administrateur.class::isInstance)
                .map(Administrateur.class::cast)
                .orElseThrow(() -> new IllegalStateException("Demo admin missing"));
        List<Professeur> professors = seedSubjectProfessors(passwordEncoder, userRepository, professeurRepository);
        List<Enseignement> teachings = List.of(
                ensureTeaching(enseignementRepository, professors.get(0), subjects.get(0)),
                ensureTeaching(enseignementRepository, professors.get(1), subjects.get(1)),
                ensureTeaching(enseignementRepository, professors.get(2), subjects.get(2)),
                ensureTeaching(enseignementRepository, professors.get(3), subjects.get(3)),
                ensureTeaching(enseignementRepository, professors.get(4), subjects.get(4))
        );
        reassignGroupSubjectSessions(seanceRepository, group, teachings);
        List<StudentDemoSession> demoSessions = List.of(
                new StudentDemoSession(0, "Lundi", LocalTime.of(10, 15), LocalTime.of(11, 45),
                        "Cours", "Bloc A", "Salle 101", "Seance Architecture Logicielle",
                        "Seance", 0F, "Planifiee", "", "Present"),
                new StudentDemoSession(0, "Mardi", LocalTime.of(8, 30), LocalTime.of(10, 0),
                        "TD", "Bloc A", "Salle 102", "TD Architecture Logicielle",
                        "Seance", 0F, "Planifiee", "", "Present"),
                new StudentDemoSession(1, "Mardi", LocalTime.of(10, 15), LocalTime.of(11, 45),
                        "Cours", "Bloc B", "Salle 203", "Cours Bases de Donnees",
                        "Seance", 0F, "Planifiee", "", "Present"),
                new StudentDemoSession(1, "Mercredi", LocalTime.of(8, 30), LocalTime.of(10, 0),
                        "TD", "Bloc B", "Salle 204", "TD Bases de Donnees",
                        "Seance", 0F, "Planifiee", "", "Present"),
                new StudentDemoSession(2, "Mercredi", LocalTime.of(13, 30), LocalTime.of(15, 0),
                        "Cours", "Lab Web", "Salle 301", "Cours Angular",
                        "Seance", 0F, "Planifiee", "", "Present"),
                new StudentDemoSession(2, "Jeudi", LocalTime.of(8, 30), LocalTime.of(10, 0),
                        "TD", "Lab Web", "Salle 302", "TD Angular",
                        "Seance", 0F, "Planifiee", "", "Present"),
                new StudentDemoSession(2, "Jeudi", LocalTime.of(13, 30), LocalTime.of(15, 0),
                        "TP", "Lab Web", "Salle TP 2", "TP Angular",
                        "Seance", 0F, "Planifiee", "", "Retard"),
                new StudentDemoSession(3, "Jeudi", LocalTime.of(10, 15), LocalTime.of(11, 45),
                        "Cours", "Bloc C", "Salle 305", "Cours APIs REST Spring Boot",
                        "Seance", 0F, "Planifiee", "", "Present"),
                new StudentDemoSession(3, "Vendredi", LocalTime.of(8, 30), LocalTime.of(10, 0),
                        "TD", "Bloc C", "Salle 306", "TD APIs REST Spring Boot",
                        "Seance", 0F, "Planifiee", "", "Present"),
                new StudentDemoSession(3, "Vendredi", LocalTime.of(13, 30), LocalTime.of(15, 0),
                        "TP", "Lab API", "Salle TP 3", "TP APIs REST Spring Boot",
                        "Seance", 0F, "Planifiee", "", "Present"),
                new StudentDemoSession(4, "Lundi", LocalTime.of(15, 15), LocalTime.of(16, 45),
                        "Cours", "Bloc D", "Salle 402", "Cours DevOps",
                        "Seance", 0F, "Planifiee", "", "Present"),
                new StudentDemoSession(4, "Mardi", LocalTime.of(15, 15), LocalTime.of(16, 45),
                        "TD", "Bloc D", "Salle 403", "TD DevOps",
                        "Seance", 0F, "Planifiee", "", "Present"),
                new StudentDemoSession(2, "Vendredi", LocalTime.of(15, 15), LocalTime.of(16, 45),
                        "Rattrapage", "Lab Web", "Salle TP 1", "Rattrapage Angular Routing",
                        "Rattrapage", 12.5F, "En attente", "Seance de consolidation", "Absent")
        );

        for (int index = 0; index < demoSessions.size(); index++) {
            StudentDemoSession demoSession = demoSessions.get(index);
            Enseignement teaching = teachings.get(demoSession.subjectIndex());
            Seance seance = ensureSession(
                    seanceRepository,
                    teaching,
                    group,
                    demoSession.day(),
                    new TimeSlot(demoSession.start(), demoSession.end(), demoSession.type()),
                    demoSession.building(),
                    demoSession.room()
            );
            if (isMajorEvaluationType(demoSession.evaluationType())) {
                Evaluation evaluation = ensureStudentEvaluation(
                        evaluationRepository,
                        seance,
                        demoSession.evaluationLabel(),
                        demoSession.evaluationType(),
                        index
                );
                ensureStudentNote(noteRepository, evaluation, student, demoSession);
            }
            ensureStudentPresence(presenceRepository, seance, student, demoSession, index);
        }
        ensureStudentEvaluationPlan(seanceRepository, evaluationRepository, teachings, group);
        ensureStudentMajorEvaluationNotes(
                seanceRepository,
                evaluationRepository,
                noteRepository,
                student,
                group
        );

        ensureStudentSupport(
                supportCoursRepository,
                teachings.get(0),
                "Chapitre 1 - Architecture en couches",
                "architecture-couches.txt",
                "Rappels sur la separation des responsabilites, DTO, services et repositories."
        );
        ensureStudentSupport(
                supportCoursRepository,
                teachings.get(2),
                "Guide TP Angular - Dashboard",
                "tp-angular-dashboard.txt",
                "Consignes pour construire un dashboard Angular avec services HTTP et signals."
        );
        ensureStudentSupport(
                supportCoursRepository,
                teachings.get(3),
                "Fiche pratique REST Spring Boot",
                "fiche-rest-spring.txt",
                "Exemples de controllers, DTO, services et validation des endpoints REST."
        );

        ensureStudentAnnouncement(
                annonceRepository,
                admin,
                "Planning GI-3A publie",
                "L'emploi du temps de la semaine GI-3A est disponible dans l'espace etudiant."
        );
        ensureStudentAnnouncement(
                annonceRepository,
                admin,
                "Depot des supports de cours",
                "Les nouveaux supports Angular et Spring Boot sont disponibles en telechargement."
        );
    }

    private Evaluation ensureStudentEvaluation(
            EvaluationRepository evaluationRepository,
            Seance seance,
            String label,
            String type,
            int index
    ) {
        Evaluation evaluation = evaluationRepository.findBySeanceId(seance.getId()).stream()
                .filter(existingEvaluation -> existingEvaluation.getLibelle().equalsIgnoreCase(label))
                .findFirst()
                .orElseGet(() -> {
                    Evaluation newEvaluation = new Evaluation();
                    newEvaluation.setDateEvaluation(LocalDateTime.now()
                            .minusDays(20L - (index * 3L))
                            .withHour(seance.getHeureDebut().getHour())
                            .withMinute(seance.getHeureDebut().getMinute())
                            .withSecond(0)
                            .withNano(0));
                    return newEvaluation;
                });
        evaluation.setLibelle(label);
        evaluation.setTypeEvaluation(normalizeMajorEvaluationType(type));
        evaluation.setCoefficient(seedEvaluationCoefficient(evaluation.getTypeEvaluation(), seance));
        evaluation.setSeance(seance);
        return evaluationRepository.save(evaluation);
    }

    private void reassignGroupSubjectSessions(
            SeanceRepository seanceRepository,
            Groupe group,
            List<Enseignement> teachings
    ) {
        seanceRepository.findByGroupeId(group.getId()).forEach(seance -> teachings.stream()
                .filter(teaching -> teaching.getMatiere().getId().equals(seance.getEnseignement().getMatiere().getId()))
                .findFirst()
                .ifPresent(teaching -> {
                    seance.setEnseignement(teaching);
                    seanceRepository.save(seance);
                }));
    }

    private void ensureStudentNote(
            NoteRepository noteRepository,
            Evaluation evaluation,
            Etudiant student,
            StudentDemoSession demoSession
    ) {
        Note note = noteRepository.findByEvaluationId(evaluation.getId()).stream()
                .filter(existingNote -> existingNote.getEtudiant().getId().equals(student.getId()))
                .findFirst()
                .orElseGet(Note::new);

        note.setEvaluation(evaluation);
        note.setEtudiant(student);
        note.setValeur(demoSession.grade());
        note.setStatut(demoSession.gradeStatus());
        note.setRemarque(demoSession.remark());
        noteRepository.save(note);
    }

    private void ensureStudentMajorEvaluationNotes(
            SeanceRepository seanceRepository,
            EvaluationRepository evaluationRepository,
            NoteRepository noteRepository,
            Etudiant student,
            Groupe group
    ) {
        List<Evaluation> majorEvaluations = seanceRepository.findByGroupeId(group.getId()).stream()
                .flatMap(seance -> evaluationRepository.findBySeanceId(seance.getId()).stream())
                .filter(this::isMajorEvaluation)
                .toList();

        for (int index = 0; index < majorEvaluations.size(); index++) {
            Evaluation evaluation = majorEvaluations.get(index);
            boolean alreadyGraded = noteRepository.findByEvaluationId(evaluation.getId()).stream()
                    .anyMatch(note -> note.getEtudiant().getId().equals(student.getId()));

            if (alreadyGraded) {
                continue;
            }

            Note note = new Note();
            note.setEvaluation(evaluation);
            note.setEtudiant(student);
            note.setValeur(12.5F + (index % 5));
            note.setStatut("Valide");
            note.setRemarque("Note ajoutee pour completer le calcul de la moyenne.");
            noteRepository.save(note);
        }
    }

    private boolean isMajorEvaluation(Evaluation evaluation) {
        return isMajorEvaluationType(evaluation.getTypeEvaluation());
    }

    private void ensureStudentEvaluationPlan(
            SeanceRepository seanceRepository,
            EvaluationRepository evaluationRepository,
            List<Enseignement> teachings,
            Groupe group
    ) {
        for (Enseignement teaching : teachings) {
            List<Seance> teachingSessions = seanceRepository.findByGroupeId(group.getId()).stream()
                    .filter(seance -> seance.getEnseignement().getId().equals(teaching.getId()))
                    .toList();
            if (teachingSessions.isEmpty()) {
                continue;
            }

            Seance referenceSession = teachingSessions.get(0);
            boolean hasTp = teachingSessions.stream()
                    .anyMatch(seance -> "TP".equalsIgnoreCase(seance.getTypeSeance()));
            ensureStudentEvaluation(
                    evaluationRepository,
                    referenceSession,
                    "DS " + teaching.getMatiere().getLibelle(),
                    "DS",
                    1
            );
            ensureStudentEvaluation(
                    evaluationRepository,
                    referenceSession,
                    "Examen " + teaching.getMatiere().getLibelle(),
                    "Examen",
                    2
            );

            if (hasTp) {
                Seance tpSession = teachingSessions.stream()
                        .filter(seance -> "TP".equalsIgnoreCase(seance.getTypeSeance()))
                        .findFirst()
                        .orElse(referenceSession);
                ensureStudentEvaluation(
                        evaluationRepository,
                        tpSession,
                        "Examen TP " + teaching.getMatiere().getLibelle(),
                        "Examen TP",
                        3
                );
            }
        }
    }

    private boolean isMajorEvaluationType(String type) {
        try {
            normalizeMajorEvaluationType(type);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private String normalizeMajorEvaluationType(String type) {
        String normalized = type == null ? "" : type.trim().toLowerCase();
        if (normalized.equals("ds") || normalized.contains("devoir surveille")) {
            return "DS";
        }
        if (normalized.equals("examen tp") || normalized.equals("exam tp") || normalized.equals("tp")) {
            return "Examen TP";
        }
        if (normalized.equals("examen") || normalized.equals("exam")) {
            return "Examen";
        }

        throw new IllegalArgumentException("Type evaluation invalide");
    }

    private float seedEvaluationCoefficient(String type, Seance seance) {
        String normalizedType = normalizeMajorEvaluationType(type);
        boolean hasTp = "Examen TP".equals(normalizedType)
                || "TP".equalsIgnoreCase(seance.getTypeSeance());

        if ("DS".equals(normalizedType) || "Examen TP".equals(normalizedType)) {
            return 2.0F;
        }

        return hasTp ? 6.0F : 8.0F;
    }

    private void ensureStudentPresence(
            PresenceRepository presenceRepository,
            Seance seance,
            Etudiant student,
            StudentDemoSession demoSession,
            int index
    ) {
        Presence presence = presenceRepository.findBySeanceId(seance.getId()).stream()
                .filter(existingPresence -> existingPresence.getEtudiant().getId().equals(student.getId()))
                .findFirst()
                .orElseGet(Presence::new);

        presence.setSeance(seance);
        presence.setEtudiant(student);
        presence.setStatut(demoSession.presenceStatus());
        presence.setDateSaisie(LocalDateTime.now()
                .minusDays(6L - index)
                .withHour(seance.getHeureDebut().getHour())
                .withMinute(seance.getHeureDebut().getMinute())
                .withSecond(0)
                .withNano(0));
        presenceRepository.save(presence);
    }

    private void ensureStudentSupport(
            SupportCoursRepository supportCoursRepository,
            Enseignement teaching,
            String title,
            String fileName,
            String content
    ) throws IOException {
        SupportCours supportCours = supportCoursRepository.findByEnseignementId(teaching.getId()).stream()
                .filter(existingSupport -> existingSupport.getTitre().equalsIgnoreCase(title))
                .findFirst()
                .orElseGet(SupportCours::new);
        Path uploadDirectory = Path.of("uploads", "course-materials").toAbsolutePath().normalize();
        Files.createDirectories(uploadDirectory);
        Path supportPath = uploadDirectory.resolve(fileName).normalize();

        if (!supportPath.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("Chemin de support invalide");
        }

        Files.writeString(supportPath, content, StandardCharsets.UTF_8);
        supportCours.setTitre(title);
        supportCours.setCheminFichier(fileName);
        supportCours.setNomFichierOriginal(fileName);
        supportCours.setTypeFichier("text/plain");
        supportCours.setTailleOctets(Files.size(supportPath));
        supportCours.setDateDepot(LocalDateTime.now().minusDays(2));
        supportCours.setEnseignement(teaching);
        supportCoursRepository.save(supportCours);
    }

    private void ensureStudentAnnouncement(
            AnnonceRepository annonceRepository,
            Administrateur admin,
            String title,
            String content
    ) {
        Annonce annonce = annonceRepository.findAll().stream()
                .filter(existingAnnonce -> existingAnnonce.getTitre().equalsIgnoreCase(title))
                .findFirst()
                .orElseGet(Annonce::new);

        annonce.setTitre(title);
        annonce.setContenu(content);
        annonce.setDatePublication(LocalDateTime.now().minusDays(1));
        annonce.setDateExpiration(LocalDateTime.now().plusDays(21));
        annonce.setCibleGlobale(false);
        annonce.setCibleRole("STUDENT");
        annonce.setAdministrateur(admin);
        annonceRepository.save(annonce);
    }

    private record SeedAcademicData(List<Matiere> subjects, List<Groupe> groups, Groupe gi3a) {
    }

    private record TimeSlot(LocalTime start, LocalTime end, String type) {
    }

    private record StudentDemoSession(
            int subjectIndex,
            String day,
            LocalTime start,
            LocalTime end,
            String type,
            String building,
            String room,
            String evaluationLabel,
            String evaluationType,
            Float grade,
            String gradeStatus,
            String remark,
            String presenceStatus
    ) {
    }
}
