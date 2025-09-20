#!/bin/bash

# DMTools MCP CLI Wrapper Script
# This script provides a user-friendly interface for executing MCP tools via the DMTools fat JAR.

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Path to the fat JAR
JAR_FILE="$SCRIPT_DIR/build/libs/dmtools-v1.7.16-all.jar"

# Helper functions
error() {
    echo -e "${RED}Error: $1${NC}" >&2
    exit 1
}

info() {
    echo -e "${GREEN}Info: $1${NC}"
}

warn() {
    echo -e "${YELLOW}Warning: $1${NC}"
}

usage() {
    cat << EOF
DMTools MCP CLI Wrapper

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
  $0 jira_get_ticket --file ticket_params.json
  $0 confluence_search_content_by_text --data '{"text": "API documentation"}'

Environment Variables:
  JIRA_BASE_PATH     - Jira server URL
  JIRA_TOKEN         - Jira API token
  CONFLUENCE_BASE_PATH - Confluence server URL
  CONFLUENCE_TOKEN   - Confluence API token
  FIGMA_TOKEN        - Figma API token
  GEMINI_API_KEY     - Gemini AI API key

EOF
}

# Check arguments
if [ $# -eq 0 ]; then
    usage
    exit 1
fi

COMMAND="$1"
shift

# Handle special commands
case "$COMMAND" in
    "help"|"-h"|"--help")
        usage
        ;;
    "list")
        echo "Listing available MCP tools..."
        java -cp "$JAR_FILE" com.github.istin.dmtools.job.JobRunner mcp list 2>/dev/null
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
if [ ! -f "$JAR_FILE" ]; then
    error "JAR file not found: $JAR_FILE. Please build the project first with: ./gradlew build"
fi

# Determine data source priority: STDIN > --data > --file > args
if [ -n "$STDIN_DATA" ]; then
    [ "$QUIET" = false ] && info "Reading JSON from STDIN (${#STDIN_DATA} characters)"
    [ "$QUIET" = false ] && info "Using STDIN data"
    if [ "$QUIET" = true ]; then
        java -cp "$JAR_FILE" com.github.istin.dmtools.job.JobRunner mcp "$COMMAND" --stdin-data "$STDIN_DATA" "${ARGS[@]}" 2>/dev/null | tail -1
    else
        java -cp "$JAR_FILE" com.github.istin.dmtools.job.JobRunner mcp "$COMMAND" --stdin-data "$STDIN_DATA" "${ARGS[@]}" 2>/dev/null
    fi
elif [ -n "$DATA" ]; then
    [ "$QUIET" = false ] && info "Using inline data"
    if [ "$QUIET" = true ]; then
        java -cp "$JAR_FILE" com.github.istin.dmtools.job.JobRunner mcp "$COMMAND" --data "$DATA" "${ARGS[@]}" 2>/dev/null | tail -1
    else
        java -cp "$JAR_FILE" com.github.istin.dmtools.job.JobRunner mcp "$COMMAND" --data "$DATA" "${ARGS[@]}" 2>/dev/null
    fi
elif [ -n "$FILE" ]; then
    if [ ! -f "$FILE" ]; then
        error "File not found: $FILE"
    fi
    [ "$QUIET" = false ] && info "Using file data: $FILE"
    FILE_DATA=$(cat "$FILE")
    if [ "$QUIET" = true ]; then
        java -cp "$JAR_FILE" com.github.istin.dmtools.job.JobRunner mcp "$COMMAND" --data "$FILE_DATA" "${ARGS[@]}" 2>/dev/null | tail -1
    else
        java -cp "$JAR_FILE" com.github.istin.dmtools.job.JobRunner mcp "$COMMAND" --data "$FILE_DATA" "${ARGS[@]}" 2>/dev/null
    fi
else
    [ "$QUIET" = false ] && info "Using positional arguments"
    if [ "$QUIET" = true ]; then
        java -cp "$JAR_FILE" com.github.istin.dmtools.job.JobRunner mcp "$COMMAND" "${ARGS[@]}" 2>/dev/null | tail -1
    else
        java -cp "$JAR_FILE" com.github.istin.dmtools.job.JobRunner mcp "$COMMAND" "${ARGS[@]}" 2>/dev/null
    fi
fi