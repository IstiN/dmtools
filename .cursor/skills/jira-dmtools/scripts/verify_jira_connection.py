#!/usr/bin/env python3
"""
Validate JIRA connection via dmtools and write confirmation to .dmtools/jira.cfg.
Run from repository root (where dmtools.env exists).

Modes:
  Default: Run dmtools --version, list (jira tools), jira_get_my_profile;
           optionally --project PROJ to fetch and cache metadata; write jira.cfg on success.
  --verify: Only validate that .dmtools/jira.cfg exists and has mandatory keys in correct format (no dmtools calls).
"""

import argparse
import json
import os
import re
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path

DMTOOLS_README = "https://github.com/IstiN/dmtools/blob/main/README.md"
JIRA_API_TOKENS = "https://id.atlassian.com/manage-profile/security/api-tokens"
REQUIRED_ENV_VARS = "JIRA_BASE_PATH, JIRA_EMAIL, JIRA_API_TOKEN"
JIRA_CFG_MANDATORY_KEYS = ("validated_at", "jira_base_path", "account_id")
SCRIPT_INVOKE = "python .cursor/skills/jira-dmtools/scripts/verify_jira_connection.py"


def find_repo_root() -> Path:
    """Return directory containing dmtools.env (current dir or parent walk)."""
    path = Path.cwd().resolve()
    for _ in range(20):
        if (path / "dmtools.env").is_file():
            return path
        parent = path.parent
        if parent == path:
            break
        path = parent
    return Path.cwd().resolve()


def run_dmtools(repo_root: Path, *args: str) -> tuple[int, str, str]:
    """Run dmtools with given args; cwd=repo_root. Returns (returncode, stdout, stderr)."""
    cmd = ["dmtools"] + list(args)
    try:
        r = subprocess.run(
            cmd,
            cwd=repo_root,
            capture_output=True,
            text=True,
            timeout=60,
        )
        return r.returncode, r.stdout or "", r.stderr or ""
    except FileNotFoundError:
        return -1, "", "dmtools not found"
    except subprocess.TimeoutExpired:
        return -1, "", "dmtools timed out"


def err(msg: str, fix: str = "", links: str = "") -> None:
    print(msg, file=sys.stderr)
    if fix:
        print(f"Fix: {fix}", file=sys.stderr)
    if links:
        print(f"See: {links}", file=sys.stderr)


def fail_dmtools_not_found() -> None:
    err(
        "dmtools is not on PATH or not installed.",
        "Install: macOS/Linux/Git Bash: curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install | bash  |  Windows: curl -fsSL https://raw.githubusercontent.com/IstiN/dmtools/main/install.bat -o \"%TEMP%\\dmtools-install.bat\" && \"%TEMP%\\dmtools-install.bat\"  |  Then ensure dmtools is on PATH.",
        DMTOOLS_README,
    )
    sys.exit(2)


def fail_no_jira_config() -> None:
    err(
        "JIRA tools are missing from dmtools. dmtools.env is not loaded or does not contain JIRA variables.",
        f"Create or fix dmtools.env in the project root. Required: {REQUIRED_ENV_VARS}. Optional: JIRA_AUTH_TYPE (Basic or Bearer). Example: JIRA_BASE_PATH=https://your-company.atlassian.net  JIRA_EMAIL=your@email.com  JIRA_API_TOKEN=<token from link below>.",
        f"{DMTOOLS_README}  |  API token: {JIRA_API_TOKENS}",
    )
    sys.exit(3)


def fail_auth() -> None:
    err(
        "JIRA authentication failed (jira_get_my_profile returned an error).",
        f"Check dmtools.env in project root: {REQUIRED_ENV_VARS}. Ensure JIRA_BASE_PATH has no trailing slash. Token may be invalid or expired; create a new API token at the link below.",
        JIRA_API_TOKENS,
    )
    sys.exit(4)


def verify_mode(repo_root: Path) -> None:
    """Validate .dmtools/jira.cfg exists and has mandatory keys in correct format."""
    cfg_path = repo_root / ".dmtools" / "jira.cfg"
    if not cfg_path.is_file():
        err(
            f"Missing required file: {cfg_path}",
            f"Run from repo root: {SCRIPT_INVOKE} (without --verify) to validate JIRA connection and create jira.cfg.",
            DMTOOLS_README,
        )
        sys.exit(1)

    try:
        data = json.loads(cfg_path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as e:
        err(
            f"Invalid JSON in {cfg_path}: {e}",
            f"Re-run: {SCRIPT_INVOKE} to regenerate a valid jira.cfg.",
        )
        sys.exit(1)

    for key in JIRA_CFG_MANDATORY_KEYS:
        if key not in data:
            err(
                f"Missing required key in {cfg_path}: {key}",
                f"Re-run: {SCRIPT_INVOKE} to refresh jira.cfg.",
            )
            sys.exit(1)

    if not isinstance(data.get("jira_base_path"), str) or not data["jira_base_path"].strip():
        err(
            f"Invalid format in {cfg_path}: jira_base_path must be a non-empty string.",
            f"Re-run: {SCRIPT_INVOKE} to refresh jira.cfg.",
        )
        sys.exit(1)
    if not isinstance(data.get("account_id"), str) or not data["account_id"].strip():
        err(
            f"Invalid format in {cfg_path}: account_id must be a non-empty string.",
            f"Re-run: {SCRIPT_INVOKE} to refresh jira.cfg.",
        )
        sys.exit(1)

    validated_at = data.get("validated_at")
    if not isinstance(validated_at, str) or not validated_at.strip():
        err(
            f"Invalid format in {cfg_path}: validated_at must be a non-empty ISO 8601 string.",
            f"Re-run: {SCRIPT_INVOKE} to refresh jira.cfg.",
        )
        sys.exit(1)
    try:
        datetime.fromisoformat(validated_at.replace("Z", "+00:00"))
    except ValueError:
        err(
            f"Invalid format in {cfg_path}: validated_at must be parseable as ISO 8601 date.",
            f"Re-run: {SCRIPT_INVOKE} to refresh jira.cfg.",
        )
        sys.exit(1)

    print("OK: .dmtools/jira.cfg is present and valid.")
    sys.exit(0)


def extract_account_id(stdout: str) -> str | None:
    """Parse accountId from jira_get_my_profile JSON (top-level or result wrapper)."""
    try:
        data = json.loads(stdout)
    except json.JSONDecodeError:
        return None
    if isinstance(data.get("accountId"), str):
        return data["accountId"]
    if isinstance(data.get("result"), dict) and isinstance(data["result"].get("accountId"), str):
        return data["result"]["accountId"]
    return None


def has_jira_tools(stdout: str) -> bool:
    """Check dmtools list output for any jira_ tool."""
    try:
        data = json.loads(stdout)
    except json.JSONDecodeError:
        return False
    tools = data.get("tools") if isinstance(data.get("tools"), list) else data if isinstance(data, list) else []
    return any(
        isinstance(t, dict) and str(t.get("name", "")).startswith("jira_")
        or isinstance(t, str) and t.startswith("jira_")
        for t in tools
    )


def full_validation(repo_root: Path, project_key: str | None) -> None:
    """Run full connection checks and write .dmtools/jira.cfg on success."""
    code, out, err_out = run_dmtools(repo_root, "--version")
    if code != 0:
        fail_dmtools_not_found()

    code, out, err_out = run_dmtools(repo_root, "list")
    if code != 0 or not has_jira_tools(out):
        fail_no_jira_config()

    code, out, err_out = run_dmtools(repo_root, "jira_get_my_profile")
    if code != 0:
        fail_auth()
    account_id = extract_account_id(out)
    if not account_id:
        err(
            "jira_get_my_profile succeeded but response did not contain accountId.",
            f"Check dmtools output. Ensure {REQUIRED_ENV_VARS} are set in dmtools.env.",
            DMTOOLS_README,
        )
        sys.exit(4)

    # Optional: get JIRA_BASE_PATH from dmtools.env for jira.cfg
    jira_base_path = ""
    env_file = repo_root / "dmtools.env"
    if env_file.is_file():
        for line in env_file.read_text(encoding="utf-8").splitlines():
            m = re.match(r"^\s*JIRA_BASE_PATH\s*=\s*(.+)$", line)
            if m:
                jira_base_path = m.group(1).strip().strip('"')
                break
    if not jira_base_path:
        jira_base_path = os.environ.get("JIRA_BASE_PATH", "").strip()
    if not jira_base_path:
        jira_base_path = "unknown"

    cfg = {
        "validated_at": datetime.now(tz=timezone.utc).isoformat(),
        "jira_base_path": jira_base_path,
        "account_id": account_id,
    }

    # Optional: profile cache
    try:
        cfg["profile"] = json.loads(out)
    except json.JSONDecodeError:
        pass

    # Optional: project metadata
    if project_key:
        projects = cfg.setdefault("projects", {})
        proj = {}
        for cmd, key in [
            ("jira_get_issue_types", "issue_types"),
            ("jira_get_project_statuses", "statuses"),
            ("jira_get_components", "components"),
            ("jira_get_fields", "fields"),
        ]:
            code, out, _ = run_dmtools(repo_root, cmd, project_key)
            if code == 0 and out.strip():
                try:
                    proj[key] = json.loads(out)
                except json.JSONDecodeError:
                    proj[key] = out.strip()
        projects[project_key] = proj

    out_dir = repo_root / ".dmtools"
    out_dir.mkdir(parents=True, exist_ok=True)
    cfg_path = out_dir / "jira.cfg"
    cfg_path.write_text(json.dumps(cfg, indent=2), encoding="utf-8")
    print(f"OK: JIRA connection validated. Wrote {cfg_path}")
    sys.exit(0)


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Validate JIRA connection and write .dmtools/jira.cfg, or only verify jira.cfg (--verify)."
    )
    parser.add_argument(
        "--verify",
        action="store_true",
        help="Only validate that .dmtools/jira.cfg exists and has mandatory keys; do not call dmtools.",
    )
    parser.add_argument(
        "--project",
        metavar="PROJECT_KEY",
        help="Optional project key to fetch and cache issue types, statuses, components, fields into jira.cfg.",
    )
    args = parser.parse_args()

    repo_root = find_repo_root()
    if args.verify:
        verify_mode(repo_root)
    full_validation(repo_root, args.project)


if __name__ == "__main__":
    main()
