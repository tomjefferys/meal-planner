#!/usr/bin/env bash
# Build script for Meal Planner
# Builds the React frontend and packages everything into a Spring Boot JAR

set -e

echo "🍽️  Building Meal Planner..."

# Build frontend
echo "📦 Building frontend..."
cd frontend
npm install
npm run build
cd ..

# Build backend (frontend assets are already in backend/src/main/resources/static/)
echo "☕ Building backend..."
cd backend
./mvnw clean package -DskipTests
cd ..

echo ""
echo "✅ Build complete!"
echo "Run with: java -jar backend/target/meal-planner-1.0.0.jar"
echo "Then open: http://localhost:8080"
