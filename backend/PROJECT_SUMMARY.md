# Résumé du Projet - Système de Gestion des Étudiants

## 📋 Vue d'ensemble
Ce projet implémente un système complet de gestion des étudiants construit avec Spring Boot et PostgreSQL.

## 🗂️ Structure du Projet

### Modèles (Entités JPA)
- **User** (classe parent avec héritage)
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

### Couches de l'Application

#### 1. **Model Layer** (src/main/java/.../Model/)
Entités JPA représentant la structure de la base de données avec relations appropriées.

#### 2. **Repository Layer** (src/main/java/.../Repository/)
Interfaces Spring Data JPA pour accéder aux données:
- UserRepository
- AdministrateurRepository
- EtudiantRepository
- ProfesseurRepository
- DepartementRepository
- GroupeRepository
- MatiereRepository
- EnseignementRepository
- SeanceRepository
- EvaluationRepository
- NoteRepository
- PresenceRepository
- AnnonceRepository
- SupportCoursRepository

#### 3. **Service Layer** (src/main/java/.../Service/)
Logique métier pour chaque entité (CRUD + opérations spécifiques)

#### 4. **Controller Layer** (src/main/java/.../Controller/)
Endpoints REST API organisés par ressource:
- UserController
- AdministrateurController
- EtudiantController
- ProfesseurController
- DepartementController
- GroupeController
- MatiereController
- EnseignementController
- SeanceController
- EvaluationController
- NoteController
- PresenceController
- AnnonceController
- SupportCoursController
- HealthController

#### 5. **Config Layer** (src/main/java/.../Config/)
- SecurityConfig: Configuration de Spring Security et CORS
- WebMvcConfig: Configuration MVC supplémentaire

#### 6. **DTO Layer** (src/main/java/.../DTO/)
Data Transfer Objects pour les requêtes/réponses API

#### 7. **Exception Layer** (src/main/java/.../Exception/)
GlobalExceptionHandler pour la gestion centralisée des erreurs

#### 8. **Utils** (src/main/java/.../Utils/)
Utilitaires:
- PasswordUtils: Hachage de mots de passe
- DateUtils: Gestion des dates

## 🚀 Démarrage Rapide

### Prérequis
- Java 17+
- Maven 3.8.0+
- PostgreSQL 12+

### Installation
```bash
cd Backend
mvn clean install
```

### Configuration BD
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/miniprojet_db
spring.datasource.username=postgres
spring.datasource.password=password
```

### Lancement
```bash
# Option 1: Maven
mvn spring-boot:run

# Option 2: Avec Docker
docker-compose up -d

# Option 3: Script Windows
start.bat

# Option 4: Script Linux/Mac
./start.sh
```

L'application sera disponible sur: `http://localhost:8080`

## 🔌 Endpoints API Principaux

### Santé de l'application
- `GET /api/health` - Vérifier le statut du serveur
- `GET /api/health/info` - Informations sur l'application

### Gestion des Utilisateurs
- `GET /api/users` - Lister tous les utilisateurs
- `POST /api/users` - Créer un utilisateur
- `PUT /api/users/{id}` - Modifier
- `DELETE /api/users/{id}` - Supprimer

### Gestion des Étudiants
- `GET /api/etudiants` - Lister tous
- `GET /api/etudiants/{id}` - Détails
- `GET /api/etudiants/matricule/{matricule}` - Par matricule
- `GET /api/etudiants/groupe/{groupeId}` - Par groupe
- `POST /api/etudiants` - Créer
- `PUT /api/etudiants/{id}` - Modifier
- `DELETE /api/etudiants/{id}` - Supprimer

### Gestion des Professeurs
- `GET /api/professeurs` - Lister tous
- `GET /api/professeurs/{id}` - Détails
- `GET /api/professeurs/matricule/{matricule}` - Par matricule
- `POST /api/professeurs` - Créer
- `PUT /api/professeurs/{id}` - Modifier
- `DELETE /api/professeurs/{id}` - Supprimer

### Et plus...
Consultez `API_DOCUMENTATION.md` pour la liste complète des endpoints.

## 📦 Dépendances Principales
```xml
- Spring Boot 4.0.5
- Spring Data JPA
- Spring Security
- PostgreSQL Driver
- Lombok
- Jackson Databind
```

## 🗄️ Architecture de Base de Données

### Héritage JPA
Les utilisateurs utilisent une stratégie d'héritage JOINED:
- Colonne `dtype` pour identifier le type
- Tables séparées pour chaque sous-type

### Relations
- One-to-Many: Departement → Groupe, Matiere
- Many-to-One: Etudiant → Groupe, Seance → Groupe
- One-to-Many: Professeur → Enseignement
- Many-to-Many (implicite): Groupe ↔ Seance

## 🔒 Sécurité
- CORS activé pour localhost:4200, localhost:3000, localhost:8080
- CSRF désactivé (configuration pour dev)
- Session sans état (STATELESS)
- À implémenter: JWT, OAuth2, BCrypt pour les mots de passe

## 📝 Convention de Code
- Utilisation de Lombok (@Data, @Service, etc.)
- Nommage en camelCase pour les variables
- Nommage en PascalCase pour les classes
- Annotations @RestController, @Repository, @Service

## 🧪 Tests
```bash
mvn test
```

## 📊 Fichiers Importants
- `pom.xml`: Dépendances Maven
- `application.properties`: Configuration
- `docker-compose.yml`: Configuration Docker
- `Dockerfile`: Image Docker
- `init-data.sql`: Script d'initialisation

## 🎯 Prochaines Étapes
- [ ] Ajouter l'authentification JWT
- [ ] Ajouter les validations avec @Valid
- [ ] Ajouter la pagination et le tri
- [ ] Documenter avec Swagger/OpenAPI
- [ ] Ajouter les tests unitaires
- [ ] Ajouter les logs structurés
- [ ] Optimiser les requêtes BD
- [ ] Ajouter la mise en cache

## 👥 Contribution
Pour toute question ou contribution, veuillez contacter l'équipe de développement.

## 📄 License
Propriétaire - Tous droits réservés

