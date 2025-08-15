#!/bin/bash

# setup-cli-env.sh - Setup environment for CLI tools (Aider/Gemini)
# Used by GitHub Actions for consistent environment setup

set -e

CLI_TOOL="$1"
MODEL="${2:-gemini/gemini-2.5-flash-preview-05-20}"
API_KEY="$3"

echo "🔧 Setting up $CLI_TOOL environment..."

# Create output directories
mkdir -p outputs
mkdir -p outputs/pr-summary

# Validate API key
if [ -z "$API_KEY" ]; then
    echo "ERROR: No API key provided"
    exit 1
fi

echo "✅ API key validation passed"

# Set up environment based on CLI tool
case "$CLI_TOOL" in
    "aider")
        echo "📦 Setting up Aider environment..."
        
        # Check if aider is already cached and working
        if command -v aider >/dev/null 2>&1 && aider --version >/dev/null 2>&1; then
            echo "✅ Aider found in cache and working, skipping installation"
            aider --version
        else
            echo "📦 Aider not found in cache or not working, installing..."
            pip install --upgrade pip
            pip install aider-install
            aider-install
            
            # Verify new installation
            echo "🔍 Verifying new Aider installation..."
            aider --version
            echo "✅ Aider installation verified"
        fi
        
        # Install additional dependencies for Gemini models
        pip install google-generativeai
        ;;
        
    "gemini")
        echo "📦 Setting up Gemini CLI environment..."
        npm install -g @google/gemini-cli@latest
        echo "✅ Gemini CLI installed successfully"
        echo "📋 Gemini CLI version:"
        gemini --version
        
        # Create settings file for Gemini CLI
        mkdir -p .gemini
        cat > .gemini/settings.json << EOF
{
  "model": "$MODEL",
  "temperature": 0.1,
  "maxOutputTokens": 8192
}
EOF
        echo "✅ Gemini CLI settings configured"
        ;;
        
    *)
        echo "ERROR: Unknown CLI tool: $CLI_TOOL"
        echo "Supported tools: aider, gemini"
        exit 1
        ;;
esac

echo "✅ $CLI_TOOL environment setup complete"
