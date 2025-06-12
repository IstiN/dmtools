#!/bin/bash

# Production Deployment Script for dmtools App Engine

echo "🚀 Starting production deployment to App Engine..."

# Check if GEMINI_API_KEY is set in app.yaml
if grep -q "YOUR_GEMINI_API_KEY_HERE" app.yaml; then
    echo "⚠️  WARNING: Please update GEMINI_API_KEY in app.yaml before deploying!"
    echo "   Edit app.yaml and replace 'YOUR_GEMINI_API_KEY_HERE' with your actual Gemini API key."
    read -p "   Have you updated the API key? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ Deployment cancelled. Please update the API key first."
        exit 1
    fi
fi

# Build the application for production
echo "🔨 Building application for production..."
./gradlew clean bootJar -PbuildProfile=production -x test

if [ $? -ne 0 ]; then
    echo "❌ Build failed! Please fix compilation errors."
    exit 1
fi

echo "✅ Build successful!"

# Deploy to App Engine
echo "☁️  Deploying to App Engine..."
gcloud app deploy app.yaml --quiet

if [ $? -eq 0 ]; then
    echo "✅ Deployment successful!"
    echo "🌐 Your application is available at: https://$(gcloud config get-value project).lm.r.appspot.com"
    echo "🔍 To view logs: gcloud app logs read --service=default"
    echo "📱 To open in browser: gcloud app browse"
else
    echo "❌ Deployment failed!"
    exit 1
fi 