#!/bin/bash
set -euo pipefail

usage() {
  cat <<EOF
Usage: $(basename "$0") [cursor-agent options] "prompt"

Runs cursor-agent with the provided options and prompt.

Example:
  $(basename "$0") "process the input folder"
  $(basename "$0") --force --print --model sonnet-4.5 "process the input folder"

Notes:
  - Provide the prompt as the final argument
  - All other arguments are passed through to cursor-agent
  - Default options: --force --print --model sonnet-4.5 --output-format=text
EOF
}

if [ $# -lt 1 ]; then
  usage
  exit 1
fi

if ! command -v cursor-agent >/dev/null 2>&1; then
  echo "Error: cursor-agent not found in PATH" >&2
  exit 127
fi

# Extract prompt (last argument)
PROMPT="${!#}"

if [ -z "$PROMPT" ]; then
  echo "Error: prompt argument is required" >&2
  usage
  exit 1
fi

# Get all arguments except the last one (the prompt)
PASS_ARGS=()
if [ $# -gt 1 ]; then
  PASS_ARGS=("${@:1:$#-1}")
fi

# Build command with defaults if no options provided
if [ ${#PASS_ARGS[@]} -eq 0 ]; then
  CMD=(cursor-agent --force --print --model sonnet-4.5 --output-format=text "$PROMPT")
else
  CMD=(cursor-agent "${PASS_ARGS[@]}" --output-format=text "$PROMPT")
fi

echo "Running: ${CMD[*]}"
echo ""

# Execute cursor-agent directly
"${CMD[@]}"

exit_code=$?

echo ""
echo "=== Cursor Agent completed with exit code: $exit_code ==="

exit $exit_code