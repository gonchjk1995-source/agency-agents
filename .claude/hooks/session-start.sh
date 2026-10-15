#!/bin/bash
# Installs this repo's agents into ~/.claude/agents/ at the start of every
# Claude Code cloud session, so subagents (e.g. engineering-frontend-developer)
# are available without a manual install.sh run each time.
set -euo pipefail

if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

"$CLAUDE_PROJECT_DIR/scripts/install.sh" --tool claude-code --no-interactive
