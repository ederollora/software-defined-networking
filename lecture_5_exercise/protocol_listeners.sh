#!/bin/bash

echo "Starting protocol listeners on h2..."
echo "======================================"

echo "UDP 53  - DNS"
nc -u -lk -p 8881 &
PID1=$!

echo "TCP 23  - Telnet"
nc -lk -p 8882 &
PID2=$!

echo "TCP 22  - SSH"
nc -lk -p 8883 &
PID3=$!

echo "UDP 23"
nc -u -lk -p 8884
 &
PID4=$!

echo
echo "Listeners started."
echo "UDP/53  PID: $PID1"
echo "TCP/23  PID: $PID2"
echo "TCP/22  PID: $PID3"
echo "UDP/23  PID: $PID4"
echo
echo "Press Ctrl+C to stop all listeners."

trap 'kill $PID1 $PID2 $PID3 $PID4 2>/dev/null; exit' INT TERM

wait
