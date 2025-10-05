#!/bin/bash
# Test script to simulate PR creation workflow

set -e

echo "=== PR Creation Test Simulation ==="
echo ""

# Step 1: Create outputs directory if it doesn't exist
echo "1. Creating outputs directory..."
mkdir -p outputs

# Step 2: Create fake response.md with complex markdown
echo "2. Creating fake outputs/response.md with complex markdown..."
cat > outputs/response.md << 'EOF'
## Development Summary

### Approach
Implemented file path injection support for AI Teammate JSON configuration in CLI mode by enhancing the existing `extractIfNeeded()` method in `Teammate.java`.

### Key Changes

#### Source Code
- Modified `Teammate.java`:
  ```java
  // Resolve relative paths from current working directory
  Path basePath = Paths.get(System.getProperty("user.dir"));
  Path filePath = input.startsWith("/") ? 
          Paths.get(input) : basePath.resolve(input).normalize();
  ```

#### Special Characters Test
- Dollar signs: $PATH, $HOME, ${PROJECT_ROOT}
- Backticks: `code`, `file.txt`
- Quotes: "double", 'single'
- Newlines and special chars: \n, \t, \r

### Test Coverage
- **14 test cases** covering all aspects
- **100% coverage** of new methods
- **All tests passing** ✅

### Notes
This is a **comprehensive** implementation with *proper* error handling!
EOF

echo "   Created $(wc -l < outputs/response.md) lines of markdown"

# Step 3: Create a fake code change to commit
echo "3. Creating fake code changes..."
echo "// Test change at $(date)" >> test-change-marker.txt

# Step 4: Stage the changes
echo "4. Staging test changes..."
git add test-change-marker.txt

# Step 5: Show what would be committed
echo "5. Changes ready to commit:"
git status --short

echo ""
echo "=== Ready to test PR creation ==="
echo ""
echo "Run the following command to test:"
echo "  dmtools run test-pr-creation.json"
echo ""
echo "To clean up after test:"
echo "  git reset HEAD test-change-marker.txt"
echo "  rm test-change-marker.txt outputs/response.md outputs/pr_body.md"
echo ""
