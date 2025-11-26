#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
PROMPT_FILE="$SCRIPT_DIR/unit-test-generation-prompt.txt"

usage() {
  cat <<EOF
Usage: $(basename "$0") <package-name> [cursor-agent options]

Generates unit tests for all Java files in the specified package.

Arguments:
  package-name          Full package name (e.g., com.github.istin.dmtools.microsoft.ado.model)
  cursor-agent options  Optional arguments passed to cursor-agent (e.g., --model sonnet-4.5)

Examples:
  $(basename "$0") com.github.istin.dmtools.microsoft.ado.model
  $(basename "$0") com.github.istin.dmtools.microsoft.ado.model --model sonnet-4.5

Process:
  1. Converts package name to folder path
  2. Finds all .java files in the package
  3. For each file, runs cursor-agent to generate/extend unit tests
  4. Tests are created in src/test/java with same package structure
EOF
}

if [ $# -lt 1 ]; then
  usage
  exit 1
fi

if [ ! -f "$PROMPT_FILE" ]; then
  echo "Error: Prompt file not found: $PROMPT_FILE" >&2
  exit 1
fi

RUN_AGENT_SCRIPT="$SCRIPT_DIR/run-cursor-agent.sh"
if [ ! -f "$RUN_AGENT_SCRIPT" ]; then
  echo "Error: run-cursor-agent.sh not found: $RUN_AGENT_SCRIPT" >&2
  exit 1
fi

if [ ! -x "$RUN_AGENT_SCRIPT" ]; then
  chmod +x "$RUN_AGENT_SCRIPT"
fi

PACKAGE_NAME="$1"
shift || true  # Get remaining args for cursor-agent (if any)

# Convert package name to folder path
# com.github.istin.dmtools.microsoft.ado.model -> dmtools-core/src/main/java/com/github/istin/dmtools/microsoft/ado/model
PACKAGE_PATH="${PACKAGE_NAME//.//}"
SOURCE_DIR="$PROJECT_ROOT/dmtools-core/src/main/java/$PACKAGE_PATH"
TEST_DIR="$PROJECT_ROOT/dmtools-core/src/test/java/$PACKAGE_PATH"

if [ ! -d "$SOURCE_DIR" ]; then
  echo "Error: Source directory not found: $SOURCE_DIR" >&2
  exit 1
fi

# Ensure test directory exists
mkdir -p "$TEST_DIR"

echo "Package: $PACKAGE_NAME"
echo "Source directory: $SOURCE_DIR"
echo "Test directory: $TEST_DIR"
echo ""

# Find all Java files in the source directory
JAVA_FILES=()
while IFS= read -r -d '' file; do
  JAVA_FILES+=("$file")
done < <(find "$SOURCE_DIR" -maxdepth 1 -name "*.java" -type f -print0 | sort -z)

if [ ${#JAVA_FILES[@]} -eq 0 ]; then
  echo "No Java files found in $SOURCE_DIR"
  exit 0
fi

echo "Found ${#JAVA_FILES[@]} Java file(s) to process:"
for file in "${JAVA_FILES[@]}"; do
  echo "  - $(basename "$file")"
done
echo ""

# Read the prompt template
PROMPT_TEMPLATE=$(cat "$PROMPT_FILE")

# Save cursor-agent arguments before the loop
CURSOR_AGENT_ARGS=("$@")

# Process each Java file
SUCCESS_COUNT=0
FAILED_COUNT=0
FAILED_FILES=()

for java_file in "${JAVA_FILES[@]}"; do
  filename=$(basename "$java_file")
  classname="${filename%.java}"
  test_filename="${classname}Test.java"
  test_file="$TEST_DIR/$test_filename"
  
  # Check if test file exists
  if [ -f "$test_file" ]; then
    echo "=========================================="
    echo "Processing: $filename (test exists, will extend)"
    echo "=========================================="
    ACTION="extend"
  else
    echo "=========================================="
    echo "Processing: $filename (creating new test)"
    echo "=========================================="
    ACTION="create"
  fi
  
  # Build the full prompt with file path and context
  if [ "$ACTION" = "extend" ]; then
    ACTION_MSG="extend the existing test file"
  else
    ACTION_MSG="create a new test file"
  fi
  
  # Extract package name from the file path for test commands
  # Convert: dmtools-core/src/test/java/com/github/istin/dmtools/microsoft/ado/model/WorkItemCommentTest.java
  # To: com.github.istin.dmtools.microsoft.ado.model.WorkItemCommentTest
  RELATIVE_TEST_PATH="${test_file#$PROJECT_ROOT/dmtools-core/src/test/java/}"
  TEST_PACKAGE_CLASS="${RELATIVE_TEST_PATH%.java}"
  TEST_PACKAGE_CLASS="${TEST_PACKAGE_CLASS//\//.}"
  
  FULL_PROMPT="${PROMPT_TEMPLATE}
  
File to test: $java_file
Test file location: $test_file
Test class full name: $TEST_PACKAGE_CLASS
Action: $ACTION_MSG

## Specific Commands for This Test

After writing the tests, compile and run ONLY this specific test class:

1. Compile:
   cd $PROJECT_ROOT
   ./gradlew :dmtools-core:compileTestJava

2. Run ONLY this specific test class (do NOT run all tests in the package):
   ./gradlew :dmtools-core:test --tests \"$TEST_PACKAGE_CLASS\"

**IMPORTANT**: Only run the test class you just wrote/updated. Do NOT run all tests in the package.

Please analyze the source file and $ACTION_MSG with comprehensive unit tests following the rules in .cursor/temp_rules/unit-testing.mdc. After writing tests, verify they compile and pass using the commands above. Remove any test methods that fail and cannot be easily fixed. Stop if it's covered."
  
  # Build run-cursor-agent.sh command
  # run-cursor-agent.sh accepts cursor-agent options and prompt as last argument
  if [ ${#CURSOR_AGENT_ARGS[@]} -gt 0 ]; then
    CMD=("$RUN_AGENT_SCRIPT" "${CURSOR_AGENT_ARGS[@]}" "$FULL_PROMPT")
  else
    CMD=("$RUN_AGENT_SCRIPT" "$FULL_PROMPT")
  fi
  
  echo "Running: ${CMD[*]}"
  echo ""
  
  # Execute run-cursor-agent.sh
  if "${CMD[@]}"; then
    echo ""
    echo "✓ Successfully processed $filename"
    ((SUCCESS_COUNT++))
  else
    exit_code=$?
    echo ""
    echo "✗ Failed to process $filename (exit code: $exit_code)"
    FAILED_FILES+=("$filename")
    ((FAILED_COUNT++))
  fi
  
  echo ""
done

# Summary
echo "=========================================="
echo "Summary"
echo "=========================================="
echo "Total files processed: ${#JAVA_FILES[@]}"
echo "Successful: $SUCCESS_COUNT"
echo "Failed: $FAILED_COUNT"

if [ $FAILED_COUNT -gt 0 ]; then
  echo ""
  echo "Failed files:"
  for file in "${FAILED_FILES[@]}"; do
    echo "  - $file"
  done
  exit 1
fi

echo ""
echo "All tests generated successfully!"

