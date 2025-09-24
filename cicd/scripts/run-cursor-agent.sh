#!/bin/bash
set -euo pipefail

show_usage() {
  cat <<EOF
Usage: $(basename "$0") [cursor-agent options] "prompt"

Example:
  $(basename "$0") --force --print --model sonnet-4 "process the input folder"

Notes:
  - The script always appends --output-format=json to the cursor-agent command.
  - Provide the prompt as the final argument. All preceding arguments are passed through.
EOF
}

if [ $# -lt 1 ]; then
  show_usage
  exit 1
fi

# Ensure cursor-agent exists
if ! command -v cursor-agent >/dev/null 2>&1; then
  echo "Error: cursor-agent not found in PATH" >&2
  exit 1
fi

PROMPT=${!#}
if [ -z "$PROMPT" ]; then
  echo "Error: Prompt argument is required" >&2
  show_usage
  exit 1
fi

# Collect pass-through arguments except prompt
PASS_ARGS=("${@:1:$#-1}")

LOG_FILE=$(mktemp)
cleanup() {
  rm -f "$LOG_FILE" || true
}
trap cleanup EXIT

CMD=(cursor-agent "${PASS_ARGS[@]}" --output-format=json "$PROMPT")

echo "Running: ${CMD[*]}"
"${CMD[@]}" >"$LOG_FILE" 2>&1 &
AGENT_PID=$!
tail -n +1 -f "$LOG_FILE" &
TAIL_PID=$!

exit_code=1

while kill -0 "$AGENT_PID" 2>/dev/null; do
  if grep -q '"type"\s*:\s*"result"' "$LOG_FILE"; then
    RESULT_LINE=$(grep '"type"\s*:\s*"result"' "$LOG_FILE" | tail -1)
    echo "Detected result event:"
    echo "$RESULT_LINE"
    kill -TERM "$AGENT_PID" 2>/dev/null || true
    wait "$AGENT_PID" 2>/dev/null || true
    kill -TERM "$TAIL_PID" 2>/dev/null || true
    wait "$TAIL_PID" 2>/dev/null || true
    exit_code=0
    break
  fi
  sleep 1
done

if kill -0 "$AGENT_PID" 2>/dev/null; then
  wait "$AGENT_PID" || true
fi

kill -TERM "$TAIL_PID" 2>/dev/null || true
wait "$TAIL_PID" 2>/dev/null || true

exit $exit_code

