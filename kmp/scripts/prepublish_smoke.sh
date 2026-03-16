#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
GRADLEW="$ROOT_DIR/gradlew"

if [[ ! -x "$GRADLEW" ]]; then
  echo "gradlew not found or not executable at: $GRADLEW" >&2
  exit 1
fi

DRY_RUN="${1:-}"
RUNNER=("$GRADLEW" -p "$ROOT_DIR" --no-daemon)
TASKS_JVM_BASELINE=(
  ":lanterna-core-kmp:check"
  ":lanterna-core-kmp:dokkaHtml"
  ":lanterna-core-kmp:publishJvmPublicationToMavenLocal"
)
TASKS_FULL_NATIVE=(
  ":lanterna-core-kmp:check"
  ":lanterna-core-kmp:dokkaHtml"
  ":lanterna-core-kmp:publishToMavenLocal"
)

TASKS=("${TASKS_JVM_BASELINE[@]}")
if [[ "$DRY_RUN" == "--full-native" ]]; then
  TASKS=("${TASKS_FULL_NATIVE[@]}")
fi

echo "Prepublication smoke checks:"
for task in "${TASKS[@]}"; do
  echo " - $task"
done

if [[ "$DRY_RUN" == "--dry-run" ]]; then
  echo "Dry run only; no Gradle task executed."
  exit 0
fi

"${RUNNER[@]}" "${TASKS[@]}"
echo "Prepublication smoke checks completed successfully."
