#!/bin/sh

# =============================================================================
# configs/pre-commit/v1.3.0
# =============================================================================

printf "===============================================================================
Pre-Commit Check(s):
===============================================================================
"
# =============================================================================
# Staging Check
# =============================================================================
printf "[\033[0;33m%s\033[0m]\tChecking... " "Staging"
if ! git diff --quiet; then
  printf '\033[0;31m%s\033[0m' "Failed!"
  printf '%s\n' "	- The working directory has unstaged file changes."
  printf '\n\033[0;31m'
  git diff --shortstat
  printf '\033[0m\n'
  printf '%s\n\n' "This may lead to a false positive during pre-commit validation.
Checks may pass because of changes present in your local repository that will not be included in the commit.

Stash or stage the changes."
  printf 'Hint:\t\033[0;36m%s\033[0m\n' "git add ." "git stash --keep-index"
  exit 1
fi
printf '\033[0;32m%s\033[0m\n' "Passed."
# =============================================================================
# Formatting Check
# =============================================================================
printf "[\033[0;33m%s\033[0m]\tChecking... " "Formatting"
STDOUT=$(make format-diff-check)
EXIT_CODE=$?
if [ "$EXIT_CODE" -ne 0 ]; then
	printf '\033[0;31m%s\033[0m' "Failed!"
	printf '%s\n' "	- There are files with dirty format. Run formatting and restage."
	printf '\n\033[0;31m'
	printf '%s\n' "$STDOUT"
	printf '\033[0m\n'

	printf 'Hint:\t\033[0;36m%s\033[0m\n'"make format"
  exit 1
fi
printf '\033[0;32m%s\033[0m\n' "Passed."
# =============================================================================
# Build Check
# =============================================================================
printf "[\033[0;33m%s\033[0m]\tChecking... " "Building"
STDOUT=$(make build)
EXIT_CODE=$?
if [ "$EXIT_CODE" -ne 0 ]; then
	printf '\033[0;31m%s\033[0m' "Failed!"
	printf '%s\n' "	- The build failed. Resolve and restage."
	printf '\n\033[0;31m'
	printf '%s\n' "$STDOUT"
	printf '\033[0m\n'

	printf 'Hint:\t\033[0;36m%s\033[0m\n'"make build"
  exit 1
fi
printf '\033[0;32m%s\033[0m\n' "Passed."
# =============================================================================
# ANSI Color Escape Codes
# =============================================================================
# YELLOW='\033[0;33m'
# RED='\033[0;31m'
# GREEN='\033[0;32m'
# CYAN='\033[0;36m'
# BLUE='\033[0;34m'
# NONE='\033[0m'
# =============================================================================
printf '%s\n' "==============================================================================="
