#!/bin/bash

# Check destination IP argument
if [ -z "$1" ]; then
    echo "Usage: $0 <destination-ip>"
    echo "Example: $0 10.0.0.2"
    exit 1
fi

TARGET="$1"

echo "Testing traffic to $TARGET"
echo "========================================"

echo
echo "[1] DNS-like traffic - UDP destination port 53"
echo "DNS TEST" | nc -u -w 2 "$TARGET" 8881
echo "UDP/53 packet sent."
echo "Check h2 to confirm whether it was actually received."

echo
echo "[2] Telnet-like traffic - TCP destination port 23"
echo "TELNET TEST" | nc -w 3 "$TARGET" 8882

if [ $? -eq 0 ]; then
    echo "TCP/23 CONNECTED"
else
    echo "TCP/23 FAILED - blocked, refused, or timed out"
fi

echo
echo "[3] SSH-like traffic - TCP destination port 22"
echo "SSH TEST" | nc -w 3 "$TARGET" 8883

if [ $? -eq 0 ]; then
    echo "TCP/22 CONNECTED"
else
    echo "TCP/22 FAILED - blocked, refused, or timed out"
fi

echo
echo "[4] UDP destination port 23"
echo "UDP/23 TEST" | nc -u -w 2 "$TARGET" 8884
echo "UDP/23 packet sent."
echo "Check h2 to confirm whether it was actually received."

echo
echo "========================================"
echo "Done."
