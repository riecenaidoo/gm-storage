#!/bin/sh

# =============================================================================
# configs/pre-push/v1.3.0
# =============================================================================
# ANSI Color Escape Codes
# =============================================================================
YELLOW="\033[0;33m"
RED="\033[0;31m"
GREEN="\033[0;32m"
CYAN="\033[0;36m"
BLUE="\033[0;34m"
NONE="\033[0m"
# =============================================================================

printf "===============================================================================
Pre-Push Check(s):
===============================================================================
"
# =============================================================================
# Branch Check
# =============================================================================
printf "[${YELLOW}%s${NONE}]\tChecking... " "Branch"
if git branch --show-current | grep -q -E "\bmain\b|\bmaster\b|\bproject\b|\bproduction\b|\bdevelop\b|\bfeature\b"; then
  printf "${RED}%s${NONE} - %s\n" "Failed!" "$(git branch --show-current) is a protected branch."
  exit 1
fi
printf "${GREEN}%s${NONE}\n" "Passed."
# =============================================================================
# Staging Check
# =============================================================================
printf "[${YELLOW}%s${NONE}]\tChecking... " "Staging"
if ! git diff --quiet || [ -n "$(git ls-files --others --exclude-standard)" ]; then
	printf "${RED}%s${NONE} - %s\n" "Failed!" "The working directory has unstaged changes."

	printf "\n"
	git status -s	# Call directly 'printf "\n%s\n" "git status -s"' will not preserve Git output formatting.
	printf "\n"

	printf "%s\n\n" "This may lead to a false positive during validation.
Checks may pass due to changes that are only present in your local working directory.

Stage or stash the changes."
	printf "Hint:\t${CYAN}%s${NONE}\n" "git add ." "git stash -u --keep-index"
	exit 1
fi
printf "${GREEN}%s${NONE}\n" "Passed."
# =============================================================================
# Build Check
# =============================================================================
printf "[${YELLOW}%s${NONE}]\t\tChecking... " "Build"
STDOUT=$(make build 2>&1)
if [ "$?" -ne 0 ]; then
	printf "${RED}%s${NONE} - %s\n" "Failed!" "The build failed. Resolve and restage."

	printf "\n${RED}%s${NONE}\n\n" "$STDOUT"

	printf "Hint:\t${CYAN}%s${NONE}\n" "make build"
	exit 1
fi
printf "${GREEN}%s${NONE}\n" "Passed."
# =============================================================================
# Test Check
# =============================================================================
printf "[${YELLOW}%s${NONE}]\t\tChecking...\n" "Test"
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
printf " > [${YELLOW}%s${NONE}]\t\t    " "Unit-Test"
wait $UNIT_PID; UNIT_EXIT_CODE=$?
if [ "$UNIT_EXIT_CODE" -ne 0 ]; then
  printf "${RED}%s${NONE} - %s\n" "Failed!" "There are Unit-Test failures; the build is unstable. Resolve and restage."
else
  printf "${GREEN}%s${NONE}\n" "Passed."
fi
# -----------------------------------------------------------------------------
printf " > [${YELLOW}%s${NONE}]\t    " "Integration-Test"
wait $INTEGRATION_PID; INTEGRATION_EXIT_CODE=$?
if [ "$INTEGRATION_EXIT_CODE" -ne 0 ]; then
  printf "${RED}%s${NONE} - %s\n" "Failed!" "There are Integration-Test failures; the build is unstable. Resolve and restage."
else
  printf "${GREEN}%s${NONE}\n" "Passed."
fi
# -----------------------------------------------------------------------------
printf " > [${YELLOW}%s${NONE}]\t\t    " "Smoke-Test"
wait $SMOKE_PID; SMOKE_EXIT_CODE=$?
if [ "$SMOKE_EXIT_CODE" -ne 0 ]; then
  printf "${RED}%s${NONE} - %s\n" "Failed!" "There are Smoke-Test failures; the build is unstable. Resolve and restage."
else
  printf "${GREEN}%s${NONE}\n" "Passed."
fi
# -----------------------------------------------------------------------------

if [ "$UNIT_EXIT_CODE" -ne 0 ] || [ "$INTEGRATION_EXIT_CODE" -ne 0 ] || [ "$SMOKE_EXIT_CODE" -ne 0 ]; then
  exit 1
fi
# =============================================================================
printf "%s\n" "==============================================================================="
