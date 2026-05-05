# CORS Configuration Fix - 2026-04-13

## Problème Résolu
Les erreurs suivantes ont été corrigées:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "When allowCredentials is true, allowedOrigins cannot contain the special value \"*\" since that cannot be set on the \"Access-Control-Allow-Origin\" response header."
}
```

## Cause Racine
Il y avait des configurations CORS conflictantes:
1. `SecurityConfig.java` utilisait correctement `allowedOriginPatterns` avec `allowCredentials(true)`
2. `WebMvcConfig.java` utilisait incorrectement `allowedOrigins("*")` avec `allowCredentials(true)` ce qui causait le conflit
3. `HealthController.java` avait une annotation `@CrossOrigin(origins = "*")` qui entrait également en conflit

## Corrections Apportées

### 1. WebMvcConfig.java
**Avant:**
```java
registry.addMapping("/api/**")
    .allowedOrigins("http://localhost:4200", "http://localhost:3000")
    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
    .allowedHeaders("*")
    .allowCredentials(true)
    .maxAge(3600);
```

**Après:**
```java
// CORS configuration pour la racine sans credentials
// La configuration API avec credentials est gérée par SecurityConfig
registry.addMapping("/")
    .allowedOrigins("http://localhost:4200", "http://localhost:3000", "http://localhost:8080")
    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
    .allowedHeaders("*")
    .maxAge(3600);
```

### 2. SecurityConfig.java
**Correction:**
- Limité la configuration CORS à `/api/**` seulement (au lieu de `/**`)
- Utilisation correcte de `allowedOriginPatterns` avec `allowCredentials(true)`
- Patterns spécifiés: `http://localhost:3000`, `http://localhost:4200`, `http://localhost:8080`

### 3. HealthController.java
**Avant:**
```java
@CrossOrigin(origins = "*")
public class HealthController {
    // ...
}
```

**Après:**
```java
public class HealthController {
    // Annotation @CrossOrigin supprimée - CORS géré par SecurityConfig
    // ...
}
```

## Résultats Attendus
✅ Endpoint racine `/` doit fonctionner sans erreur CORS
✅ Endpoint `/api/health` doit fonctionner sans erreur CORS
✅ Endpoint `/api/health/info` doit fonctionner sans erreur CORS
✅ Les credentials peuvent être envoyés aux origines spécifiées

## Instructions de Test

### 1. Tester la racine
```bash
curl -X GET http://localhost:8080/ \
  -H "Origin: http://localhost:3000"
```

Response attendue: HTTP 200 avec JSON du message d'accueil

### 2. Tester /api/health
```bash
curl -X GET http://localhost:8080/api/health \
  -H "Origin: http://localhost:3000"
```

Response attendue: HTTP 200 avec status "UP"

### 3. Tester depuis Postman
Assurez-vous que dans Postman:
- Le `{{base_url}}` est défini à `http://localhost:8080`
- Tester les endpoints: `{{base_url}}/api/health` et `{{base_url}}/`

## Build et Déploiement

La compilation a été vérifiée: ✅ BUILD SUCCESS

Pour redémarrer:
```bash
# Redémarrer les conteneurs Docker
docker-compose down
docker-compose up -d

# Ou en local:
mvn clean package -DskipTests
mvn spring-boot:run
```

## Configuration CORS Finale

**SecurityConfig.java - Endpoints API:**
- Patterns: `http://localhost:3000`, `http://localhost:4200`, `http://localhost:8080`
- allowCredentials: true
- Méthodes: GET, POST, PUT, DELETE, OPTIONS, PATCH
- Headers: * (tous autorisés)
- MaxAge: 3600 secondes

**WebMvcConfig.java - Endpoint Racine:**
- Origins: `http://localhost:4200`, `http://localhost:3000`, `http://localhost:8080`
- allowCredentials: false (pas nécessaire pour la racine)
- Méthodes: GET, POST, PUT, DELETE, OPTIONS, PATCH
- Headers: * (tous autorisés)
- MaxAge: 3600 secondes

## Fichiers Modifiés
1. ✅ `src/main/java/MiniProjet_Backend/Backend/Config/WebMvcConfig.java`
2. ✅ `src/main/java/MiniProjet_Backend/Backend/Config/SecurityConfig.java`
3. ✅ `src/main/java/MiniProjet_Backend/Backend/Controller/HealthController.java`

