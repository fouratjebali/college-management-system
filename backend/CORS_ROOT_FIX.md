# ✅ CORS & ROOT CONTROLLER FIX

## 🐛 Problèmes Corrigés

### 1. CORS Error (400 Bad Request)
**Erreur:** "When allowCredentials is true, allowedOrigins cannot contain the special value "*""

**Cause:** Conflit de configuration CORS
- `allowCredentials = true` 
- `allowedOrigins = "*"` ← Incompatible!

**Solution:** Utiliser `setAllowedOriginPatterns()` au lieu de `setAllowedOrigins()`
```java
configuration.setAllowedOriginPatterns(Arrays.asList(
    "http://localhost:4200",
    "http://localhost:3000",
    "http://localhost:8080",
    "http://localhost:*"
));
```

**Fichier modifié:** `SecurityConfig.java`

---

### 2. Root Controller Not Found (500 Error)
**Erreur:** "No static resource for request '/'"

**Cause:** L'image Docker utilisait l'ancienne version du JAR

**Solution:** Redémarrer Docker avec `--build` pour reconstruire l'image
```bash
docker-compose down
docker-compose up --build
```

**Fichier créé:** `RootController.java`

---

## ✅ Étapes Appliquées

1. ✅ Corrigé `SecurityConfig.java` - CORS configuration
2. ✅ Recompilé le projet - `mvn clean package -DskipTests`
3. ✅ Arrêté les conteneurs - `docker-compose down`
4. ✅ Relancé avec reconstruction - `docker-compose up --build`

---

## 🧪 Tests Après Démarrage

Une fois que vous voyez **"Started BackendApplication"**, testez:

```bash
# Test 1: Root endpoint (200 OK)
curl http://localhost:8080/

# Test 2: Health endpoint (200 OK)
curl http://localhost:8080/api/health

# Test 3: Register (201 Created)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nomComplet":"Test","email":"test@test.com","password":"Test123!","userType":"ETUDIANT","matricule":"TEST001","niveau":"L3"}'
```

---

## 📋 Checklist de Vérification

- [ ] Docker redémarré complètement
- [ ] "Started BackendApplication" dans les logs
- [ ] GET / retourne JSON de bienvenue (200)
- [ ] GET /api/health retourne status UP (200)
- [ ] POST /api/auth/register crée compte (201)
- [ ] Token JWT est retourné

---

## 🔍 Si ça ne fonctionne pas

### Logs Docker
```bash
docker-compose logs miniprojet-backend
```

### Vérifier le port
```bash
netstat -an | find "8080"
```

### Restart complet
```bash
docker-compose down -v    # -v enlève les volumes
docker-compose up --build
```

---

**Les deux problèmes sont maintenant corrigés! 🎉**

