#!/bin/bash

# Test script for developTicketAndCreatePR.js workflow
# This script simulates the AI Teammate development workflow locally

set -e

echo "=========================================="
echo "AI Teammate Development Workflow Test"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Step 1: Build dmtools
echo -e "${YELLOW}Step 1: Building dmtools...${NC}"
if ./gradlew build -x test > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Build successful${NC}"
else
    echo -e "${RED}✗ Build failed${NC}"
    echo "Run: ./gradlew clean build -x test"
    exit 1
fi
echo ""

# Step 2: Create test file (simulating cursor agent development)
echo -e "${YELLOW}Step 2: Creating test file (simulating development work)...${NC}"
cat > TEST_FILE_DMC544.md << 'EOF'
# Test Development for DMC-544

This file was created to simulate cursor agent development work.

## Changes Made
- Implemented file path injection functionality
- Added comprehensive test coverage
- Updated documentation

## Files Modified
- Teammate.java
- TeammateFilePathTest.java

This test file will be committed and pushed as part of the workflow simulation.
EOF

if [ -f TEST_FILE_DMC544.md ]; then
    echo -e "${GREEN}✓ Test file created: TEST_FILE_DMC544.md${NC}"
else
    echo -e "${RED}✗ Failed to create test file${NC}"
    exit 1
fi
echo ""

# Step 3: Check if dmtools.sh exists
echo -e "${YELLOW}Step 3: Checking dmtools CLI...${NC}"
if [ -f dmtools.sh ]; then
    echo -e "${GREEN}✓ dmtools.sh found${NC}"
else
    echo -e "${RED}✗ dmtools.sh not found${NC}"
    exit 1
fi
echo ""

# Step 4: Run the JavaScript via JSRunner
echo -e "${YELLOW}Step 4: Executing developTicketAndCreatePR.js via JSRunner...${NC}"
echo ""
echo "========================================"
echo "JavaScript Execution Output:"
echo "========================================"
echo ""

# Run dmtools with JSRunner
if ./dmtools.sh run jsrunner test-develop-pr.json; then
    echo ""
    echo "========================================"
    echo -e "${GREEN}✓ JavaScript execution completed${NC}"
    echo "========================================"
else
    EXIT_CODE=$?
    echo ""
    echo "========================================"
    echo -e "${RED}✗ JavaScript execution failed with code ${EXIT_CODE}${NC}"
    echo "========================================"
    echo ""
    echo -e "${YELLOW}Common Issues:${NC}"
    echo "1. CLI integration not exposed: Check if 'cli' is in JobJavaScriptBridge integrations set"
    echo "2. No changes to commit: Create TEST_FILE_DMC544.md before running"
    echo "3. GitHub authentication: Set PAT_TOKEN or run 'gh auth login'"
    echo "4. Jira authentication: Check dmtools.env configuration"
    echo ""
    
    # Cleanup
    echo -e "${YELLOW}Cleaning up test file...${NC}"
    rm -f TEST_FILE_DMC544.md
    echo -e "${GREEN}✓ Cleanup complete${NC}"
    
    exit $EXIT_CODE
fi

echo ""
echo -e "${YELLOW}Step 5: Cleanup (optional)...${NC}"
read -p "Do you want to clean up the test branch? (y/N): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    # Delete test file
    rm -f TEST_FILE_DMC544.md
    echo -e "${GREEN}✓ Deleted test file${NC}"
    
    # Ask about branch deletion
    read -p "Delete local branch feature/DMC-544? (y/N): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        git branch -D feature/DMC-544 2>/dev/null && echo -e "${GREEN}✓ Deleted local branch${NC}" || echo -e "${YELLOW}Branch not found or already deleted${NC}"
    fi
    
    read -p "Delete remote branch feature/DMC-544? (y/N): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        git push origin --delete feature/DMC-544 2>/dev/null && echo -e "${GREEN}✓ Deleted remote branch${NC}" || echo -e "${YELLOW}Remote branch not found or already deleted${NC}"
    fi
else
    echo -e "${YELLOW}Skipping cleanup. Test artifacts remain for inspection.${NC}"
fi

echo ""
echo "=========================================="
echo -e "${GREEN}Test Complete!${NC}"
echo "=========================================="

