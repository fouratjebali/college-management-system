-- Script d'initialisation de la base de données
-- À exécuter après le démarrage de l'application

-- Insérer des départements
INSERT INTO departement (nom) VALUES ('Informatique');
INSERT INTO departement (nom) VALUES ('Mathématiques');
INSERT INTO departement (nom) VALUES ('Physique');

-- Insérer des administrateurs
INSERT INTO utilisateur (nom_complet, email, mot_de_passe_hash, actif)
VALUES ('Admin Principal', 'admin@university.edu', 'admin_hash_123', true);

INSERT INTO administrateur (id, matricule_admin, fonction)
VALUES (1, 'ADM001', 'Directeur Académique');

-- Insérer des professeurs
INSERT INTO utilisateur (nom_complet, email, mot_de_passe_hash, actif)
VALUES ('Dr. Fatima Belkhir', 'fatima@university.edu', 'prof_hash_123', true);

INSERT INTO professeur (id, matricule_pro, grade)
VALUES (2, 'PROF001', 'Maître de Conférences');

INSERT INTO utilisateur (nom_complet, email, mot_de_passe_hash, actif)
VALUES ('Prof. Ahmed Saidi', 'ahmed@university.edu', 'prof_hash_456', true);

INSERT INTO professeur (id, matricule_pro, grade)
VALUES (3, 'PROF002', 'Professeur');

-- Insérer des groupes
INSERT INTO groupe (libelle, niveau, annee_universitaire, departement_id)
VALUES ('L3-INFO-A', 'L3', '2025-2026', 1);

INSERT INTO groupe (libelle, niveau, annee_universitaire, departement_id)
VALUES ('L3-INFO-B', 'L3', '2025-2026', 1);

-- Insérer des étudiants
INSERT INTO utilisateur (nom_complet, email, mot_de_passe_hash, actif)
VALUES ('Mohammed Aziz', 'aziz@student.edu', 'etudiant_hash_123', true);

INSERT INTO etudiant (id, matricule, niveau, groupe_id)
VALUES (4, 'ETU001', 'L3', 1);

INSERT INTO utilisateur (nom_complet, email, mot_de_passe_hash, actif)
VALUES ('Leila Ben Salah', 'leila@student.edu', 'etudiant_hash_456', true);

INSERT INTO etudiant (id, matricule, niveau, groupe_id)
VALUES (5, 'ETU002', 'L3', 1);

INSERT INTO utilisateur (nom_complet, email, mot_de_passe_hash, actif)
VALUES ('Karim Bouattaa', 'karim@student.edu', 'etudiant_hash_789', true);

INSERT INTO etudiant (id, matricule, niveau, groupe_id)
VALUES (6, 'ETU003', 'L3', 2);

-- Insérer des matières
INSERT INTO matiere (code, libelle, coefficient, departement_id)
VALUES ('INFO-301', 'Algorithmes Avancés', 3.0, 1);

INSERT INTO matiere (code, libelle, coefficient, departement_id)
VALUES ('INFO-302', 'Bases de Données', 3.5, 1);

INSERT INTO matiere (code, libelle, coefficient, departement_id)
VALUES ('INFO-303', 'Développement Web', 2.5, 1);

-- Insérer des enseignements
INSERT INTO enseignement (semestre, annee_universitaire, professeur_id, matiere_id)
VALUES (1, '2025-2026', 2, 1);

INSERT INTO enseignement (semestre, annee_universitaire, professeur_id, matiere_id)
VALUES (1, '2025-2026', 3, 2);

-- Insérer des séances
INSERT INTO seance (type_seance, joursemaine, heure_debut, heure_fin, salle, batiment, enseignement_id, groupe_id)
VALUES ('CM', 'Lundi', '09:00:00', '11:00:00', 'A101', 'Bâtiment A', 1, 1);

INSERT INTO seance (type_seance, joursemaine, heure_debut, heure_fin, salle, batiment, enseignement_id, groupe_id)
VALUES ('TD', 'Mercredi', '14:00:00', '16:00:00', 'A201', 'Bâtiment A', 1, 1);

INSERT INTO seance (type_seance, joursemaine, heure_debut, heure_fin, salle, batiment, enseignement_id, groupe_id)
VALUES ('CM', 'Mardi', '10:00:00', '12:00:00', 'B102', 'Bâtiment B', 2, 1);

-- Insérer des annonces
INSERT INTO annonce (titre, contenu, date_publication, date_expiration, cible_globale, administrateur_id)
VALUES ('Bienvenue', 'Bienvenue dans le système de gestion des étudiants', NOW(), NOW() + INTERVAL '30 days', true, 1);

