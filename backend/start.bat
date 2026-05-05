@echo off
REM Script de démarrage de l'application pour Windows

echo ================================
echo Student Management System Backend
echo ================================
echo.

REM Vérifier Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed!
    exit /b 1
)

REM Afficher la version Java
for /f "tokens=*" %%i in ('java -version 2^>^&1') do (
    echo Java Version: %%i
    goto next
)

:next
echo.
echo Compiling project...
call mvn clean compile

if %errorlevel% neq 0 (
    echo Compilation failed!
    exit /b 1
)

echo.
echo Starting application...
call mvn spring-boot:run

pause

