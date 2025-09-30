#!/bin/bash
set -euo pipefail

usage() {
  cat <<EOF
Usage: $(basename "$0") [cursor-agent options] "prompt"

Example:
  $(basename "$0") --force --print --model sonnet-4.5 "process the input folder"

Notes:
  - The script always appends --output-format=json to the cursor-agent command.
  - Provide the prompt as the final argument; everything before it is passed through.
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

PROMPT=${!#}
if [ -z "$PROMPT" ]; then
  echo "Error: prompt argument is required" >&2
  usage
  exit 1
fi

PASS_ARGS=("${@:1:$#-1}")

LOG_FILE=$(mktemp)
PIPE_PATH=$(mktemp -u)
mkfifo "$PIPE_PATH"

cleanup() {
  rm -f "$LOG_FILE" "$PIPE_PATH" || true
}
trap cleanup EXIT

CMD=(cursor-agent --force --print --model sonnet-4.5 "${PASS_ARGS[@]+"${PASS_ARGS[@]}"}" --output-format=json "$PROMPT")

echo "Running: ${CMD[*]}"

tee "$LOG_FILE" <"$PIPE_PATH" &
TEE_PID=$!

"${CMD[@]}" >"$PIPE_PATH" 2>&1 &
AGENT_PID=$!

exit_code=1

while kill -0 "$AGENT_PID" 2>/dev/null; do
  if grep -q '"type"\s*:\s*"result"' "$LOG_FILE"; then
    RESULT_LINE=$(grep '"type"\s*:\s*"result"' "$LOG_FILE" | tail -1)
    echo "Detected result event:"
    echo "$RESULT_LINE"
    kill -TERM "$AGENT_PID" 2>/dev/null || true
    wait "$AGENT_PID" 2>/dev/null || true
    exit_code=0
    break
  fi
  sleep 1
done

if kill -0 "$AGENT_PID" 2>/dev/null; then
  wait "$AGENT_PID"
  exit_code=$?
fi

wait "$TEE_PID" 2>/dev/null || true

echo ""
echo "=== Cursor Agent log ==="
cat "$LOG_FILE"

exit $exit_code

