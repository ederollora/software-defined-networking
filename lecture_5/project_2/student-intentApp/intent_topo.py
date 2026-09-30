#!/usr/bin/env python3

from mininet.net import Mininet
from mininet.node import RemoteController, OVSSwitch
from mininet.cli import CLI
from mininet.log import setLogLevel, info


def run():

    net = Mininet(
        controller=None,
        switch=OVSSwitch,
        build=False
    )

    # ONOS controller
    c0 = net.addController(
        'c0',
        controller=RemoteController,
        ip='127.0.0.1',
        port=6653
    )

    # Switches
    s1 = net.addSwitch(
        's1',
        dpid='0000000000000001',
        protocols='OpenFlow13'
    )

    s2 = net.addSwitch(
        's2',
        dpid='0000000000000002',
        protocols='OpenFlow13'
    )

    s3 = net.addSwitch(
        's3',
        dpid='0000000000000003',
        protocols='OpenFlow13'
    )

    s4 = net.addSwitch(
        's4',
        dpid='0000000000000004',
        protocols='OpenFlow13'
    )

    # Hosts
    h1 = net.addHost(
        'h1',
        ip='10.0.0.1/24',
        mac='00:00:00:00:00:01'
    )

    h2 = net.addHost(
        'h2',
        ip='10.0.0.2/24',
        mac='00:00:00:00:00:02'
    )

    # -------------------------------------------------
    # Explicit port assignments
    # -------------------------------------------------

    # h1 -- s1
    # s1 port 1
    net.addLink(
        h1, s1,
        port2=1
    )

    # s1 -- s2
    # s1 port 2
    # s2 port 1
    net.addLink(
        s1, s2,
        port1=2,
        port2=1
    )

    # s1 -- s3
    # s1 port 3
    # s3 port 1
    net.addLink(
        s1, s3,
        port1=3,
        port2=1
    )

    # s2 -- s4
    # s2 port 2
    # s4 port 2
    net.addLink(
        s2, s4,
        port1=2,
        port2=2
    )

    # s3 -- s4
    # s3 port 2
    # s4 port 3
    net.addLink(
        s3, s4,
        port1=2,
        port2=3
    )

    # h2 -- s4
    # s4 port 1
    net.addLink(
        h2, s4,
        port2=1
    )

    # Build network
    info("*** Building network\n")
    net.build()

    # Start controller
    info("*** Starting ONOS controller\n")
    c0.start()

    # Start switches
    info("*** Starting switches\n")
    for sw in net.switches:
        sw.start([c0])

    info("\n*** Port mapping ***\n")
    info("s1: 1=h1, 2=s2, 3=s3\n")
    info("s2: 1=s1, 2=s4\n")
    info("s3: 1=s1, 2=s4\n")
    info("s4: 1=h2, 2=s2, 3=s3\n\n")

    info("*** Path 1: h1-s1-s2-s4-h2\n")
    info("*** Path 2: h1-s1-s3-s4-h2\n\n")

    CLI(net)

    net.stop()


if __name__ == '__main__':
    setLogLevel('info')
    run()