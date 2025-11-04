#!/bin/sh

# =============================================================================
# configs/makefiles/v1.2.0
# =============================================================================

# Gracefully terminate a process and its children (by their shared process group).
PID="$1"

if kill -0 "$PID" 2>/dev/null; then
    if [ "$(uname -s)" = "Linux" ]; then
        # On Linux, child processes continue running after their parents die.
        # To stop them as well, we extract the process group id (PGID), and
        # configure the 'kill' argument (by prefixing '-') to send the
        # termination signal to the entire process group.
        PID=-$(ps -o pgid= -p "$PID" | xargs)
    fi
    # Send the termination signal (SIGTERM); allow the processes to terminate gracefully.
    kill -TERM "$PID"
fi
