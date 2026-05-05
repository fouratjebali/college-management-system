# 🚀 QUICK START GUIDE

## Démarrage rapide du système de gestion des étudiants

### 📋 Prérequis Minimums
- Java 17+
- Maven 3.8+  
- PostgreSQL 12+ (optionnel avec Docker)
- Git

### ⚡ Option 1: Démarrage Rapide avec Docker (Recommandé)

```bash
# Cloner le projet
git clone <repository-url>
cd Backend

# Démarrer les services
docker-compose up

# L'application sera accessible sur http://localhost:8080
```

**Avantages:**
- ✅ PostgreSQL inclus et configuré
- ✅ Aucune installation locale requise
- ✅ Isolé du système

### ⚡ Option 2: Démarrage Local avec Maven

#### 1. Préparer PostgreSQL
```bash
# Créer la base de données
createdb -U postgres miniprojet_db

# Ou avec Docker:
docker run --name miniprojet-db -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=miniprojet_db -p 5432:5432 -d postgres:15-alpine
```

#### 2. Compiler et Lancer
```bash
cd Backend

# Option A: Avec Maven (développement)
mvn clean spring-boot:run

# Option B: JAR standalone (production)
mvn clean package -DskipTests
java -jar target/Backend-0.0.1-SNAPSHOT.jar
```

### 📡 Vérifier que tout fonctionne

```bash
# Vérifier l'application
curl http://localhost:8080/api/health

# Response attendue:
{
  "status": "UP",
  "message": "Student Management System is running",
  "timestamp": 1234567890
}
```

### 🛠️ Configuration rapide (Optional)

Fichier: `src/main/resources/application.properties`

```properties
# Base de données (si pas Docker)
spring.datasource.url=jdbc:postgresql://localhost:5432/miniprojet_db
spring.datasource.username=postgres
spring.datasource.password=password

# Port de l'application
server.port=8080
```

## 📚 Endpoints Clés pour Tester

### 1️⃣ Santé de l'Application
```bash
GET http://localhost:8080/api/health
```

### 2️⃣ Créer un Administrateur
```bash
curl -X POST http://localhost:8080/api/administrateurs \
  -H "Content-Type: application/json" \
  -d '{
    "nomComplet": "Admin Test",
    "email": "admin@test.edu",
    "motDePasseHash": "hash123",
    "actif": true,
    "matriculeAdmin": "ADM001",
    "fonction": "Directeur"
  }'
```

### 3️⃣ Créer un Département
```bash
curl -X POST http://localhost:8080/api/departements \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Informatique"
  }'
```

### 4️⃣ Créer un Groupe
```bash
curl -X POST http://localhost:8080/api/groupes \
  -H "Content-Type: application/json" \
  -d '{
    "libelle": "L3-INFO-A",
    "niveau": "L3",
    "anneeUniversitaire": "2025-2026",
    "departementId": 1
  }'
```

### 5️⃣ Créer un Étudiant
```bash
curl -X POST http://localhost:8080/api/etudiants \
  -H "Content-Type: application/json" \
  -d '{
    "nomComplet": "Mohammed Aziz",
    "email": "aziz@student.edu",
    "motDePasseHash": "hash123",
    "actif": true,
    "matricule": "ETU001",
    "niveau": "L3",
    "groupeId": 1
  }'
```

### 6️⃣ Lister tous les étudiants
```bash
curl http://localhost:8080/api/etudiants
```

## 🔗 URLs Importantes

| Ressource | URL |
|-----------|-----|
| Santé | `http://localhost:8080/api/health` |
| Utilisateurs | `http://localhost:8080/api/users` |
| Administrateurs | `http://localhost:8080/api/administrateurs` |
| Étudiants | `http://localhost:8080/api/etudiants` |
| Professeurs | `http://localhost:8080/api/professeurs` |
| Départements | `http://localhost:8080/api/departements` |
| Groupes | `http://localhost:8080/api/groupes` |
| Matières | `http://localhost:8080/api/matieres` |
| Enseignements | `http://localhost:8080/api/enseignements` |
| Séances | `http://localhost:8080/api/seances` |
| Évaluations | `http://localhost:8080/api/evaluations` |
| Notes | `http://localhost:8080/api/notes` |
| Présences | `http://localhost:8080/api/presences` |
| Annonces | `http://localhost:8080/api/annonces` |
| Supports | `http://localhost:8080/api/supports` |

## 📊 Format des Dates

Toutes les dates doivent être au format ISO 8601:
```
2025-01-15T09:00:00
```

## 🆘 Troubleshooting

### ❌ Port 8080 déjà en utilisation
```bash
# Changer le port dans application.properties
server.port=8081

# Ou au démarrage:
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### ❌ Impossible de se connecter à PostgreSQL
```bash
# Vérifier que PostgreSQL fonctionne
psql -U postgres -d miniprojet_db

# Ou vérifier avec Docker:
docker logs miniprojet-db
```

### ❌ Erreur de compilation
```bash
# Nettoyer et reconstruire
mvn clean -DskipTests
mvn install
```

## 📖 Documentation Complète

- **README.md** - Documentation générale
- **API_DOCUMENTATION.md** - Référence complète des APIs
- **PROJECT_SUMMARY.md** - Vue d'ensemble du projet
- **FILES_CREATED.md** - Liste des fichiers créés

## 💡 Conseils

1. **Utiliser Postman ou Insomnia** pour tester les APIs
2. **Importer le fichier API_DOCUMENTATION.md** dans Postman
3. **Vérifier les logs** en cas d'erreur
4. **Créer un administrateur en premier** pour avoir accès à toutes les fonctionnalités

## ✅ Prochaines Étapes

1. Créer un administrateur
2. Créer un département
3. Créer des groupes
4. Créer des matières
5. Créer des professeurs
6. Créer des enseignements
7. Créer des séances
8. Ajouter des étudiants

## 🆘 Support

En cas de problème:
1. Vérifier les logs de l'application
2. Consulter la documentation
3. Vérifier la connexion BD
4. Vérifier les ports utilisés

---

**Bienvenue dans le système de gestion des étudiants! 🎓**

