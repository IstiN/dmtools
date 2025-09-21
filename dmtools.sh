#!/bin/bash
# DMTools CLI Wrapper
# Usage: ./dmtools.sh [command] [args...]
# Supports: STDIN, heredoc, file input, and inline JSON

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Try to find JAR file in multiple locations
find_jar_file() {
    local jar_file=""
    
    # 1. Check if installed via installer (user's home directory)
    if [ -f "$HOME/.dmtools/dmtools.jar" ]; then
        jar_file="$HOME/.dmtools/dmtools.jar"
    # 2. Check local build directory (development)
    elif [ -f "$SCRIPT_DIR/build/libs"/*.jar ]; then
        jar_file=$(find "$SCRIPT_DIR/build/libs" -name "*-all.jar" | head -1)
    # 3. Check for any JAR file in the script directory
    elif [ -f "$SCRIPT_DIR"/*.jar ]; then
        jar_file=$(find "$SCRIPT_DIR" -name "dmtools*.jar" -o -name "*-all.jar" | head -1)
    # 4. Check parent directory build folder (if script is in subdirectory)
    elif [ -f "$SCRIPT_DIR/../build/libs"/*.jar ]; then
        jar_file=$(find "$SCRIPT_DIR/../build/libs" -name "*-all.jar" | head -1)
    fi
    
    echo "$jar_file"
}

JAR_FILE=$(find_jar_file)

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Helper functions
error() {
    echo -e "${RED}Error: $1${NC}" >&2
    exit 1
}

info() {
    echo -e "${GREEN}Info: $1${NC}" >&2
}

usage() {
    cat << EOF
DMTools CLI Wrapper

Usage:
  $0 list                           # List available MCP tools
  $0 <tool> [args...]              # Execute MCP tool with args
  $0 <tool> --data '{"json"}'      # Execute with inline JSON
  $0 <tool> --file params.json     # Execute with JSON file
  $0 <tool> --verbose              # Execute with debug output
  $0 <tool> <<EOF                  # Execute with heredoc
  {"json": "data"}
  EOF

Examples:
  $0 list
  $0 jira_get_ticket DMC-479 summary,description
  $0 jira_get_ticket --data '{"key": "DMC-479", "fields": ["summary"]}'
  $0 jira_get_ticket <<EOF
  {
    "key": "DMC-479",
    "fields": ["summary", "description"]
  }
  EOF

Environment Variables:
  DMTOOLS_INTEGRATIONS    Comma-separated list of integrations (jira,confluence,figma)

Installation:
  If you don't have DMTools installed, run:
  curl https://github.com/IstiN/dmtools/releases/latest/download/install.sh -fsS | bash

EOF
    exit 0
}

# Parse arguments
if [ $# -eq 0 ]; then
    usage
fi

COMMAND="$1"
shift

# Handle special commands
case "$COMMAND" in
    "help"|"-h"|"--help")
        usage
        ;;
    "list")
        if [ $# -gt 0 ]; then
            # List with filter
            java -Dlog4j2.configurationFile=classpath:log4j2-cli.xml -Dlog4j.configuration=log4j2-cli.xml -cp "$JAR_FILE" com.github.istin.dmtools.job.JobRunner mcp list "$1" 2>/dev/null
        else
            # List all tools
            java -Dlog4j2.configurationFile=classpath:log4j2-cli.xml -Dlog4j.configuration=log4j2-cli.xml -cp "$JAR_FILE" com.github.istin.dmtools.job.JobRunner mcp list 2>/dev/null
        fi
        exit 0
        ;;
esac

# Check if STDIN has data (heredoc/pipe)
STDIN_DATA=""
if [ ! -t 0 ]; then
    STDIN_DATA=$(cat)
fi

# Parse remaining arguments
ARGS=()
DATA=""
FILE=""
QUIET=true

while [[ $# -gt 0 ]]; do
    case $1 in
        --data)
            DATA="$2"
            shift 2
            ;;
        --file)
            FILE="$2"
            shift 2
            ;;
        --verbose)
            QUIET=false
            shift
            ;;
        --quiet)
            QUIET=true
            shift
            ;;
        *)
            ARGS+=("$1")
            shift
            ;;
    esac
done

# Check if JAR file exists
if [ -z "$JAR_FILE" ] || [ ! -f "$JAR_FILE" ]; then
    error "DMTools JAR file not found. Please install DMTools first:
  curl https://github.com/IstiN/dmtools/releases/latest/download/install.sh -fsS | bash

Or if you're developing locally, build the project first:
  ./gradlew build

Note: Java 23 is required for DMTools to run."
fi

# Determine data source priority: STDIN > --data > --file > args
if [ -n "$STDIN_DATA" ]; then
    [ "$QUIET" = false ] && info "Reading JSON from STDIN (${#STDIN_DATA} characters)"
    [ "$QUIET" = false ] && info "Using STDIN data"
    if [ "$QUIET" = true ]; then
        java -Dlog4j2.configurationFile=classpath:log4j2-cli.xml -Dlog4j.configuration=log4j2-cli.xml -cp "$JAR_FILE" com.github.istin.dmtools.job.JobRunner mcp "$COMMAND" --stdin-data "$STDIN_DATA" "${ARGS[@]}" 2>/dev/null | tail -1
    else
        java -Dlog4j2.configurationFile=classpath:log4j2-cli.xml -Dlog4j.configuration=log4j2-cli.xml -cp "$JAR_FILE" com.github.istin.dmtools.job.JobRunner mcp "$COMMAND" --stdin-data "$STDIN_DATA" "${ARGS[@]}" 2>/dev/null
    fi
elif [ -n "$DATA" ]; then
    [ "$QUIET" = false ] && info "Using inline data"
    if [ "$QUIET" = true ]; then
        java -Dlog4j2.configurationFile=classpath:log4j2-cli.xml -Dlog4j.configuration=log4j2-cli.xml -cp "$JAR_FILE" com.github.istin.dmtools.job.JobRunner mcp "$COMMAND" --data "$DATA" "${ARGS[@]}" 2>/dev/null | tail -1
    else
        java -Dlog4j2.configurationFile=classpath:log4j2-cli.xml -Dlog4j.configuration=log4j2-cli.xml -cp "$JAR_FILE" com.github.istin.dmtools.job.JobRunner mcp "$COMMAND" --data "$DATA" "${ARGS[@]}" 2>/dev/null
    fi
elif [ -n "$FILE" ]; then
    if [ ! -f "$FILE" ]; then
        error "File not found: $FILE"
    fi
    [ "$QUIET" = false ] && info "Using file data: $FILE"
    FILE_DATA=$(cat "$FILE")
    if [ "$QUIET" = true ]; then
        java -Dlog4j2.configurationFile=classpath:log4j2-cli.xml -Dlog4j.configuration=log4j2-cli.xml -cp "$JAR_FILE" com.github.istin.dmtools.job.JobRunner mcp "$COMMAND" --data "$FILE_DATA" "${ARGS[@]}" 2>/dev/null | tail -1
    else
        java -Dlog4j2.configurationFile=classpath:log4j2-cli.xml -Dlog4j.configuration=log4j2-cli.xml -cp "$JAR_FILE" com.github.istin.dmtools.job.JobRunner mcp "$COMMAND" --data "$FILE_DATA" "${ARGS[@]}" 2>/dev/null
    fi
else
    [ "$QUIET" = false ] && info "Using positional arguments"
    if [ "$QUIET" = true ]; then
        java -Dlog4j2.configurationFile=classpath:log4j2-cli.xml -Dlog4j.configuration=log4j2-cli.xml -cp "$JAR_FILE" com.github.istin.dmtools.job.JobRunner mcp "$COMMAND" "${ARGS[@]}" 2>/dev/null | tail -1
    else
        java -Dlog4j2.configurationFile=classpath:log4j2-cli.xml -Dlog4j.configuration=log4j2-cli.xml -cp "$JAR_FILE" com.github.istin.dmtools.job.JobRunner mcp "$COMMAND" "${ARGS[@]}" 2>/dev/null
    fi
fi