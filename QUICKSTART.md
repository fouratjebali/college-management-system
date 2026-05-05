# College Management System - Complete Setup & Testing Guide

**Status:** ✅ **ALL SERVICES RUNNING AND TESTED**

---

## 🚀 Project Status Summary

### Services Running
- ✅ **PostgreSQL Database** (localhost:5432) - Healthy
- ✅ **Spring Boot Backend** (localhost:8080) - Running 
- ✅ **Angular Frontend** (localhost:4200) - Running
- ✅ **Authentication System** - Fully Functional

### Tests Completed
- ✅ Health Check (Backend API responding)
- ✅ User Registration (with correct role mapping)
- ✅ Login (credentials verified)
- ✅ Get Current User (token validation)
- ✅ Validate Token (JWT checks)
- ✅ Error Handling (401 Unauthorized properly rejected)
- ✅ Token Refresh (new tokens generated)

---

## 🌐 Access Points

### Test the Application Now:

```
Frontend (UI):  http://localhost:4200
Backend API:    http://localhost:8080
Database:       localhost:5432
```

### API Endpoints

**Authentication:**
- POST `/api/auth/register` - Create new account
- POST `/api/auth/login` - Login with credentials
- GET `/api/auth/me` - Get current user info
- POST `/api/auth/validate` - Validate token
- POST `/api/auth/refresh` - Get new access token
- POST `/api/auth/logout` - Logout

---

## 📋 Complete Auth Flow Testing Steps

### Step 1: Open Application
```
1. Open browser: http://localhost:4200
2. You should see the login/register page
3. Check DevTools (F12) for any errors
```

### Step 2: Register New Account

1. Click **"Sign Up"** button
2. Fill in the registration form:
   - **Email**: Enter any email (e.g., `student@example.com`)
   - **Password**: Enter password (e.g., `password123`)
   - **Full Name**: Enter name (e.g., `John Doe`)
   - **User Type**: Select **"Student"**
   - **Matricule**: Enter ID (e.g., `STU001`)
   - **Niveau**: Select **"L3"**
3. Click **"Register"**
4. Should be redirected to dashboard

### Step 3: Verify Token Storage

After registration/login:
1. Open DevTools: **Press F12**
2. Go to **Application** tab
3. Click **Local Storage** → **http://localhost:4200**
4. Look for key: `app_auth`
5. You should see:
   ```json
   {
     "token": "eyJhbGciOiJIUzUxMiJ9...",
     "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
     "user": {
       "id": 1,
       "email": "student@example.com",
       "nomComplet": "John Doe",
       "role": "STUDENT"
     }
   }
   ```

### Step 4: Check JWT Headers

1. Open DevTools: **Press F12**
2. Go to **Network** tab
3. Click any API endpoint or navigate to a page
4. Look for any request to backend
5. Click on the request
6. Go to **Request Headers**
7. You should see:
   ```
   Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
   ```

### Step 5: Test Login/Logout

1. **Logout**: Click logout button
2. Should be redirected to login page
3. Local Storage `app_auth` should be cleared
4. **Login**: Enter credentials
5. Should see tokens restored in Local Storage

### Step 6: Test Protected Routes

1. Try accessing admin-only routes (if available)
2. If you're a STUDENT, you should be blocked
3. Role-based access control is working if you see permission denial

### Step 7: Check Browser Console

Open DevTools Console (F12 → Console):
```javascript
// View auth state
const auth = JSON.parse(localStorage.getItem('app_auth'));
console.log('Current Auth:', auth);
console.log('User Role:', auth.user.role);
console.log('Token Expiry:', auth.token);
```

---

## 🧪 Manual API Testing (Advanced)

If you want to test the backend API directly:

```powershell
# Test in PowerShell

$baseUrl = "http://localhost:8080/api"

# 1. Register
$register = @{
    email = "test@example.com"
    password = "password123"
    nomComplet = "Test User"
    userType = "ETUDIANT"
    matricule = "STU001"
    niveau = "L3"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri "$baseUrl/auth/register" `
    -Method Post -ContentType "application/json" -Body $register | ConvertFrom-Json

$token = $response.token

# 2. Get current user
Invoke-WebRequest -Uri "$baseUrl/auth/me" `
    -Method Get -Headers @{"Authorization" = "Bearer $token"} | ConvertFrom-Json

# 3. Validate token
Invoke-WebRequest -Uri "$baseUrl/auth/validate" `
    -Method Post -Headers @{"Authorization" = "Bearer $token"} | ConvertFrom-Json
```

---

## 🔐 JWT Token Structure

Your JWT token contains:
- **Header**: Algorithm (HS512)
- **Payload**: Contains:
  - `sub` - Subject (user)
  - `userId` - User ID from database
  - `userType` - ETUDIANT, PROFESSEUR, or ADMINISTRATEUR
  - `iat` - Issued at (timestamp)
  - `exp` - Expiration (24 hours from issue)
- **Signature**: Verified by backend

---

## 📊 Database Setup

The database automatically:
1. Creates all tables on first startup
2. Loads sample data:
   - 3 departments
   - 3 admin users
   - 3 students
   - Sample courses and groups

**Sample Users Available:**
- Admin: `admin@university.edu` / `admin`
- Professor: `prof@university.edu` / `professor`
- Student: `aziz@student.edu` / `etudiant`

---

## 🛠️ Troubleshooting

### Frontend not loading?
```bash
# Check frontend logs
docker-compose logs frontend

# Restart frontend
docker-compose restart frontend
```

### Backend not responding?
```bash
# Check backend logs
docker-compose logs backend

# Test health endpoint
curl http://localhost:8080/api/health
```

### Database connection failed?
```bash
# Check database logs
docker-compose logs db

# Direct database connection
docker exec -it institut-db psql -U institut_user -d institut_db

# Check if database is healthy
docker-compose ps
```

### Clear all data and restart fresh?
```bash
# Stop and remove everything (including volumes)
docker-compose down -v

# Start fresh
docker-compose up -d

# Wait 45 seconds for initialization
```

### Port already in use?
```bash
# Check what's using the ports
netstat -ano | findstr "4200|8080|5432"

# Kill process (Windows)
taskkill /PID <PID> /F

# Or change docker-compose ports:
# - Change 4200:4200 to 4201:4200 for frontend
# - Change 8080:8080 to 8081:8080 for backend
```

---

## 📁 Project Files Modified

**Recent Changes:**
- ✅ Updated `frontend/Dockerfile` - Changed Node.js from v18 to v22 (Angular requirement)
- ✅ `docker-compose.yml` - 3 services configured for dev use
- ✅ `application.properties` - Local PostgreSQL config
- ✅ `application-docker.properties` - Docker network config
- ✅ `backend/src/main/java/.../AuthController.java` - Added /refresh and /me endpoints
- ✅ `backend/src/main/resources/db/init/schema.sql` - Full database setup
- ✅ `backend/src/main/resources/db/init/seed_data.sql` - Sample data

---

## 📚 Key Features Implemented

### Authentication System
- ✅ JWT token generation (24-hour expiration)
- ✅ Refresh tokens (7-day expiration)
- ✅ Password hashing (BCrypt)
- ✅ Role-based access control
- ✅ Token validation on requests
- ✅ Automatic token refresh on 401

### Data Persistence
- ✅ PostgreSQL database in Docker
- ✅ Named volume for data persistence
- ✅ Automatic schema initialization
- ✅ Sample data loaded on startup

### Frontend
- ✅ Angular 20.3.0
- ✅ Route guards (authentication & role-based)
- ✅ HTTP interceptors (JWT injection & error handling)
- ✅ Auth service with full lifecycle
- ✅ localStorage persistence
- ✅ Reactive auth state management

### Backend
- ✅ Spring Boot 4.0.5
- ✅ Spring Security integration
- ✅ JPA/Hibernate ORM
- ✅ JJWT token library
- ✅ Role mapping (ETUDIANT → STUDENT)
- ✅ Error handling with proper HTTP codes

---

## 🎯 Next Steps

### Option 1: Explore the UI
1. Go to http://localhost:4200
2. Register a new account
3. Login and explore the dashboard
4. Check browser DevTools (F12) to see tokens

### Option 2: Test with API
1. Run `test-auth-simple.ps1` to test all endpoints
2. Use curl or Postman to test API manually
3. Check backend logs for debug info

### Option 3: Development
1. Make code changes (auto-reload enabled)
2. Frontend: Changes auto-compile
3. Backend: Needs restart for code changes
4. Database: Schema persists in named volume

### Option 4: Debugging
1. Check logs: `docker-compose logs -f service-name`
2. Database: `docker exec -it institut-db psql ...`
3. Browser DevTools: F12 → Console, Network, Storage
4. API Testing: Use PowerShell or Postman

---

## 📋 Checklist: Everything Working?

- [ ] Frontend loads at http://localhost:4200
- [ ] Can register new account
- [ ] Tokens stored in localStorage
- [ ] Can login with credentials
- [ ] Backend API responding on port 8080
- [ ] Tokens show in Network -> Authorization header
- [ ] Protected routes require authentication
- [ ] Role mapping working (ETUDIANT → STUDENT)
- [ ] Can see Console tab with no major errors
- [ ] Database is healthy and filled with data

---

## 🎉 Success!

Your **College Management System** is fully operational with:
- ✅ Complete authentication system
- ✅ Secure JWT tokens
- ✅ Role-based access control
- ✅ Data persistence
- ✅ Modern Angular frontend
- ✅ Spring Boot backend
- ✅ PostgreSQL database

**Start testing at:** http://localhost:4200

---

## 📞 Commands Reference

```bash
# View all containers
docker-compose ps

# View logs
docker-compose logs -f                 # All services
docker-compose logs -f backend         # Only backend
docker-compose logs -f frontend        # Only frontend

# Start/Stop
docker-compose up -d                   # Start in background
docker-compose down                    # Stop all
docker-compose restart backend         # Restart one service

# Database
docker exec -it institut-db psql -U institut_user -d institut_db

# Clean start (WARNING: deletes data)
docker-compose down -v && docker-compose up -d
```

---

**Last Updated:** April 13, 2026
**Version:** Development (dev environment)
**Status:** Production Ready for Testing ✅
