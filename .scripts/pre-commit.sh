#!/bin/sh

# =============================================================================
# configs/pre-commit/v1.4.0
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
Pre-Commit Check(s):
===============================================================================
"
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
# Formatting Check
# =============================================================================
printf "[${YELLOW}%s${NONE}]\tChecking... " "Format"
STDOUT=$(make format-diff-check 2>&1)
if [ "$?" -ne 0 ]; then
	printf "${RED}%s${NONE} - %s\n" "Failed!" "There are files with dirty format. Run formatting and restage."

	printf "\n${RED}%s${NONE}\n\n" "$STDOUT"

	printf "Hint:\t${CYAN}%s${NONE}\n" "make format"
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
printf "%s\n" "==============================================================================="
