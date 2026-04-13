#!/usr/bin/env bash

# ============================================
# Student Management System - Setup Script
# ============================================

echo "=================================================="
echo "Student Management System - Backend Setup"
echo "=================================================="
echo ""

# Couleurs pour l'output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Vérifier Java
echo -e "${YELLOW}Checking Java installation...${NC}"
if ! command -v java &> /dev/null; then
    echo -e "${RED}ERROR: Java is not installed!${NC}"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "\K[^"]*')
echo -e "${GREEN}✓ Java Version: $JAVA_VERSION${NC}"
echo ""

# Vérifier Maven
echo -e "${YELLOW}Checking Maven installation...${NC}"
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}ERROR: Maven is not installed!${NC}"
    exit 1
fi

MVN_VERSION=$(mvn -v | grep "Apache Maven" | cut -d' ' -f3)
echo -e "${GREEN}✓ Maven Version: $MVN_VERSION${NC}"
echo ""

# Options de démarrage
echo "=============================================="
echo "Chose une option de démarrage:"
echo "=============================================="
echo "1) Build + Run (Recommandé pour dev)"
echo "2) Clean Build + Run"
echo "3) Build only (sans run)"
echo "4) Docker Compose"
echo "5) JAR only (target/*.jar)"
echo "6) Exit"
echo ""

read -p "Entrez votre choix (1-6): " choice

case $choice in
    1)
        echo ""
        echo -e "${YELLOW}Compiling and running...${NC}"
        mvn spring-boot:run
        ;;
    2)
        echo ""
        echo -e "${YELLOW}Clean building and running...${NC}"
        mvn clean spring-boot:run
        ;;
    3)
        echo ""
        echo -e "${YELLOW}Building only...${NC}"
        mvn clean package -DskipTests
        echo ""
        echo -e "${GREEN}✓ Build completed!${NC}"
        echo "JAR location: target/Backend-0.0.1-SNAPSHOT.jar"
        ;;
    4)
        echo ""
        echo -e "${YELLOW}Starting with Docker Compose...${NC}"
        docker-compose up
        ;;
    5)
        echo ""
        if [ -f "target/Backend-0.0.1-SNAPSHOT.jar" ]; then
            echo -e "${YELLOW}Running JAR file...${NC}"
            java -jar target/Backend-0.0.1-SNAPSHOT.jar
        else
            echo -e "${RED}JAR file not found! Build first using option 3${NC}"
        fi
        ;;
    6)
        echo "Goodbye!"
        exit 0
        ;;
    *)
        echo -e "${RED}Invalid choice!${NC}"
        exit 1
        ;;
esac

