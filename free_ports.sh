#!/bin/bash

# Define the docker-compose file
COMPOSE_FILE="docker-compose.yml"

if [ ! -f "$COMPOSE_FILE" ]; then
    echo "Error: $COMPOSE_FILE not found."
    exit 1
fi

echo "Extracting ports from $COMPOSE_FILE..."

# Extract published ports (host side) using grep/sed. 
# Looks for patterns like: - "8081:8081" or - 8081:8081
PORTS=$(grep -E '^\s*-\s*"?([0-9]+):([0-9]+)"?' "$COMPOSE_FILE" | sed -E 's/.*- "?([0-9]+):.*/\1/')

if [ -z "$PORTS" ]; then
    echo "No ports found in $COMPOSE_FILE."
    exit 0
fi

echo "Found ports: $PORTS"

for PORT in $PORTS; do
    echo "Checking port $PORT..."
    
    # Find PID using lsof (works on macOS/Linux)
    # -t: terse (only PIDs)
    # -i tcp:PORT: select IPv[46] TCP files at this port
    PIDS=$(lsof -ti tcp:$PORT)
    
    if [ -n "$PIDS" ]; then
        echo "Port $PORT is occupied by PID(s): $PIDS"
        
        # Check if it's a Docker process to be polite
        COMMANDS=$(ps -p $PIDS -o comm=)
        echo "Process names: $COMMANDS"
        
        echo "Closing port $PORT (killing PIDS: $PIDS)..."
        kill -9 $PIDS
        
        if [ $? -eq 0 ]; then
            echo "Successfully freed port $PORT."
        else
            echo "Failed to free port $PORT."
        fi
    else
        echo "Port $PORT is already free."
    fi
done

echo "Port check and cleanup complete."
