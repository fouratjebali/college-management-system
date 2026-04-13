# Authentication Flow Testing Guide

**Date:** April 13, 2026  
**Status:** Ready for Testing 🚀

---

## 🚀 Step 1: Start the Application

### Option A: Using docker-compose (Recommended)

```bash
# From project root
docker-compose up --build

# This starts:
# - PostgreSQL database on localhost:5432
# - Spring Boot backend on localhost:8080
# - Angular frontend on localhost:4200
```

### Option B: Local Development (without Docker)

```bash
# Terminal 1: PostgreSQL
docker run -d --name miniprojet-db \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=miniprojet_db \
  -p 5432:5432 \
  postgres:14-alpine

# Terminal 2: Backend
cd backend
mvn clean spring-boot:run

# Terminal 3: Frontend
cd frontend
npm install
npm start
```

### Verify Services

```bash
# Check all services are running
docker-compose ps

# Check backend is responding
curl http://localhost:8080/api/health

# Expected response:
# {"status":"UP","message":"Student Management System is running"}
```

---

## 🧪 Step 2: Test Authentication Flow

### Test 1: Register New User

```bash
# Register a student
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@student.edu",
    "password": "password123",
    "nomComplet": "John Doe",
    "userType": "ETUDIANT",
    "matricule": "STU001",
    "niveau": "L3"
  }'

# Expected response (201 Created):
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "user": {
    "id": 1,
    "email": "john@student.edu",
    "nomComplet": "John Doe",
    "role": "STUDENT"
  }
}
```

**Save the token for next steps!**

```bash
# Save token in variable for easy testing
TOKEN="eyJhbGciOiJIUzUxMiJ9..."  # Replace with your token
REFRESH_TOKEN="eyJhbGciOiJIUzUxMiJ9..."  # Replace with your refresh token
```

---

### Test 2: Login with Credentials

```bash
# Login with registered user
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@student.edu",
    "password": "password123"
  }'

# Expected response (200 OK):
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "user": {
    "id": 1,
    "email": "john@student.edu",
    "nomComplet": "John Doe",
    "role": "STUDENT"
  }
}
```

---

### Test 3: Get Current User Info

```bash
# Get current authenticated user
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN"

# Expected response (200 OK):
{
  "id": 1,
  "email": "john@student.edu",
  "nomComplet": "John Doe",
  "role": "STUDENT"
}
```

---

### Test 4: Validate Token

```bash
# Validate if token is still valid
curl -X POST http://localhost:8080/api/auth/validate \
  -H "Authorization: Bearer $TOKEN"

# Expected response (200 OK):
{
  "valid": true,
  "email": "john@student.edu"
}
```

---

### Test 5: Refresh Access Token

```bash
# Get new access token using refresh token
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\": \"$REFRESH_TOKEN\"}"

# Expected response (200 OK):
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",  # New token
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",  # New refresh token
  "user": {
    "id": 1,
    "email": "john@student.edu",
    "nomComplet": "John Doe",
    "role": "STUDENT"
  }
}
```

---

### Test 6: Logout

```bash
# Logout (front-end will clear localStorage)
curl -X POST http://localhost:8080/api/auth/logout

# Expected response (200 OK):
{
  "message": "Logged out successfully. Please remove token from client."
}
```

---

## 🌐 Step 3: Test Through Browser UI

### 1. Register New User

1. Open http://localhost:4200
2. Click "Sign Up" or navigate to `/auth/register`
3. Fill in registration form:
   - Email: `jane@student.edu`
   - Password: `password123`
   - Full Name: `Jane Smith`
   - User Type: Select "Student"
   - Matricule: `STU002`
   - Niveau: Select `L3`
4. Click "Register"
5. Should be redirected to dashboard
6. Check browser DevTools (F12):
   - **Application → Storage → Local Storage**
   - Look for key `app_auth`
   - Should contain: `{token, refreshToken, user}`

### 2. Check JWT Token

In browser console (F12 → Console):

```javascript
// Get and decode token
const auth = JSON.parse(localStorage.getItem('app_auth'));
console.log('Token:', auth.token);
console.log('Refresh Token:', auth.refreshToken);
console.log('User:', auth.user);

// Decode JWT payload (if using jwt-decode library)
const payload = JSON.parse(atob(auth.token.split('.')[1]));
console.log('Token Payload:', payload);
// Shows: { sub, userId, userType, iat, exp }
```

### 3. Login Flow

1. Logout first (if logged in)
2. Navigate to http://localhost:4200/auth/login
3. Enter credentials:
   - Email: `jane@student.edu`
   - Password: `password123`
4. Click "Login"
5. Should be redirected to dashboard
6. Verify tokens in localStorage (F12 → Storage)

### 4. Check HTTP Headers

1. Open DevTools (F12 → Network tab)
2. Make any API request (click to load data)
3. Look at Request headers for that API call
4. Should see:
   ```
   Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
   ```

### 5. Test Token Expiration Handling

1. Open DevTools Console (F12)
2. Clear the access token (simulate expiration):
   ```javascript
   const auth = JSON.parse(localStorage.getItem('app_auth'));
   auth.token = 'expired-token-for-testing';
   localStorage.setItem('app_auth', JSON.stringify(auth));
   ```
3. Try to make an API call
4. Application should automatically:
   - Detect 401 response
   - Call `/api/auth/refresh` with refreshToken
   - Get new access token
   - Retry original request
   - User shouldn't notice anything!

### 6. Test Role-Based Access

1. Login as ADMIN user (if available):
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"admin@university.edu","password":"admin"}'
   ```
2. Check user.role in localStorage
3. Should see `"role": "ADMIN"`
4. Try accessing admin-only routes
5. Should have access ✅

6. Login as STUDENT:
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"aziz@student.edu","password":"etudiant"}'
   ```
7. Try accessing admin routes
8. Should be blocked ❌ (403 Forbidden)

---

## 📊 Test Results Checklist

### Registration ✅
- [ ] Can register with all required fields
- [ ] Token and refreshToken received
- [ ] User role correctly converted (ETUDIANT → STUDENT)
- [ ] Data stored in localStorage
- [ ] Redirected to dashboard

### Login ✅
- [ ] Can login with correct credentials
- [ ] Cannot login with wrong password
- [ ] Token and refreshToken received
- [ ] User info persists after page refresh
- [ ] Tokens in localStorage

### Protected Requests ✅
- [ ] Authorization header sent with token
- [ ] /api/auth/me returns correct user info
- [ ] Protected routes blocked without token
- [ ] 401 response when token invalid

### Token Refresh ✅
- [ ] Can refresh token using refreshToken
- [ ] New token and refreshToken received
- [ ] Automatic refresh on 401 response
- [ ] No manual app restart needed

### Guards & Permissions ✅
- [ ] AuthGuard blocks unauthenticated access
- [ ] RoleGuard blocks unauthorized roles
- [ ] Admin routes only accessible to ADMIN
- [ ] Student routes only accessible to STUDENT/PROFESSOR/ADMIN

### Error Handling ✅
- [ ] 401 errors redirect to login
- [ ] 403 errors show permission denied
- [ ] Network errors display in console
- [ ] Error interceptor working

### Database ✅
- [ ] Users created in database
- [ ] Passwords hashed (not plain text)
- [ ] User roles stored correctly
- [ ] Data persists across restarts

---

## 🔍 Debugging Tips

### Check Backend Logs
```bash
docker-compose logs -f backend

# Look for:
# - "Login successful for email:"
# - "Token generated"
# - Any ERROR messages
```

### Check Frontend Console
Press F12 in browser and look at:
- **Console tab**: Error messages, auth state changes
- **Network tab**: HTTP requests/responses with tokens
- **Storage tab**: localStorage with tokens
- **Application tab**: Cookies (if any)

### Test Database Directly
```bash
# Connect to PostgreSQL
docker exec -it institut-db psql -U institut_user -d institut_db

# List users
SELECT id, email, nom_complet FROM utilisateur;

# Check if user was created
SELECT * FROM utilisateur WHERE email = 'john@student.edu';

# Exit
\q
```

### Common Issues

| Issue | Solution |
|-------|----------|
| 401 Invalid Token | Token may be expired, clear localStorage and login again |
| CORS Errors | Ensure frontend runs on http://localhost:4200 |
| Database Connection Failed | Check PostgreSQL is running: `docker-compose ps` |
| Backend not responding | Check logs: `docker-compose logs backend` |
| Tokens not in localStorage | Check browser DevTools > Application > Storage |
| 403 Forbidden on protected routes | Check user role, may need ADMIN or PROFESSOR role |

---

## 📝 Sample Test Execution

```bash
# Terminal window - Run from project root

# 1. Start services
docker-compose up --build

# 2. In new terminal, wait 30 seconds then test

# 3. Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test123","nomComplet":"Test User","userType":"ETUDIANT","matricule":"T001","niveau":"L3"}'

# Save token from response
# TOKEN="<copied_token>"

# 4. Get current user
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN"

# 5. Refresh token
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<copied_refresh_token>"}'

# 6. Logout
curl -X POST http://localhost:8080/api/auth/logout
```

---

## ✅ All Tests Passed?

If all checks ✅ pass:
1. Authentication system fully working
2. Frontend can login/register
3. Backend validates tokens correctly
4. Database stores users
5. Role-based access control working
6. Token refresh mechanism working
7. Error handling working
8. Ready for feature development! 🚀

---

## 🆘 Need Help?

Check:
1. Backend logs: `docker-compose logs backend`
2. Frontend console: F12 → Console
3. Database connection: `docker exec -it institut-db psql`
4. Network requests: F12 → Network tab (with token in headers)

Still stuck? Verify:
- Services running: `docker-compose ps`
- Ports available: 4200, 8080, 5432
- Docker volumes not corrupted: `docker-compose down -v && docker-compose up --build`

