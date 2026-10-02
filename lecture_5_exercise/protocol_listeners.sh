#!/bin/bash

echo "Starting listeners on h2..."

nc -u -lk -p 53 > /tmp/udp53.log 2>&1 &
echo "UDP/53 listener started (DNS-like)"

nc -lk -p 23 > /tmp/tcp23.log 2>&1 &
echo "TCP/23 listener started (Telnet-like)"

nc -lk -p 20 > /tmp/tcp22.log 2>&1 &
echo "TCP/20 listener started (SSH-like)"
echo "We put SSH on port 20, because SSH (default) is already listening on port 22."

nc -u -lk -p 23 > /tmp/udp23.log 2>&1 &
echo "UDP/23 listener started"

echo
echo "All listeners started."
