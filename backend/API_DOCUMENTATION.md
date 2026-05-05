# API Collection for Student Management System

Cette collection contient tous les endpoints API du système de gestion des étudiants.

## Base URL
```
http://localhost:8080/api
```

## Variables d'environnement (à configurer)
- `base_url`: http://localhost:8080/api
- `admin_id`: ID d'un administrateur
- `etudiant_id`: ID d'un étudiant
- `professeur_id`: ID d'un professeur
- `departement_id`: ID d'un département
- `groupe_id`: ID d'un groupe
- `matiere_id`: ID d'une matière
- `enseignement_id`: ID d'un enseignement
- `seance_id`: ID d'une séance

## Exemples de requêtes

### Créer un administrateur
```json
POST /api/administrateurs
{
  "nomComplet": "Ahmed Ben Ali",
  "email": "admin@university.edu",
  "motDePasseHash": "hash_password",
  "actif": true,
  "matriculeAdmin": "ADM001",
  "fonction": "Directeur Académique"
}
```

### Créer un étudiant
```json
POST /api/etudiants
{
  "nomComplet": "Mohammed Aziz",
  "email": "aziz@student.edu",
  "motDePasseHash": "hash_password",
  "actif": true,
  "matricule": "ETU001",
  "niveau": "L3",
  "groupeId": 1
}
```

### Créer un professeur
```json
POST /api/professeurs
{
  "nomComplet": "Dr. Fatima Belkhir",
  "email": "fatima@university.edu",
  "motDePasseHash": "hash_password",
  "actif": true,
  "matriculePro": "PROF001",
  "grade": "Maître de Conférences"
}
```

### Créer un département
```json
POST /api/departements
{
  "nom": "Informatique"
}
```

### Créer un groupe
```json
POST /api/groupes
{
  "libelle": "L3-INFO-A",
  "niveau": "L3",
  "anneeUniversitaire": "2025-2026",
  "departementId": 1
}
```

### Créer une matière
```json
POST /api/matieres
{
  "code": "INFO-301",
  "libelle": "Algorithmes Avancés",
  "coefficient": 3.0,
  "departementId": 1
}
```

### Créer un enseignement
```json
POST /api/enseignements
{
  "semestre": 1,
  "anneeUniversitaire": "2025-2026",
  "professeurId": 1,
  "matiereId": 1
}
```

### Créer une séance
```json
POST /api/seances
{
  "typeSeance": "CM",
  "joursemaine": "Lundi",
  "heureDebut": "09:00:00",
  "heureFin": "11:00:00",
  "salle": "A101",
  "batiment": "Bâtiment A",
  "enseignementId": 1,
  "groupeId": 1
}
```

### Enregistrer une présence
```json
POST /api/presences
{
  "statut": "PRESENT",
  "dateSaisie": "2025-01-15T09:00:00",
  "etudiantId": 1,
  "seanceId": 1
}
```

### Créer une évaluation
```json
POST /api/evaluations
{
  "libelle": "Examen Partiel",
  "typeEvaluation": "EXAMEN",
  "dateEvaluation": "2025-01-20T14:00:00",
  "coefficient": 0.4,
  "seanceId": 1
}
```

### Enregistrer une note
```json
POST /api/notes
{
  "valeur": 15.5,
  "statut": "VALIDEE_PROF",
  "remarque": "Bon travail",
  "evaluationId": 1,
  "etudiantId": 1
}
```

### Créer une annonce
```json
POST /api/annonces
{
  "titre": "Réunion Importante",
  "contenu": "Une réunion aura lieu demain à 10h",
  "datePublication": "2025-01-15T08:00:00",
  "dateExpiration": "2025-01-20T23:59:59",
  "cibleGlobale": true,
  "administrateurId": 1
}
```

### Ajouter un support de cours
```json
POST /api/supports
{
  "titre": "Chapitre 1: Introduction aux Algorithmes",
  "cheminFichier": "/uploads/chapter1.pdf",
  "dateDepot": "2025-01-10T10:00:00",
  "enseignementId": 1
}
```

## Erreurs courantes

- **400 Bad Request**: Les données envoyées sont invalides
- **404 Not Found**: La ressource n'existe pas
- **500 Internal Server Error**: Erreur serveur

## Notes
- Tous les timestamps doivent être au format ISO 8601
- Les mots de passe doivent être hashés avant d'être envoyés
- Les IDs doivent être des entiers positifs

