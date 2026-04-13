#!/bin/bash

# Script de démarrage de l'application

echo "================================"
echo "Student Management System Backend"
echo "================================"
echo ""

# Vérifier Java
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed!"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "\K[^"]*')
echo "Java Version: $JAVA_VERSION"
echo ""

# Compiler et démarrer
echo "Compiling project..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

echo ""
echo "Starting application..."
mvn spring-boot:run


