✅ POSTMAN SETUP - COMPLETE GUIDE

================================================================================
FICHIERS CRÉÉS POUR POSTMAN
================================================================================

1. 📮 Postman_Collection.json
   └─ Fichier JSON à importer directement dans Postman
   └─ Contient:
      • 30+ endpoints pré-configurés
      • Toutes les requêtes JWT
      • Variables d'environnement
      • Tests automatiques
      • Exemples de body

2. 📖 POSTMAN_GUIDE.md
   └─ Guide complet d'utilisation
   └─ Incluant:
      • Import instructions
      • Workflow de test
      • Tips & tricks
      • Troubleshooting
      • Checklist

================================================================================
QUICK START
================================================================================

1. OUVRIR POSTMAN
   Télécharger: https://www.postman.com/downloads/

2. IMPORTER COLLECTION
   File → Import → Sélectionner: Postman_Collection.json

3. TESTER LE SERVEUR
   GET /api/health
   Response: {"status":"UP","message":"..."}

4. S'INSCRIRE (Auto-save token)
   POST /api/auth/register
   Body: {...credentials...}
   Response: {"token":"eyJhbGc...","userId":1,...}

5. ACCÉDER AUX DONNÉES PROTÉGÉES
   GET /api/etudiants
   Header: Authorization: Bearer {{jwt_token}}
   Response: [...liste d'étudiants...]

================================================================================
ENDPOINTS INCLUS
================================================================================

AUTHENTICATION (4)
  ✅ POST /api/auth/register       → S'inscrire + token
  ✅ POST /api/auth/login          → Se connecter + token
  ✅ POST /api/auth/validate       → Valider token
  ✅ POST /api/auth/logout         → Se déconnecter

HEALTH CHECK (3)
  ✅ GET /                          → Accueil
  ✅ GET /api/health               → Santé app
  ✅ GET /api/health/info          → Infos app

ETUDIANTS (3)
  ✅ GET /api/etudiants            → Lister
  ✅ GET /api/etudiants/{id}       → Par ID
  ✅ POST /api/etudiants           → Créer

PROFESSEURS (2)
  ✅ GET /api/professeurs          → Lister
  ✅ POST /api/professeurs         → Créer

DEPARTEMENTS (2)
  ✅ GET /api/departements         → Lister
  ✅ POST /api/departements        → Créer

GROUPES (2)
  ✅ GET /api/groupes              → Lister
  ✅ POST /api/groupes             → Créer

MATIERES (2)
  ✅ GET /api/matieres             → Lister
  ✅ POST /api/matieres            → Créer

TOTAL: 20+ Endpoints configurés

================================================================================
VARIABLES D'ENVIRONNEMENT
================================================================================

{{base_url}}          = http://localhost:8080
{{jwt_token}}         = Auto-sauvegardé après Login/Register
{{user_id}}           = Auto-sauvegardé après Login/Register
{{user_email}}        = Auto-sauvegardé après Login/Register

Les variables sont automatiquement mises à jour via les tests
quand vous appelez /api/auth/login ou /api/auth/register

================================================================================
FEATURES AUTOMATIQUES
================================================================================

✅ Token Auto-Save
   Après POST /api/auth/register ou /login
   Le token est auto-sauvegardé dans {{jwt_token}}

✅ Auto JWT Header
   Tous les endpoints protégés incluent automatiquement:
   Authorization: Bearer {{jwt_token}}

✅ Tests Automatiques
   Cliquer "Tests" sur chaque requête pour voir les validations

✅ Exemples de Body
   Chaque POST/PUT inclut un exemple de body JSON

================================================================================
WORKFLOW DE TEST RECOMMANDÉ
================================================================================

ÉTAPE 1: Vérifier que le serveur est actif
   GET /api/health
   ✓ Réponse: status = "UP"

ÉTAPE 2: S'inscrire (crée un compte + retourne token)
   POST /api/auth/register
   Body: {
     "nomComplet": "Test User",
     "email": "test@test.com",
     "password": "Test123!",
     "userType": "ETUDIANT",
     "matricule": "TEST001",
     "niveau": "L3"
   }
   ✓ Réponse: token sauvegardé automatiquement

ÉTAPE 3: Lister les étudiants
   GET /api/etudiants
   ✓ Header: Authorization auto-inclus
   ✓ Réponse: Liste d'étudiants

ÉTAPE 4: Créer un groupe
   POST /api/groupes
   Body: {
     "libelle": "L3-INFO-A",
     "niveau": "L3",
     "anneeUniversitaire": "2025-2026",
     "departementId": 1
   }
   ✓ Réponse: Groupe créé

ÉTAPE 5: Tester sans token (doit échouer)
   GET /api/etudiants
   ✗ Supprimer le header Authorization
   ✗ Réponse: 401 Unauthorized

================================================================================
ERREURS COURANTES & SOLUTIONS
================================================================================

❌ 401 Unauthorized
   Solution:
   1. Appeler POST /api/auth/register ou /login d'abord
   2. Vérifier que {{jwt_token}} est rempli
   3. Vérifier que le header Authorization est présent

❌ 500 Internal Server Error
   Solution:
   1. Vérifier que le serveur démarre: docker-compose up
   2. Vérifier que la BD est accessible
   3. Vérifier GET /api/health

❌ 400 Bad Request
   Solution:
   1. Vérifier le format JSON
   2. Vérifier les champs requis
   3. Vérifier que les types matchent (string, int, etc.)

❌ 404 Not Found
   Solution:
   1. Vérifier l'URL exacte
   2. Vérifier que l'endpoint existe
   3. Vérifier que l'ID existe

================================================================================
TIPS & TRICKS
================================================================================

💡 Tip 1: Cookies vs Token
   Postman gère les cookies et tokens automatiquement
   Pas besoin de copier/coller manuellement

💡 Tip 2: Historique des Requêtes
   Postman sauvegarde automatiquement chaque requête envoyée
   Accès via "History" en bas à gauche

💡 Tip 3: Collections Partagées
   Vous pouvez partager la collection avec votre équipe
   Right-click → Export

💡 Tip 4: Scripts de Test
   Les scripts exécutent après chaque réponse
   Voir l'onglet "Tests" sur chaque requête

💡 Tip 5: Pré-request Scripts
   Exécutent avant l'envoi de la requête
   Voir l'onglet "Pre-request Script"

================================================================================
EXEMPLE COMPLET: CRÉATION D'UN ÉTUDIANT
================================================================================

1. S'INSCRIRE (Administrateur)
   POST /api/auth/register
   Body: {"nomComplet":"Admin","email":"admin@test.com",
          "password":"Admin123!","userType":"ADMINISTRATEUR"}
   Result: {{jwt_token}} = eyJhbGc...

2. CRÉER UN DÉPARTEMENT
   POST /api/departements
   Authorization: Bearer {{jwt_token}}
   Body: {"nom":"Informatique"}
   Result: {"id":1,"nom":"Informatique"}

3. CRÉER UN GROUPE
   POST /api/groupes
   Authorization: Bearer {{jwt_token}}
   Body: {"libelle":"L3-INFO-A","niveau":"L3",
          "anneeUniversitaire":"2025-2026","departementId":1}
   Result: {"id":1,"libelle":"L3-INFO-A",...}

4. CRÉER UN ÉTUDIANT
   POST /api/etudiants
   Authorization: Bearer {{jwt_token}}
   Body: {"nomComplet":"Mohammed Aziz","email":"aziz@test.com",
          "motDePasseHash":"hash123","actif":true,
          "matricule":"ETU001","niveau":"L3","groupeId":1}
   Result: Étudiant créé avec succès

================================================================================
TESTS À EFFECTUER
================================================================================

[ ] Serveur démarre sans erreurs
[ ] GET /api/health retourne UP
[ ] GET / retourne message de bienvenue
[ ] POST /api/auth/register crée compte
[ ] Token JWT est sauvegardé dans {{jwt_token}}
[ ] GET /api/etudiants avec token retourne données
[ ] GET /api/etudiants sans token retourne 401
[ ] POST /api/etudiants crée étudiant avec token
[ ] PUT /api/etudiants/{id} modifie étudiant
[ ] DELETE /api/etudiants/{id} supprime étudiant
[ ] POST /api/auth/logout déconnecte
[ ] POST /api/auth/validate valide token
[ ] Endpoints protégés demandent token
[ ] Token expiré retourne 401

================================================================================
STRUCTURE DE FICHIER
================================================================================

Backend/
├── Postman_Collection.json      ← Importer dans Postman
├── POSTMAN_GUIDE.md             ← Guide d'utilisation
└── ...autres fichiers...

================================================================================
SUPPORT & RESSOURCES
================================================================================

Postman Documentation: https://learning.postman.com/
JWT Debugging: https://jwt.io/
API Docs: API_DOCUMENTATION.md
Quick Start: QUICK_START.md
JWT Guide: JWT_IMPLEMENTATION.md

================================================================================

🎉 POSTMAN COLLECTION READY TO USE!

1. Télécharger Postman: https://www.postman.com/downloads/
2. File → Import → Postman_Collection.json
3. Commencer à tester!

Generated: 2026-04-12
Status: ✅ READY FOR TESTING

================================================================================

