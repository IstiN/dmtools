#!/bin/bash

set -e  # Exit on any error

echo "🔐 Setting up database password..."

# Create temp directory if it doesn't exist
mkdir -p temp

# Generate a secure password (32 characters)
DB_PASSWORD=$(openssl rand -base64 32)

# Write password to file
echo -n "$DB_PASSWORD" > temp/dbpassword.key

# Make the file readable only by owner for security
chmod 600 temp/dbpassword.key

echo "✅ Password generated and saved to temp/dbpassword.key"
echo "Generated password: $DB_PASSWORD"

# Create the database user with the generated password
echo "👤 Creating database user..."
gcloud sql users create dmtools-user \
    --instance=dmtools-db \
    --password="$DB_PASSWORD"

echo "✅ Database user 'dmtools-user' created successfully"

# Verify user creation
echo "📋 Listing database users:"
gcloud sql users list --instance=dmtools-db

# Try to add to GitHub secrets using GitHub CLI
echo ""
echo "🔑 Attempting to add password to GitHub secrets..."

if command -v gh &> /dev/null; then
    # Check if GitHub CLI is authenticated
    if gh auth status &> /dev/null; then
        echo "📡 GitHub CLI is authenticated, adding secret..."
        gh secret set DB_PASSWORD --body "$DB_PASSWORD"
        echo "✅ Password successfully added to GitHub secrets!"
        
        # Verify the secret was added
        echo "📋 Verifying GitHub secrets:"
        gh secret list | grep DB_PASSWORD || echo "❌ DB_PASSWORD not found in secrets list"
    else
        echo "❌ GitHub CLI not authenticated. Please run 'gh auth login' first"
        echo "📝 Manual step required: Add password to GitHub secrets"
        echo "   Password: $DB_PASSWORD"
        echo "   Or run: gh secret set DB_PASSWORD --body \"\$(cat temp/dbpassword.key)\""
    fi
else
    echo "❌ GitHub CLI not installed or not in PATH"
    echo "📝 Manual step required: Add password to GitHub secrets"
    echo "   Password: $DB_PASSWORD"
    echo "   Go to: https://github.com/IstiN/dmtools/settings/secrets/actions"
    echo "   Add new secret with name: DB_PASSWORD"
    echo "   Or install GitHub CLI: brew install gh"
fi

echo ""
echo "🎯 Setup Summary:"
echo "✅ Password generated: temp/dbpassword.key"
echo "✅ Database user created: dmtools-user"
echo "✅ Database instance: dmtools-db"
echo "✅ Database name: dmtools"
echo ""
echo "🧪 Test connection:"
echo "   gcloud sql connect dmtools-db --user=dmtools-user --database=dmtools"
echo ""
echo "🚀 Ready for deployment!" 