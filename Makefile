# =============================================================================
# configs:/makefiles/v1.3.0;/java/v1.1.0
# =============================================================================
# ANSI Color Escape Codes
# =============================================================================
YELLOW=\033[0;33m
RED=\033[0;31m
GREEN=\033[0;32m
CYAN=\033[0;36m
BLUE=\033[0;34m
NONE=\033[0m
# =============================================================================

# See [7.2.1 General Conventions for Makefiles](https://www.gnu.org/prep/standards/html_node/Makefile-Basics.html)
# See [6.2.2 Simply Expanded Variable Assignment](https://www.gnu.org/software/make/manual/html_node/Simple-Assignment.html)
SHELL := /bin/sh

init: project git	## default (no-arg) target to initialise the Project and local repository

# See [7.2.6 Standard Targets for Users > 'all'](https://www.gnu.org/prep/standards/html_node/Standard-Targets.html)
all: init docker java	## primary target for creating all Project artifacts

start: docker serve	## start the Project (make stop)
	$(COMPOSE) start

stop: kill-serve	## stop the Project
	$(COMPOSE) stop

build: java	## build the Project

log:	## show logs of the Project
	tail $(MADE)/serve
	@printf "$(CYAN)\n%s\n$(NONE)" "(Streaming Mode) tail -f $(MADE)/serve"

.PHONY: init all start stop build log
# =============================================================================
# Environment Variables
# =============================================================================
# [6.2.4 Conditional Variable Assignment](https://www.gnu.org/software/make/manual/html_node/Conditional-Assignment.html)
# [6.10 Variables from the Environment](https://www.gnu.org/software/make/manual/html_node/Environment.html)
COMPOSE ?= docker compose
JAVA ?= java
MVN ?= mvn
# =============================================================================
# Script Macros
# =============================================================================
XARGS := xargs -0 --no-run-if-empty
PLAINTEXT_FILTER := $(XARGS) file --mime-type | awk -F: '/text\// { printf "%s\0", $$1 }'

STOP_PROCESS := ./.scripts/stop-process.sh

define stop_process	##> Given the PID file, stop the process
@if [ -f "$(1)" ]; then \
	$(XARGS) --arg-file "$(1)" "$(STOP_PROCESS)"; \
fi
endef
# =============================================================================
# Project
# =============================================================================
MADE := ./.made

project: $(MADE) $(MADE)/stop-script	##> alias for initialising the Project

$(MADE):
	mkdir $(MADE)

# See [4.3 Types of Prerequisites](https://www.gnu.org/software/make/manual/html_node/Prerequisite-Types.html) > order-only-prerequisites
$(MADE)/stop-script: $(STOP_PROCESS) | $(MADE)	##> mark scripts executable
	chmod +x $(STOP_PROCESS)
	touch $(MADE)/stop-script

rm-project:	##> remove all Project initialisation artifacts
	rm -f $(MADE)/stop-script
	@if [ -d $(MADE) ]; then \
		rmdir --ignore-fail-on-non-empty $(MADE); \
	fi

.PHONY: project rm-project
# =============================================================================
# Git
# - [Git Hooks](https://git-scm.com/book/ms/v2/Customizing-Git-Git-Hooks)
# =============================================================================
DIFF_FILES := git diff HEAD --diff-filter=ACM --name-only --relative -z
UNTRACKED_FILES := git ls-files --others --exclude-standard --full-name -z

git: .git/hooks/pre-commit .git/hooks/pre-push	##> alias for initialising the local repository; creates Git artifacts

.git/hooks/pre-commit: ./.scripts/pre-commit.sh	| $(MADE)	## updates the pre-commit hook in the local repository
	@if [ -f .git/hooks/pre-commit ]; then \
		cat .git/hooks/pre-commit >> $(MADE)/pre-commit; \
	fi
	cat .scripts/pre-commit.sh > .git/hooks/pre-commit
	chmod +x .git/hooks/pre-commit	# Ensure the script is executable.
	@printf '\n$(YELLOW)%s$(NONE)\n' "Pre-Commit Hook installed."
	@printf '\tHint:\t$(CYAN)%s$(NONE)\n' "rm .git/hooks/pre-commit" "make rm-git"

.git/hooks/pre-push: ./.scripts/pre-push.sh	| $(MADE)	## updates the pre-push hook in the local repository
	@if [ -f .git/hooks/pre-push ]; then \
		cat .git/hooks/pre-push >> $(MADE)/pre-push; \
	fi
	cat .scripts/pre-push.sh > .git/hooks/pre-push
	chmod +x .git/hooks/pre-push	# Ensure the script is executable.
	@printf '\n$(YELLOW)%s$(NONE)\n' "Pre-Push Hook installed."
	@printf '\tHint:\t$(CYAN)%s$(NONE)\n' "rm .git/hooks/pre-push" "make rm-git"

rm-git:	##> remove all Git artifacts produced by this script
	rm -f .git/hooks/pre-commit .git/hooks/pre-push
	@printf '\n$(YELLOW)%s$(NONE)\n' "Git Hook(s) removed."
	@printf '\tHint:\t$(CYAN)%s$(NONE) contains any overwritten existing Git hooks.\n' "$(MADE)"

.PHONY: git rm-git
# =============================================================================
# Docker
# =============================================================================
docker:	##> create all Docker infrastructure artifacts
	$(COMPOSE) create

image: $(MADE)/image-dev	## build the (dev) image

$(MADE)/image-dev: $(APP)
	docker build -t gm-storage:dev . -f ./.scripts/Dockerfile
	touch $(MADE)/image-dev

latest: $(MADE)/image-dev	## tag the (dev) image as (latest)
	docker tag gm-storage:dev gm-storage:latest

rm-docker:	##> remove all Docker artifacts produced by this script
	$(COMPOSE) down
	@printf '\nHint:\t$(CYAN)%s$(NONE)\t (Prune volume data)\n' "$(COMPOSE) down --volumes"
	rm -f $(MADE)/image-dev

.PHONY: docker image latest rm-docker
# =============================================================================
# Java
# =============================================================================
APP := application/target/application-1.0-SNAPSHOT.jar
SRC_FILES := $(shell find . -type f \( -name '*.java' -o -name '*.xml' -o -name '*.properties' \) -not -path "*/target/*")
LIB := ./.libs

java: $(APP)	##> alias for creating all Java artifacts

$(APP): $(SRC_FILES) pom.xml
	$(MVN) package -DskipTests

GJF_VERSION := 1.32.0
GJF := google-java-format-$(GJF_VERSION)-all-deps.jar

java-dev: $(LIB)/$(GJF)	##> alias for creating all Java artifacts for development

$(LIB):
	mkdir $(LIB)

$(LIB)/$(GJF): $(LIB)	##> download standalone GJF formatter
	curl -L -O --output-dir $(LIB) https://github.com/google/google-java-format/releases/download/v$(GJF_VERSION)/$(GJF)

rm-java: kill-serve	##> remove all Java artifacts produced by this script
	$(MVN) clean
	rm -f $(LIB)/$(GJF)
	@if [ -d $(LIB) ]; then \
		rmdir --ignore-fail-on-non-empty $(LIB); \
	fi
	rm -f $(MADE)/serve $(MADE)/serve.pid

serve: $(APP) kill-serve | $(MADE)	##> start the Java server
	$(JAVA) -jar $(APP) > $(MADE)/serve 2>&1 & echo $$! > $(MADE)/serve.pid

kill-serve:	##> kill the Java server process
	$(call stop_process,$(MADE)/serve.pid)

test: ## run all tests
	$(MVN) verify

test-unit:	##> run all unit tests (semantic.UnitTest)
	$(MVN) test -Dgroups="unit"

test-smoke:	##> run all smoke tests (semantic.SmokeTest)
	$(MVN) test -Dgroups="smoke"

test-integration:	##> run all integration tests (semantic.IntegrationTest), excluding external system tests (test-integration-ext)
	$(MVN) verify -Dgroups="integration" -DexcludedGroups="external"

test-integration-ext:	##> run all external integration tests (semantic.IntegrationTest#EXTERNAL)
	$(MVN) verify -Dgroups="external"

JAVA_FILTER := $(XARGS) awk -v RS='\0' '/\.java$$/'

.PHONY: java java-dev rm-java serve kill-serve test test-unit test-smoke test-integration test-integration-ext
# =============================================================================
# Formatting
# =============================================================================
FORMAT := $(JAVA_FILTER) | $(XARGS) $(JAVA) -jar $(LIB)/$(GJF) --replace
FORMAT_CHECK := $(JAVA_FILTER) | $(XARGS) $(JAVA) -jar $(LIB)/$(GJF) --set-exit-if-change

TRIM_CHECK := $(PLAINTEXT_FILTER) | $(XARGS) grep -lZ '[[:blank:]]$$'
TRIM := $(TRIM_CHECK) | $(XARGS) sed -i 's/[ \t]*$$//'

format: format-diff format-untracked	## alias to run formatting (format-diff) (format-untracked) rules
	git status -s

format-diff: java-dev	##> run formatting on modified (git diff HEAD) files
	$(DIFF_FILES) | $(TRIM)
	$(DIFF_FILES) | $(FORMAT)

format-diff-check: java-dev	##> check formatting on modified (git diff HEAD) files
	@TRAILING_WHITESPACE_FILES=$$($(DIFF_FILES) | $(TRIM_CHECK)); \
	if [ -n "$$TRAILING_WHITESPACE_FILES" ]; then \
		  printf '$(RED)%s$(NONE)' "Trailing Whitespaces!"; \
		  printf '\t- %s\n' "$$TRAILING_WHITESPACE_FILES"; \
		exit 1; \
	fi
	$(DIFF_FILES) | $(FORMAT_CHECK)

format-untracked: java-dev	##> run formatting on untracked files
	$(UNTRACKED_FILES) | $(TRIM)
	$(UNTRACKED_FILES) | $(FORMAT)

format-all: java-dev	##> run formatting on all files
	find . -maxdepth 1 -type f -print0 | $(TRIM)
	find .scripts/ application/ core/ web/ semantic/ -type f -print0 | $(TRIM)
	find . -type f -name '*.java' -print0 | $(FORMAT)

.PHONY: format format-diff format-untracked format-all
# =============================================================================
# Utilities
# =============================================================================
# See [7.2.6 Standard Targets for Users > 'clean'](https://www.gnu.org/prep/standards/html_node/Standard-Targets.html)
clean: rm-java rm-docker rm-git rm-project	## alias for cleaning up all artifacts produced by this Project

help:  ## show a summary of available targets
	@printf "%s\n" \
	"===============================================================================" \
	" General Commands" \
	"==============================================================================="
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
		| awk 'BEGIN {FS = ":.*?## "}; { \
			cmd = $$1; desc = $$2; \
			gsub(/\(([^)]*)\)/, "$(CYAN)&$(NONE)", desc); \
			printf "  $(BLUE)%-21s$(NONE) %s\n", cmd, desc \
		}'
	@printf "%s\n" \
	"==============================================================================="

help-ext:  ## show all available targets
	@printf "%s\n" \
	"===============================================================================" \
	"Available Commands" \
	"==============================================================================="
	@grep -E '^[a-zA-Z0-9_-]+:.*?##>? ' $(MAKEFILE_LIST) \
		| awk 'BEGIN {FS = ":.*?##>? "}; { \
			cmd = $$1; desc = $$2; \
			gsub(/\(([^)]*)\)/, "$(CYAN)&$(NONE)", desc); \
			printf "  $(BLUE)%-21s$(NONE) %s\n", cmd, desc \
		}'
	@printf "%s\n" \
	"==============================================================================="

.PHONY: clean help help-ext
# =============================================================================
