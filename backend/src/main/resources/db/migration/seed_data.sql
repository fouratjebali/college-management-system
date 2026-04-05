INSERT INTO departements (nom) VALUES
('Informatique'),
('Mathématiques'),
('Physique'),
('Génie Civil');

  
INSERT INTO matieres (code, libelle, coefficient) VALUES
('INF101', 'Programmation Java', 2.0),
('INF102', 'Bases de Données', 2.0),
('INF103', 'Développement Web', 1.5),
('MATH101', 'Analyse Mathématique', 2.5),
('MATH102', 'Algèbre Linéaire', 2.0);

  
INSERT INTO departement_matiere (departement_id, matiere_id) VALUES
(1, 1), (1, 2), (1, 3),    
(2, 4), (2, 5);            

   
INSERT INTO groupes (libelle, niveau, annee_universitaire) VALUES
('L3 Info Groupe A', 'L3', '2024-2025'),
('L3 Info Groupe B', 'L3', '2024-2025'),
('M1 Info', 'M1', '2024-2025');

-- Insert Admin User (password: admin123)
INSERT INTO users (email, nom_complet, mot_de_passe_hash, role, user_type) VALUES
('admin@institut.tn', 'Administrateur Principal', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 'Administrateur');

INSERT INTO administrateurs (id, matricule_admin, fonction)
SELECT id, 'ADM001', 'Directeur des Études'
FROM users WHERE email = 'admin@institut.tn';

-- Insert Professor (password: prof123)
INSERT INTO users (email, nom_complet, mot_de_passe_hash, role, user_type) VALUES
('prof1@institut.tn', 'Dr. Ahmed Ben Ali', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'PROFESSOR', 'Professeur');

INSERT INTO professeurs (id, matricule_pro, grade)
SELECT id, 'PROF001', 'Maître de Conférences'
FROM users WHERE email = 'prof1@institut.tn';

-- Insert Students (password: student123)
INSERT INTO users (email, nom_complet, mot_de_passe_hash, role, user_type) VALUES
('etud1@institut.tn', 'Mohamed Trabelsi', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'STUDENT', 'Etudiant'),
('etud2@institut.tn', 'Fatma Slimani', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'STUDENT', 'Etudiant');

INSERT INTO etudiants (id, matricule, niveau, groupe_id)
SELECT u.id, 'ETU2024001', 'L3', 1
FROM users u WHERE u.email = 'etud1@institut.tn';

INSERT INTO etudiants (id, matricule, niveau, groupe_id)
SELECT u.id, 'ETU2024002', 'L3', 1
FROM users u WHERE u.email = 'etud2@institut.tn';