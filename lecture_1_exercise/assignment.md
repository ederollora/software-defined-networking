# Mininet Ring Topology Assignment

## Title

Mininet Ring Topology with Three Switches

## Due Date

Before Lecture 2

## Instructions

In this exercise, you will create a Mininet topology representing a ring of three switches. Each switch must have two hosts connected to it.

The topology must satisfy these requirements:

- Three switches: `s1`, `s2`, and `s3`
- Six hosts total: two hosts per switch
- Host-to-switch links with 10 Mbps bandwidth
- Switch-to-switch ring links with 30 Mbps bandwidth
- Connectivity test between all hosts

The ring links are:

- `s1 <-> s2`
- `s2 <-> s3`
- `s3 <-> s1`

The access links are:

- `s1 <-> h1, h2`
- `s2 <-> h3, h4`
- `s3 <-> h5, h6`

Use `ring_challenge.py`.

In this version, some parts are left as TODOs, again:

- Add all three switches.
- Add all six hosts.
- Connect two hosts to each switch using 10 Mbps links.
- Connect the switches in a ring using 30 Mbps links.
- Enable STP on all switches, this is the key.
- Start the network and test connectivity with `net.pingAll()`.

Run it with:

```bash
sudo python3 ring_challenge.py
```

## Expected Result

FIRST: If you run as it is, without any touching, will fail the `ring_challenge.py`.

SECOND: When you start changing the topology, it will be, or not, working. All hosts SHOULD be able to reach each other.

If the first pingall() test fails, wait a few more seconds and try again from the Mininet CLI.

Let me know if something is wrong.

