CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

  
CREATE TYPE role_type AS ENUM ('STUDENT', 'PROFESSOR', 'ADMIN');
CREATE TYPE type_seance AS ENUM ('COURS', 'TD', 'TP', 'RATTRAPAGE');
CREATE TYPE statut_presence AS ENUM ('PRESENT', 'ABSENT', 'RETARD');
CREATE TYPE type_evaluation AS ENUM ('CC', 'TP', 'EXAMEN');
CREATE TYPE statut_note AS ENUM ('BROUILLON', 'VALIDEE_PROF', 'VALIDEE_ADMIN', 'PUBLIEE');

  
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    nom_complet VARCHAR(255) NOT NULL,
    mot_de_passe_hash VARCHAR(255) NOT NULL,
    role role_type NOT NULL,
    actif BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_type VARCHAR(31) NOT NULL   
);

  
CREATE TABLE administrateurs (
    id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    matricule_admin VARCHAR(50) UNIQUE NOT NULL,
    fonction VARCHAR(100)
);

  
CREATE TABLE departements (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

  
CREATE TABLE matieres (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    libelle VARCHAR(255) NOT NULL,
    coefficient DECIMAL(3,2) NOT NULL CHECK (coefficient > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

  
CREATE TABLE departement_matiere (
    departement_id BIGINT REFERENCES departements(id) ON DELETE CASCADE,
    matiere_id BIGINT REFERENCES matieres(id) ON DELETE CASCADE,
    PRIMARY KEY (departement_id, matiere_id)
);

  
CREATE TABLE groupes (
    id BIGSERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL,
    niveau VARCHAR(50) NOT NULL,
    annee_universitaire VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (libelle, annee_universitaire)
);

  
CREATE TABLE etudiants (
    id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    matricule VARCHAR(50) UNIQUE NOT NULL,
    niveau VARCHAR(50) NOT NULL,
    groupe_id BIGINT REFERENCES groupes(id) ON DELETE SET NULL
);

  
CREATE TABLE professeurs (
    id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    matricule_pro VARCHAR(50) UNIQUE NOT NULL,
    grade VARCHAR(100)
);

  
CREATE TABLE enseignements (
    id BIGSERIAL PRIMARY KEY,
    matiere_id BIGINT REFERENCES matieres(id) ON DELETE CASCADE,
    professeur_id BIGINT REFERENCES professeurs(id) ON DELETE SET NULL,
    semestre INT NOT NULL CHECK (semestre IN (1, 2)),
    annee_universitaire VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (matiere_id, semestre, annee_universitaire)
);

  
CREATE TABLE groupe_enseignement (
    groupe_id BIGINT REFERENCES groupes(id) ON DELETE CASCADE,
    enseignement_id BIGINT REFERENCES enseignements(id) ON DELETE CASCADE,
    PRIMARY KEY (groupe_id, enseignement_id)
);

  
CREATE TABLE seances (
    id BIGSERIAL PRIMARY KEY,
    enseignement_id BIGINT REFERENCES enseignements(id) ON DELETE CASCADE,
    type_seance type_seance NOT NULL,
    jour_semaine VARCHAR(20) NOT NULL,
    heure_debut TIME NOT NULL,
    heure_fin TIME NOT NULL,
    salle VARCHAR(50),
    batiment VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

  
CREATE TABLE presences (
    id BIGSERIAL PRIMARY KEY,
    etudiant_id BIGINT REFERENCES etudiants(id) ON DELETE CASCADE,
    seance_id BIGINT REFERENCES seances(id) ON DELETE CASCADE,
    statut statut_presence NOT NULL,
    date_saisie TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remarque TEXT,
    UNIQUE (etudiant_id, seance_id)
);

  
CREATE TABLE evaluations (
    id BIGSERIAL PRIMARY KEY,
    enseignement_id BIGINT REFERENCES enseignements(id) ON DELETE CASCADE,
    libelle VARCHAR(255) NOT NULL,
    type_evaluation type_evaluation NOT NULL,
    date_evaluation TIMESTAMP NOT NULL,
    coefficient DECIMAL(3,2) NOT NULL CHECK (coefficient > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

  
CREATE TABLE notes (
    id BIGSERIAL PRIMARY KEY,
    evaluation_id BIGINT REFERENCES evaluations(id) ON DELETE CASCADE,
    etudiant_id BIGINT REFERENCES etudiants(id) ON DELETE CASCADE,
    valeur DECIMAL(5,2) CHECK (valeur >= 0 AND valeur <= 20),
    statut statut_note DEFAULT 'BROUILLON',
    remarque TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (evaluation_id, etudiant_id)
);

  
CREATE TABLE annonces (
    id BIGSERIAL PRIMARY KEY,
    administrateur_id BIGINT REFERENCES administrateurs(id) ON DELETE SET NULL,
    titre VARCHAR(255) NOT NULL,
    contenu TEXT NOT NULL,
    date_publication TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_expiration TIMESTAMP,
    cible_globale BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

  
CREATE TABLE annonce_groupe (
    annonce_id BIGINT REFERENCES annonces(id) ON DELETE CASCADE,
    groupe_id BIGINT REFERENCES groupes(id) ON DELETE CASCADE,
    PRIMARY KEY (annonce_id, groupe_id)
);

  
CREATE TABLE supports_de_cours (
    id BIGSERIAL PRIMARY KEY,
    enseignement_id BIGINT REFERENCES enseignements(id) ON DELETE CASCADE,
    titre VARCHAR(255) NOT NULL,
    chemin_fichier VARCHAR(500) NOT NULL,
    date_depot TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

  
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_etudiants_matricule ON etudiants(matricule);
CREATE INDEX idx_professeurs_matricule ON professeurs(matricule_pro);
CREATE INDEX idx_enseignements_annee ON enseignements(annee_universitaire);
CREATE INDEX idx_notes_statut ON notes(statut);
CREATE INDEX idx_presences_date ON presences(date_saisie);
CREATE INDEX idx_evaluations_date ON evaluations(date_evaluation);