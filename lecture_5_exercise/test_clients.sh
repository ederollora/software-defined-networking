#!/bin/bash

# Check destination IP argument
if [ -z "$1" ]; then
    echo "Usage: $0 <destination-ip>"
    echo "Example: $0 10.0.0.2"
    exit 1
fi

TARGET="$1"

echo "Testing client traffic to $TARGET"
echo "========================================"

echo
echo "[1] DNS - UDP port 53"
echo "test" | nc -u -w 2 "$TARGET" 53

if [ $? -eq 0 ]; then
    echo "UDP/53 packet sent successfully"
else
    echo "UDP/53 failed"
fi

echo
echo "[2] Telnet - TCP port 23"
nc -vz -w 3 "$TARGET" 23
RESULT=$?

if [ $RESULT -eq 0 ]; then
    echo "TCP/23 CONNECTED"
elif [ $RESULT -eq 1 ]; then
    echo "TCP/23 FAILED - blocked, closed, refused, or timed out"
else
    echo "TCP/23 FAILED"
fi

echo
echo "[3] SSH - TCP port 20"
nc -vz -w 3 "$TARGET" 20
RESULT=$?

if [ $RESULT -eq 0 ]; then
    echo "TCP/20 CONNECTED"
elif [ $RESULT -eq 1 ]; then
    echo "TCP/20 FAILED - blocked, closed, refused, or timed out"
else
    echo "TCP/20 FAILED"
fi

echo
echo "[4] UDP port 23"
echo "test" | nc -u -w 2 "$TARGET" 23

if [ $? -eq 0 ]; then
    echo "UDP/23 packet sent successfully"
else
    echo "UDP/23 failed"
fi

echo
echo "========================================"
echo "Done."
