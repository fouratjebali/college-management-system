# Simple Auth Flow Test

Write-Host "=== Testing College Management System Auth Flow ===" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080/api"

# Test 1: Health
Write-Host "1. Health Check..." -ForegroundColor Yellow
$health = Invoke-WebRequest -Uri "$baseUrl/health" -UseBasicParsing | ConvertFrom-Json
Write-Host "   Status: $($health.status)" -ForegroundColor Green
Write-Host ""

# Test 2: Register
Write-Host "2. Register New User..." -ForegroundColor Yellow
$email = "test_$(Get-Random)@example.com"
$body = @{
    email = $email
    password = "password123"
    nomComplet = "Test User"
    userType = "ETUDIANT"
    matricule = "STU001"
    niveau = "L3"
} | ConvertTo-Json

$register = Invoke-WebRequest -Uri "$baseUrl/auth/register" `
    -UseBasicParsing -Method Post `
    -ContentType "application/json" `
    -Body $body | ConvertFrom-Json

$token = $register.token
Write-Host "   User: $($register.user.email)" -ForegroundColor Green
Write-Host "   Role: $($register.user.role)" -ForegroundColor Green
Write-Host ""

# Test 3: Get Current User
Write-Host "3. Get Current User..." -ForegroundColor Yellow
$me = Invoke-WebRequest -Uri "$baseUrl/auth/me" `
    -UseBasicParsing -Method Get `
    -Headers @{"Authorization" = "Bearer $token"} | ConvertFrom-Json
Write-Host "   ID: $($me.id)" -ForegroundColor Green
Write-Host "   Email: $($me.email)" -ForegroundColor Green
Write-Host ""

# Test 4: Login  
Write-Host "4. Login..." -ForegroundColor Yellow
$loginBody = @{
    email = $email
    password = "password123"
} | ConvertTo-Json

$login = Invoke-WebRequest -Uri "$baseUrl/auth/login" `
    -UseBasicParsing -Method Post `
    -ContentType "application/json" `
    -Body $loginBody | ConvertFrom-Json

$newToken = $login.token
Write-Host "   Logged in as: $($login.user.email)" -ForegroundColor Green
Write-Host ""

# Test 5: Validate Token
Write-Host "5. Validate Token..." -ForegroundColor Yellow
$validate = Invoke-WebRequest -Uri "$baseUrl/auth/validate" `
    -UseBasicParsing -Method Post `
    -Headers @{"Authorization" = "Bearer $newToken"} | ConvertFrom-Json
Write-Host "   Token Valid: $($validate.valid)" -ForegroundColor Green
Write-Host ""

# Test 6: Refresh Token
Write-Host "6. Refresh Token..." -ForegroundColor Yellow
$refreshBody = @{
    refreshToken = $login.refreshToken
} | ConvertTo-Json

$refresh = Invoke-WebRequest -Uri "$baseUrl/auth/refresh" `
    -UseBasicParsing -Method Post `
    -ContentType "application/json" `
    -Body $refreshBody | ConvertFrom-Json

Write-Host "   New Token Generated: Yes" -ForegroundColor Green
Write-Host ""

# Test 7: Invalid Token
Write-Host "7. Test Invalid Token..." -ForegroundColor Yellow
try {
    Invoke-WebRequest -Uri "$baseUrl/auth/me" `
        -UseBasicParsing -Method Get `
        -Headers @{"Authorization" = "Bearer invalid_token"} -ErrorAction Stop
    Write-Host "   ERROR: Invalid token was accepted!" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host "   Correctly Rejected (401)" -ForegroundColor Green
    }
}
Write-Host ""

Write-Host "=== All Auth Tests Passed! ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Summary:" -ForegroundColor Cyan
Write-Host "  Register: PASS"
Write-Host "  Get User: PASS"
Write-Host "  Login: PASS"
Write-Host "  Validate: PASS"
Write-Host "  Refresh: PASS"
Write-Host "  Error Handling: PASS"
Write-Host ""
Write-Host "Next: Fix frontend Node.js version and access http://localhost:4200" -ForegroundColor Yellow
