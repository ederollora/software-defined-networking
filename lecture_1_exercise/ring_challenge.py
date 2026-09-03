#!/usr/bin/env python3

import time

from mininet.cli import CLI
from mininet.link import TCLink
from mininet.log import setLogLevel
from mininet.net import Mininet
from mininet.node import OVSController


def ring_topology():
    """Complete this topology so it matches the assignment requirements."""
    net = Mininet(controller=OVSController, link=TCLink)

    net.addController("c0")

    # TODO 1: Add three switches named s1, s2, and s3.
    # Example:
    # s1 = net.addSwitch("s1")

    # TODO 2: Add six hosts named h1 through h6.
    # Example:
    # h1 = net.addHost("h1")

    # TODO 3: Add access links.
    # h1 and h2 should connect to s1.
    # h3 and h4 should connect to s2.
    # h5 and h6 should connect to s3.
    # Each access link should have bw=10.

    # TODO 4: Add ring links.
    # Connect s1 to s2, s2 to s3, and s3 to s1.
    # Each ring link should have bw=30.

    net.start()

    # TODO 5: Enable STP on every switch.
    # Example:
    # Use a `for switch in switches`
    # inside: switch.cmd("ovs-vsctl set bridge switch stp_enable=true")

    print("Waiting 50 seconds for STP to converge...")
    time.sleep(50)

    net.pingAll()
    CLI(net)
    net.stop()


if __name__ == "__main__":
    setLogLevel("info")
    ring_topology()

