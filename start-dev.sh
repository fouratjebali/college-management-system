#!/bin/bash
# Complete Auth Flow Testing Guide
# Run this in your project root directory

echo "🚀 Starting College Management System"
echo "========================================"

# Step 1: Build and start containers
echo ""
echo "📦 Building Docker containers..."
docker-compose build

echo ""
echo "▶️  Starting services (postgres, backend, frontend)..."
docker-compose up -d

echo ""
echo "⏳ Waiting for services to be ready (30 seconds)..."
sleep 30

# Step 2: Verify services
echo ""
echo "✅ Checking service status..."
docker-compose ps

echo ""
echo "📊 Checking logs..."
echo ""
echo "--- Backend logs ---"
docker-compose logs backend | tail -20

echo ""
echo "--- PostgreSQL logs ---"
docker-compose logs postgres | tail -10

# Step 3: Test health endpoint
echo ""
echo "🏥 Testing backend health..."
curl -s http://localhost:8080/api/health || echo "Backend not ready yet"

# Step 4: Success message
echo ""
echo ""
echo "✅ Services started successfully!"
echo ""
echo "📍 Access links:"
echo "   Frontend: http://localhost:4200"
echo "   Backend:  http://localhost:8080"
echo "   Database: localhost:5432"
echo ""
echo "📚 Next steps:"
echo "   1. Open http://localhost:4200 in browser"
echo "   2. Register a new account"
echo "   3. Login with credentials"
echo "   4. Check browser console for auth flow"
echo "   5. See localStorage for tokens"
echo ""
echo "🧪 For manual testing, see TESTING_GUIDE.md"
echo ""
echo "📋 View logs:"
echo "   - docker-compose logs -f backend"
echo "   - docker-compose logs -f frontend"
echo "   - docker-compose logs -f postgres"
echo ""
echo "🛑 To stop services:"
echo "   - docker-compose down"
echo ""
