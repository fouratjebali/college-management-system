# Student Management System - Backend

## Description
Système de gestion des étudiants - Backend basé sur Spring Boot avec une base de données PostgreSQL.

## Architecture
- **Framework**: Spring Boot 4.0.5
- **Langage**: Java 21
- **Base de données**: PostgreSQL
- **ORM**: Hibernate/JPA
- **Sécurité**: Spring Security

## Structure du projet

### Modèles (Model)
- **User** (classe parent)
  - Administrateur
  - Etudiant
  - Professeur
- **Departement**
- **Groupe**
- **Matiere**
- **Enseignement**
- **Seance**
- **Evaluation**
- **Note**
- **Presence**
- **Annonce**
- **SupportCours**

### Repositories
Interfaces JPA pour accéder à la base de données avec des méthodes de requête personnalisées.

### Services
Couche métier contenant la logique applicative pour chaque entité.

### Contrôleurs (Controllers)
Points d'entrée REST API pour toutes les opérations CRUD.

## Installation et démarrage

### Prérequis
- Java 21 ou plus
- Maven 3.8.0 ou plus
- PostgreSQL 12 ou plus
- Git

### Configuration

1. **Cloner le projet**
```bash
git clone <repository>
cd Backend
```

2. **Configurer la base de données**
   - Créer une base de données PostgreSQL nommée `miniprojet_db`
   - Modifier `application.properties` si nécessaire

3. **Modifier `application.properties`**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/miniprojet_db
spring.datasource.username=postgres
spring.datasource.password=votre_mot_de_passe
```

4. **Compiler le projet**
```bash
mvn clean install
```

5. **Démarrer l'application**
```bash
mvn spring-boot:run
```

L'application sera disponible sur: `http://localhost:8080`

## Endpoints API

### Utilisateurs
- `GET /api/users` - Lister tous les utilisateurs
- `GET /api/users/{id}` - Obtenir un utilisateur
- `GET /api/users/email/{email}` - Chercher par email
- `POST /api/users` - Créer un utilisateur
- `PUT /api/users/{id}` - Modifier un utilisateur
- `DELETE /api/users/{id}` - Supprimer un utilisateur

### Administrateurs
- `GET /api/administrateurs` - Lister tous les administrateurs
- `GET /api/administrateurs/{id}` - Obtenir un administrateur
- `GET /api/administrateurs/matricule/{matricule}` - Chercher par matricule
- `POST /api/administrateurs` - Créer un administrateur
- `PUT /api/administrateurs/{id}` - Modifier un administrateur
- `DELETE /api/administrateurs/{id}` - Supprimer un administrateur

### Étudiants
- `GET /api/etudiants` - Lister tous les étudiants
- `GET /api/etudiants/{id}` - Obtenir un étudiant
- `GET /api/etudiants/matricule/{matricule}` - Chercher par matricule
- `GET /api/etudiants/groupe/{groupeId}` - Lister les étudiants d'un groupe
- `POST /api/etudiants` - Créer un étudiant
- `PUT /api/etudiants/{id}` - Modifier un étudiant
- `DELETE /api/etudiants/{id}` - Supprimer un étudiant

### Professeurs
- `GET /api/professeurs` - Lister tous les professeurs
- `GET /api/professeurs/{id}` - Obtenir un professeur
- `GET /api/professeurs/matricule/{matricule}` - Chercher par matricule
- `POST /api/professeurs` - Créer un professeur
- `PUT /api/professeurs/{id}` - Modifier un professeur
- `DELETE /api/professeurs/{id}` - Supprimer un professeur

### Départements
- `GET /api/departements` - Lister tous les départements
- `GET /api/departements/{id}` - Obtenir un département
- `POST /api/departements` - Créer un département
- `PUT /api/departements/{id}` - Modifier un département
- `DELETE /api/departements/{id}` - Supprimer un département

### Groupes
- `GET /api/groupes` - Lister tous les groupes
- `GET /api/groupes/{id}` - Obtenir un groupe
- `GET /api/groupes/departement/{departementId}` - Lister les groupes d'un département
- `POST /api/groupes` - Créer un groupe
- `PUT /api/groupes/{id}` - Modifier un groupe
- `DELETE /api/groupes/{id}` - Supprimer un groupe

### Matières
- `GET /api/matieres` - Lister toutes les matières
- `GET /api/matieres/{id}` - Obtenir une matière
- `GET /api/matieres/departement/{departementId}` - Lister les matières d'un département
- `POST /api/matieres` - Créer une matière
- `PUT /api/matieres/{id}` - Modifier une matière
- `DELETE /api/matieres/{id}` - Supprimer une matière

### Enseignements
- `GET /api/enseignements` - Lister tous les enseignements
- `GET /api/enseignements/{id}` - Obtenir un enseignement
- `GET /api/enseignements/professeur/{professeurId}` - Par professeur
- `GET /api/enseignements/matiere/{matiereId}` - Par matière
- `POST /api/enseignements` - Créer un enseignement
- `PUT /api/enseignements/{id}` - Modifier un enseignement
- `DELETE /api/enseignements/{id}` - Supprimer un enseignement

### Séances
- `GET /api/seances` - Lister toutes les séances
- `GET /api/seances/{id}` - Obtenir une séance
- `GET /api/seances/enseignement/{enseignementId}` - Par enseignement
- `GET /api/seances/groupe/{groupeId}` - Par groupe
- `POST /api/seances` - Créer une séance
- `PUT /api/seances/{id}` - Modifier une séance
- `DELETE /api/seances/{id}` - Supprimer une séance

### Évaluations
- `GET /api/evaluations` - Lister toutes les évaluations
- `GET /api/evaluations/{id}` - Obtenir une évaluation
- `GET /api/evaluations/seance/{seanceId}` - Par séance
- `POST /api/evaluations` - Créer une évaluation
- `PUT /api/evaluations/{id}` - Modifier une évaluation
- `DELETE /api/evaluations/{id}` - Supprimer une évaluation

### Notes
- `GET /api/notes` - Lister toutes les notes
- `GET /api/notes/{id}` - Obtenir une note
- `GET /api/notes/evaluation/{evaluationId}` - Par évaluation
- `GET /api/notes/etudiant/{etudiantId}` - Par étudiant
- `POST /api/notes` - Créer une note
- `PUT /api/notes/{id}` - Modifier une note
- `DELETE /api/notes/{id}` - Supprimer une note

### Présences
- `GET /api/presences` - Lister toutes les présences
- `GET /api/presences/{id}` - Obtenir une présence
- `GET /api/presences/etudiant/{etudiantId}` - Par étudiant
- `GET /api/presences/seance/{seanceId}` - Par séance
- `POST /api/presences` - Créer une présence
- `PUT /api/presences/{id}` - Modifier une présence
- `DELETE /api/presences/{id}` - Supprimer une présence

### Annonces
- `GET /api/annonces` - Lister toutes les annonces
- `GET /api/annonces/{id}` - Obtenir une annonce
- `GET /api/annonces/administrateur/{administrateurId}` - Par administrateur
- `GET /api/annonces/global` - Annonces globales
- `POST /api/annonces` - Créer une annonce
- `PUT /api/annonces/{id}` - Modifier une annonce
- `DELETE /api/annonces/{id}` - Supprimer une annonce

### Support de cours
- `GET /api/supports` - Lister tous les supports
- `GET /api/supports/{id}` - Obtenir un support
- `GET /api/supports/enseignement/{enseignementId}` - Par enseignement
- `POST /api/supports` - Créer un support
- `PUT /api/supports/{id}` - Modifier un support
- `DELETE /api/supports/{id}` - Supprimer un support

## Utilisation avec Docker

### Démarrer avec Docker Compose
```bash
docker-compose up -d
```

Cela démarrera:
- PostgreSQL sur le port 5432
- Backend Spring Boot sur le port 8080

## Conventions de codage

- Utilisation de Lombok pour réduire le code passe-plat
- Utilisation de @Data pour les getters/setters automatiques
- Utilisation de @Repository, @Service, @RestController pour la dépendance injection
- Nommage en camelCase pour les variables et méthodes
- Nommage en PascalCase pour les classes

## Améliorations futures

- Ajouter l'authentification JWT
- Ajouter la validation des entrées
- Ajouter les tests unitaires
- Ajouter la pagination
- Ajouter le tri
- Ajouter la filtration
- Documenter l'API avec Swagger/OpenAPI

## Support

Pour toute question ou problème, veuillez contacter l'équipe de développement.

