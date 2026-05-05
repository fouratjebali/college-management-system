# JWT Implementation Guide

## ✅ JWT Implémentation Complète

Votre application utilise maintenant **JWT (JSON Web Tokens)** pour l'authentification.

---

## 🔑 Features JWT

✅ **Token Generation** - Création de tokens JWT sécurisés  
✅ **Token Validation** - Validation automatique des tokens  
✅ **Password Hashing** - BCrypt pour les mots de passe  
✅ **Role-Based Access** - Support des rôles (Etudiant, Professeur, Admin)  
✅ **Stateless Auth** - Pas de sessions serveur  
✅ **Auto Filter** - Validation automatique des requêtes  

---

## 📋 Endpoints d'Authentification

### 1️⃣ Register (Créer un compte)
```bash
POST /api/auth/register
Content-Type: application/json

{
  "nomComplet": "Mohammed Aziz",
  "email": "aziz@student.edu",
  "password": "SecurePass123!",
  "userType": "ETUDIANT",
  "matricule": "ETU001",
  "niveau": "L3"
}

Response:
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "userId": 1,
  "email": "aziz@student.edu",
  "nomComplet": "Mohammed Aziz",
  "userType": "ETUDIANT"
}
```

### 2️⃣ Login (Se connecter)
```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "aziz@student.edu",
  "password": "SecurePass123!"
}

Response:
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "userId": 1,
  "email": "aziz@student.edu",
  "nomComplet": "Mohammed Aziz",
  "userType": "ETUDIANT"
}
```

### 3️⃣ Validate Token (Valider un token)
```bash
POST /api/auth/validate
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...

Response:
{
  "valid": true,
  "email": "aziz@student.edu"
}
```

### 4️⃣ Logout (Se déconnecter)
```bash
POST /api/auth/logout

Response:
{
  "message": "Logged out successfully. Please remove token from client."
}
```

---

## 🔐 Utiliser le Token JWT

### Header Format
```
Authorization: Bearer <your-jwt-token>
```

### Exemple avec curl
```bash
curl -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..." \
     http://localhost:8080/api/etudiants
```

### Exemple avec Postman
1. Aller à l'onglet "Authorization"
2. Sélectionner "Bearer Token"
3. Coller votre token dans le champ "Token"

### Exemple avec JavaScript/Fetch
```javascript
const token = "eyJhbGciOiJIUzUxMiJ9...";

fetch('http://localhost:8080/api/etudiants', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => console.log(data));
```

---

## 🔑 Configuration JWT

### Fichier: `application.properties`
```properties
# JWT Secret (change in production!)
jwt.secret=mySecretKeyForJwtTokenGenerationAndValidationPurposeOnly123456789

# Token expiration time (24 hours in milliseconds)
jwt.expiration=86400000
```

### ⚠️ IMPORTANT POUR LA PRODUCTION
```properties
# Change to strong secret key (32+ characters)
jwt.secret=CHANGE_ME_TO_A_STRONG_SECRET_KEY_WITH_MINIMUM_32_CHARACTERS_LENGTH_1234567890

# Adjust expiration as needed
jwt.expiration=3600000  # 1 hour
```

---

## 📦 Classes Clés Créées

### 1. **JwtTokenProvider** (`Security/JwtTokenProvider.java`)
- Génère les tokens JWT
- Valide les tokens
- Extrait les informations du token

### 2. **JwtAuthenticationFilter** (`Security/JwtAuthenticationFilter.java`)
- Filtre les requêtes HTTP
- Valide automatiquement les tokens
- Définit le contexte de sécurité

### 3. **AuthService** (`Service/AuthService.java`)
- Gère la logique de connexion/inscription
- Chiffre les mots de passe avec BCrypt
- Génère les tokens JWT

### 4. **AuthController** (`Controller/AuthController.java`)
- Endpoints: `/api/auth/login`, `/api/auth/register`, `/api/auth/validate`, `/api/auth/logout`

---

## 🔄 Flux d'Authentification

```
1. User envoie credentials (email + password)
   ↓
2. AuthService valide les credentials
   ↓
3. Mot de passe vérifié avec BCrypt
   ↓
4. Token JWT généré avec claims (userId, userType)
   ↓
5. Token renvoyé au client
   ↓
6. Client envoie token dans header Authorization
   ↓
7. JwtAuthenticationFilter valide le token
   ↓
8. Requête autorisée si token valide
```

---

## 🛡️ Claims du Token

Chaque token JWT contient:
```json
{
  "sub": "user@email.com",
  "userId": 1,
  "userType": "ETUDIANT",
  "iat": 1234567890,
  "exp": 1234654290
}
```

---

## ✅ Endpoints Protégés vs Public

### 🔓 Endpoints Publics (sans token requis)
```
POST   /api/auth/login
POST   /api/auth/register
POST   /api/auth/validate
POST   /api/auth/logout
GET    /api/health
GET    /api/health/info
```

### 🔐 Endpoints Protégés (token requis)
```
GET    /api/users
GET    /api/administrateurs
GET    /api/etudiants
GET    /api/professeurs
GET    /api/departements
GET    /api/groupes
GET    /api/matieres
...et tous les autres endpoints /api/*
```

---

## 🧪 Test avec Postman

### 1. Register
```
POST http://localhost:8080/api/auth/register
Body (JSON):
{
  "nomComplet": "Test User",
  "email": "test@test.com",
  "password": "Test123!",
  "userType": "ETUDIANT",
  "matricule": "TEST001",
  "niveau": "L3"
}
```

### 2. Copier le token de la réponse

### 3. Utiliser le token pour une requête
```
GET http://localhost:8080/api/etudiants
Headers:
  Authorization: Bearer <copier-token-ici>
```

---

## ⏱️ Durée d'Expiration

Par défaut: **24 heures** (86400000 ms)

Pour changer:
```properties
# 1 hour
jwt.expiration=3600000

# 7 days
jwt.expiration=604800000

# 30 days
jwt.expiration=2592000000
```

---

## 🚨 Erreurs Courantes

### "Invalid or missing JWT token"
- Le header Authorization n'est pas défini
- Le token ne commence pas par "Bearer "
- Le token est expiré

### "Token is invalid or expired"
- Le token a été modifié
- Le token a expiré (> 24h)
- Le secret JWT a changé

### "Invalid email or password"
- Email non trouvé
- Mot de passe incorrect
- Utilisateur est désactivé

---

## 🔒 Bonnes Pratiques

1. **Ne pas partager le secret JWT** - Gardez `jwt.secret` privé
2. **HTTPS en production** - Toujours utiliser HTTPS
3. **Renouveler les tokens** - Implémenter un refresh token pour l'expiration
4. **Logs d'audit** - Logger les authentifications
5. **Rate limiting** - Limiter les tentatives de login
6. **Token storage** - Client: HttpOnly cookies ou secure localStorage

---

## 📚 Dépendances Ajoutées

```xml
<!-- JWT (JJWT) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>

<!-- BCrypt for password encoding -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
```

---

## 🚀 Prochaines Étapes (Optionnel)

1. **Refresh Token** - Ajouter des refresh tokens
2. **Token Blacklist** - Implémenter une liste noire pour logout
3. **Rate Limiting** - Limiter les tentatives
4. **2FA** - Authentification à deux facteurs
5. **OAuth2** - Intégrer Google, GitHub login

---

## 📞 Support

Consultez:
- `API_DOCUMENTATION.md` - Référence API
- `README.md` - Documentation générale
- Code source: `Security/`, `Service/`, `Controller/`

---

**JWT est maintenant pleinement opérationnel! 🎉**

