#!/usr/bin/env bash
# setup-eas-secrets.sh
# One-time helper to push all API keys to EAS Secret Store.
# 1. Copy .env.secrets.example to .env.secrets
# 2. Fill in your real values in .env.secrets (it is gitignored — NEVER commit it)
# 3. Run: bash scripts/setup-eas-secrets.sh
# This registers each key as a project-scoped EAS Secret.
# EAS injects them into every build as ORG_GRADLE_PROJECT_* env vars,
# which Gradle reads automatically via project.findProperty().

set -euo pipefail

SECRETS_FILE="${1:-.env.secrets}"

if [[ ! -f "$SECRETS_FILE" ]]; then
  echo "ERROR: $SECRETS_FILE not found."
  echo "Copy .env.secrets.example to .env.secrets, fill in values, then re-run."
  exit 1
fi

echo "Pushing secrets from $SECRETS_FILE to EAS project scope..."

while IFS='=' read -r key value; do
  # Skip comments and blank lines
  [[ "$key" =~ ^[[:space:]]*# ]] && continue
  [[ -z "$key" ]] && continue

  key="$(echo "$key" | xargs)"    # trim whitespace
  value="$(echo "$value" | xargs)" # trim whitespace

  if [[ -z "$value" || "$value" == "REPLACE_ME" ]]; then
    echo "  SKIP  $key  (no value set)"
    continue
  fi

  echo "  PUSH  $key"
  eas secret:create \
    --scope project \
    --name "$key" \
    --value "$value" \
    --force 2>/dev/null || \
  eas secret:update \
    --scope project \
    --name "$key" \
    --value "$value"
done < "$SECRETS_FILE"

echo ""
echo "Done. Verify with: eas secret:list --scope project"
