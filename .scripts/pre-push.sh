#!/bin/sh

# =============================================================================
# configs/pre-push/v1.2.0
# =============================================================================

printf "===============================================================================
Pre-Push Check(s):
===============================================================================
"
# =============================================================================
# Branch Check
# =============================================================================
printf "[\033[0;33m%s\033[0m]\t\tChecking... " "Branch"
if git branch --show-current | grep -q -E "\bmain\b|\bmaster\b|\bproject\b|\bproduction\b|\bdevelop\b|\bfeature\b"; then
  printf '\033[0;31m%s\033[0m' "Failed!"
  printf '%s\n' " - $(git branch --show-current) is a protected branch."
  printf '\n\033[0;31m'
  exit 1
fi
printf '\033[0;32m%s\033[0m\n' "Passed."
# =============================================================================
# Staging Check
# =============================================================================
printf "[\033[0;33m%s\033[0m]\t\tChecking... " "Staging"
if ! git diff --quiet; then
  printf '\033[0;31m%s\033[0m' "Failed!"
  printf '%s\n' "	- The working directory has unstaged file changes."
  printf '\n\033[0;31m'
  git diff --shortstat
  printf '\033[0m\n'
  printf '%s\n\n' "This may lead to a false positive during pre-push validation.
Checks may pass because of changes present in your local repository that will not be included in the push.

Stash or stage the changes."
  printf 'Hint:\t\033[0;36m%s\033[0m\n' "git add ." "git stash --keep-index"
  exit 1
fi
printf '\033[0;32m%s\033[0m\n' "Passed."
# =============================================================================
# Build Check
# =============================================================================
printf "[\033[0;33m%s\033[0m]\t\tChecking... " "Building"
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
# Test Check
# =============================================================================
printf "[\033[0;33m%s\033[0m]\t\t\tChecking...\n" "Test"
# Ordered by execution time, desc.
make test-smoke > /dev/null 2>&1 &
SMOKE_PID=$!
make test-integration > /dev/null 2>&1 &
INTEGRATION_PID=$!
make test-unit > /dev/null 2>&1 &
UNIT_PID=$!
# -----------------------------------------------------------------------------
# Test Reporting
# -----------------------------------------------------------------------------
wait $UNIT_PID; UNIT_EXIT_CODE=$?
printf "\t[\033[0;33m%s\033[0m]\t\t" "Unit-Test"
if [ "$UNIT_EXIT_CODE" -ne 0 ]; then
  printf '\033[0;31m%s\033[0m' "Failed!"
  printf '%s\n' " - There are Unit-Test failures; the build is unstable. Resolve and restage."
else
  printf '\033[0;32m%s\033[0m\n' "Passed."
fi
# -----------------------------------------------------------------------------
wait $INTEGRATION_PID; INTEGRATION_EXIT_CODE=$?
printf "\t[\033[0;33m%s\033[0m]\t" "Integration-Test"
if [ "$INTEGRATION_EXIT_CODE" -ne 0 ]; then
  printf '\033[0;31m%s\033[0m' "Failed!"
  printf '%s\n' " - There are Integration-Test failures; the build is unstable. Resolve and restage."
else
  printf '\033[0;32m%s\033[0m\n' "Passed."
fi
# -----------------------------------------------------------------------------
wait $SMOKE_PID; SMOKE_EXIT_CODE=$?
printf "\t[\033[0;33m%s\033[0m]\t\t" "Smoke-Test"
if [ "$SMOKE_EXIT_CODE" -ne 0 ]; then
  printf '\033[0;31m%s\033[0m' "Failed!"
  printf '%s\n' " - There are Smoke-Test failures; the build is unstable. Resolve and restage."
else
  printf '\033[0;32m%s\033[0m\n' "Passed."
fi
# -----------------------------------------------------------------------------

if [ "$UNIT_EXIT_CODE" -ne 0 ] || [ "$INTEGRATION_EXIT_CODE" -ne 0 ] || [ "$SMOKE_EXIT_CODE" -ne 0 ]; then
  exit 1
fi

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
