# 📮 Guide Postman - Student Management API

## 🚀 Import Collection dans Postman

### Étape 1: Ouvrir Postman
1. Télécharger Postman: https://www.postman.com/downloads/
2. Installer et ouvrir l'application

### Étape 2: Importer la Collection
**Méthode 1: Via File**
1. Cliquer sur **File** → **Import**
2. Sélectionner le fichier: `Postman_Collection.json`
3. Cliquer **Import**

**Méthode 2: Via le texte**
1. Cliquer sur **Import**
2. Aller à l'onglet **Raw text**
3. Copier/coller le contenu de `Postman_Collection.json`
4. Cliquer **Continue** → **Import**

### Étape 3: Configurer l'Environnement
La collection inclut déjà les variables:
- `{{base_url}}` = http://localhost:8080
- `{{jwt_token}}` = Token JWT (auto-sauvegardé)
- `{{user_id}}` = ID utilisateur
- `{{user_email}}` = Email utilisateur

---

## 📋 Workflow de Test

### 1️⃣ Vérifier que le serveur est actif
```
GET /api/health
```
Réponse attendue: `status: UP`

### 2️⃣ S'inscrire et obtenir un token
```
POST /api/auth/register
```
Body:
```json
{
  "nomComplet": "Mohammed Aziz",
  "email": "aziz@student.edu",
  "password": "SecurePass123!",
  "userType": "ETUDIANT",
  "matricule": "ETU001",
  "niveau": "L3"
}
```

✅ **Le token JWT est sauvegardé automatiquement** dans `{{jwt_token}}`

### 3️⃣ Accéder aux endpoints protégés
Tous les endpoints avec `(Protected)` utilisent automatiquement:
```
Authorization: Bearer {{jwt_token}}
```

Exemples:
```
GET /api/etudiants
GET /api/professeurs
GET /api/departements
POST /api/groupes
```

---

## 🔄 Workflow Alternatif avec Login

### Si vous avez déjà un compte
1. Utiliser: **Login** au lieu de **Register**
   ```
   POST /api/auth/login
   ```
   Body:
   ```json
   {
     "email": "aziz@student.edu",
     "password": "SecurePass123!"
   }
   ```

2. Le token est automatiquement sauvegardé
3. Utiliser les endpoints protégés

---

## 🧪 Endpoints Disponibles

### ✅ Public (Sans Token)
- `GET /` - Accueil
- `GET /api/health` - Santé app
- `GET /api/health/info` - Info app
- `POST /api/auth/login` - Se connecter
- `POST /api/auth/register` - S'inscrire
- `POST /api/auth/validate` - Valider token
- `POST /api/auth/logout` - Se déconnecter

### 🔐 Protégés (Token requis)
Tous les endpoints `/api/*` sauf ceux listés ci-dessus

Exemples:
- `GET /api/etudiants` - Lister étudiants
- `GET /api/professeurs` - Lister professeurs
- `GET /api/departements` - Lister départements
- `GET /api/groupes` - Lister groupes
- `GET /api/matieres` - Lister matières
- `POST /api/etudiants` - Créer étudiant
- `PUT /api/etudiants/{id}` - Modifier étudiant
- `DELETE /api/etudiants/{id}` - Supprimer étudiant

---

## 💡 Tips Postman

### 1. Auto-save Token
La collection inclut des **tests** qui sauvegardent automatiquement:
- Le token JWT dans `{{jwt_token}}`
- L'ID utilisateur dans `{{user_id}}`
- L'email dans `{{user_email}}`

### 2. Réutiliser les Variables
Utilisez `{{jwt_token}}` dans le header Authorization:
```
Authorization: Bearer {{jwt_token}}
```

### 3. Visualiser les Réponses
Postman affiche automatiquement:
- Status Code (200, 401, 500, etc.)
- Headers
- Body (JSON, XML, etc.)
- Temps de réponse

### 4. Tests Automatiques
Cliquez sur l'onglet **Tests** pour voir les tests:
```javascript
// Exemple: Valider le statut
pm.test("Status is 200", function () {
    pm.response.to.have.status(200);
});
```

---

## 🔐 Authentification JWT

### Header Authorization
```
Key: Authorization
Value: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### Token Structure
```
Header.Payload.Signature

{
  "alg": "HS512",
  "typ": "JWT"
}.{
  "sub": "user@email.com",
  "userId": 1,
  "userType": "ETUDIANT",
  "iat": 1712876000,
  "exp": 1712962400
}
```

---

## 🧪 Cas de Test

### Test 1: Registration Flow
1. POST `/api/auth/register`
2. Vérifier: Token retourné + Status 201
3. GET `/api/etudiants` avec token
4. Vérifier: Liste des étudiants retournée

### Test 2: Login Flow
1. POST `/api/auth/login`
2. Vérifier: Token retourné + Status 200
3. POST `/api/auth/validate`
4. Vérifier: Token valide

### Test 3: Protected Endpoint
1. GET `/api/etudiants` SANS token
2. Vérifier: Status 401 (Unauthorized)
3. GET `/api/etudiants` AVEC token
4. Vérifier: Status 200 + Data

### Test 4: Create Resource
1. POST `/api/groupes` avec token
2. Body: `{"libelle": "L3-INFO-B", "niveau": "L3", ...}`
3. Vérifier: Groupe créé + Status 201

---

## 📊 Variables d'Environnement

| Variable | Valeur Par Défaut | Modifiable |
|----------|-------------------|-----------|
| `base_url` | http://localhost:8080 | ✅ Oui |
| `jwt_token` | (vide) | Auto (Login/Register) |
| `user_id` | (vide) | Auto (Login/Register) |
| `user_email` | (vide) | Auto (Login/Register) |

**Pour modifier:**
1. Cliquer sur l'icône "Environnement" (en haut à droite)
2. Sélectionner "Manage Environments"
3. Éditer la valeur

---

## ❌ Erreurs Courantes

### 401 Unauthorized
**Cause:** Token manquant ou invalide
**Solution:** 
1. Vérifier que vous avez d'abord appelé `/api/auth/login` ou `/register`
2. Vérifier que le token est copié dans `{{jwt_token}}`
3. Vérifier que le header est: `Authorization: Bearer {{jwt_token}}`

### 500 Internal Server Error
**Cause:** Erreur serveur
**Solution:**
1. Vérifier que le serveur fonctionne: `GET /api/health`
2. Vérifier les logs du serveur
3. Redémarrer le serveur

### 404 Not Found
**Cause:** Endpoint n'existe pas ou ID invalide
**Solution:**
1. Vérifier le URL
2. Vérifier que l'endpoint existe dans la collection

### 400 Bad Request
**Cause:** Données invalides
**Solution:**
1. Vérifier le format JSON
2. Vérifier les champs requis
3. Vérifier les types (string, int, etc.)

---

## 🎯 Checklist de Test

- [ ] Serveur démarre sans erreurs
- [ ] `GET /api/health` retourne UP
- [ ] `POST /api/auth/register` crée un utilisateur
- [ ] Token JWT est sauvegardé
- [ ] `GET /api/etudiants` avec token retourne les données
- [ ] `GET /api/etudiants` sans token retourne 401
- [ ] Endpoints protégés demandent le token
- [ ] `POST /api/auth/logout` déconnecte l'utilisateur

---

## 📞 Support

**Collection créée automatiquement** avec:
- ✅ Tous les endpoints JWT
- ✅ Endpoints protégés
- ✅ Auto-save du token
- ✅ Variables d'environnement
- ✅ Tests automatiques
- ✅ Exemples de requêtes/réponses

Pour plus d'informations:
- Voir: `JWT_IMPLEMENTATION.md`
- Voir: `API_DOCUMENTATION.md`
- Voir: `QUICK_START.md`

---

**Importez la collection et commencez à tester!** 🚀

