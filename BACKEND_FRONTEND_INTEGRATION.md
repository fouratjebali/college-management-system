# Backend-Frontend Authentication Integration Guide

**Project:** College Management System (Development Environment)  
**Status:** ✅ FULLY INTEGRATED  
**Date:** April 13, 2026  

---

## Quick Start - Local Development

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 14 (or Docker with Docker Compose)
- Node.js 18+
- Angular CLI 20

### Option 1: Local Development (Recommended)
```bash
# Terminal 1: Start Local PostgreSQL
docker run --name miniprojet-db \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=miniprojet_db \
  -p 5432:5432 \
  -d postgres:14-alpine

# Terminal 2: Start Backend
cd backend
mvn clean spring-boot:run

# Terminal 3: Start Frontend
cd frontend
npm install
npm start
```

**Access:**
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080/api

### Option 2: Full Docker-Compose Development
```bash
# Start everything in Docker
docker-compose up --build

# View logs
docker-compose logs -f
```

**Access:**
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080/api
- PostgreSQL: localhost:5432 (password: institut_password)

---

## Authentication Flow

### Login/Register → Token Storage
```
User logs in
    ↓
Frontend → POST /api/auth/login
    ↓
Backend validates credentials
    ↓
Returns AuthResponse:
{
  "token": "access-token (24h)",
  "refreshToken": "refresh-token (7d)",
  "user": { "id", "email", "nomComplet", "role" }
}
    ↓
Frontend stores in localStorage
Automatic JWT injection on all API calls
```

### Protected API Requests
```
Frontend → GET /api/courses
    ↓
JWT Interceptor auto-adds:
Authorization: Bearer <access-token>
    ↓
Backend validates token
    ↓
AuthGuard checks authentication
RoleGuard checks role permissions
    ↓
✅ Request allowed or ❌ 401/403 denied
```

### Token Refresh (Auto)
```
Access token expires (24 hours)
    ↓
Frontend detects 401 response
    ↓
POST /api/auth/refresh with old refresh token
    ↓
Backend returns new tokens
    ↓
Frontend stores new tokens & retries request
User doesn't notice (seamless!)
```

---

## API Endpoints

| Method | Endpoint | Purpose | Status |
|--------|----------|---------|--------|
| POST | `/api/auth/login` | User login | 200 |
| POST | `/api/auth/register` | New user registration | 201 |
| POST | `/api/auth/refresh` | Get new access token | 200 |
| GET | `/api/auth/me` | Get current user info | 200 |
| POST | `/api/auth/validate` | Validate token | 200 |
| POST | `/api/auth/logout` | Logout (front-end clears) | 200 |

### Login Request/Response Example
```bash
# Request
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@university.edu",
    "password": "admin"
  }'

# Response
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "user": {
    "id": 1,
    "email": "admin@university.edu",
    "nomComplet": "Admin Principal",
    "role": "ADMIN"
  }
}
```

---

## Frontend Features

### ✅ Route Guards
```typescript
// Only authenticated users
canActivate: [authGuard]

// Only specific roles
canActivate: [roleGuard]
data: { roles: ['ADMIN', 'PROFESSOR'] }
```

### ✅ JWT Interceptor
Automatically injects authentication header:
```
GET /api/courses
Authorization: Bearer eyJhbGc...
```

### ✅ Error Handling
- 401 → Redirect to login
- 403 → Show permission denied
- 500 → Show error page
- Network errors → Auto-retry

### ✅ Token Persistence
- Tokens stored in browser localStorage
- Auto-load on page refresh
- Auto-clear on logout

---

## Backend Configuration

### Local Development
**File:** `application.properties`
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/miniprojet_db
spring.jpa.hibernate.ddl-auto=create-drop
jwt.expiration=86400000        # 1 day
jwt.refresh-expiration=604800000  # 7 days
```

### Docker Development  
**File:** `application-docker.properties`
```properties
spring.datasource.url=jdbc:postgresql://postgres:5432/institut_db
spring.jpa.hibernate.ddl-auto=update
jwt.expiration=86400000        # 1 day
jwt.refresh-expiration=604800000  # 7 days
```

The `update` mode means data persists across container restarts!

---

## Testing

### 1. Test Local Development
```bash
# Start backend
mvn clean spring-boot:run

# Start frontend (new terminal)
npm start

# Visit http://localhost:4200
# Register or login with demo credentials
```

### 2. Test Docker Stack
```bash
docker-compose up --build

# Check services are running
docker-compose ps

# View logs
docker-compose logs backend
docker-compose logs frontend
```

### 3. Manual API Testing
```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@university.edu","password":"admin"}'

# Use returned token
TOKEN="eyJhbGc..."

# Get current user
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/auth/me

# Refresh token
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"eyJhbGc..."}'
```

### 4. Browser DevTools Check
1. Open DevTools (F12)
2. Go to Application → Storage → Local Storage
3. Check `http://localhost:4200`
4. Look for `app_auth` key
5. Should contain: `{token, refreshToken, user}`

---

## User Roles

| Role | Backend | Frontend | Access |
|------|---------|----------|--------|
| Student | ETUDIANT | STUDENT | Student dashboard |
| Professor | PROFESSEUR | PROFESSOR | Course management |
| Admin | ADMINISTRATEUR | ADMIN | Full system |

### Sample Users (from init-data.sql)

**Admin:**
- Email: `admin@university.edu`
- Password: `admin` (in DB, use actual hash)

**Professor:**
- Email: `fatima@university.edu`
- Email: `ahmed@university.edu`

**Students:**
- Email: `aziz@student.edu`
- Email: `leila@student.edu`

---

## Development Workflow

### Edit & Test Cycle
```bash
# Terminal 1: Backend (watches for changes)
mvn clean spring-boot:run

# Terminal 2: Frontend (auto-reload on save)
npm start

# Edit code in IDE
# Save → Auto-reload in browser
# Check browser console for errors
```

### Debugging
```bash
# Backend logs
grep "ERROR\|WARN" ./logs/Backend.log

# Frontend console
DevTools → Console tab (F12)

# Database
docker exec -it miniprojet-db psql -U postgres -d miniprojet_db
miniprojet_db=# \dt  # list tables
miniprojet_db=# SELECT * FROM utilisateur;
```

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| **Port already in use** | Change port in application.properties (server.port=8081) |
| **Database connection failed** | Ensure PostgreSQL running: `docker ps` |
| **Login fails** | Check database has users, or register new account |
| **CORS errors** | Already handled by SecurityConfig, ensure frontend on 4200 |
| **Token expired** | Frontend auto-refreshes, check /api/auth/refresh works |
| **Docker won't build** | `docker-compose up --build --no-cache` |

---

## Key Files

```
backend/
├── src/main/resources/
│   ├── application.properties          # Local dev config
│   └── application-docker.properties   # Docker dev config
├── src/main/java/.../
│   ├── Controller/AuthController.java  # Auth endpoints
│   ├── Service/AuthService.java        # Auth logic
│   └── Security/JwtTokenProvider.java  # Token generation/validation
└── pom.xml                             # Maven dependencies

frontend/
├── src/app/core/
│   ├── guards/                         # Route protection
│   ├── interceptors/                   # JWT + Error handling
│   └── services/auth.ts                # Auth service
└── package.json                        # npm dependencies
```

---

## Stack Overview

**Backend:**
- Java 17 + Spring Boot 4.0.5
- JWT Token Authentication (JJWT)
- Spring Data JPA + Hibernate
- PostgreSQL 14

**Frontend:**
- Angular 20.3.0 + TypeScript
- HTTP Interceptors for auto JWT injection
- Route Guards for access control
- RxJS for reactive updates

**Containers:**
- Docker + Docker Compose
- PostgreSQL in container
- Hot-reload for development

---

## Done! 🚀

Backend and frontend are fully integrated and ready for development!

**Next Steps:**
1. `docker-compose up` to start full stack
2. Navigate to http://localhost:4200
3. Register or login with demo
4. Start building features!

For issues, check the logs or run manual curl tests above.
