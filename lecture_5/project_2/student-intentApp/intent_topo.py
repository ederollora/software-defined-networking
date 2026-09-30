#!/usr/bin/env python3

from mininet.net import Mininet
from mininet.node import RemoteController, OVSSwitch
from mininet.cli import CLI
from mininet.log import setLogLevel, info


def run():

    net = Mininet(
        controller=None,
        switch=OVSSwitch,
        autoSetMacs=True,
        build=False
    )

    info("*** Adding ONOS controller\n")
    c0 = net.addController(
        name='c0',
        controller=RemoteController,
        ip='127.0.0.1',
        port=6653
    )

    info("*** Adding switches\n")

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

    info("*** Adding hosts\n")

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

    info("*** Adding host links\n")

    net.addLink(h1, s1)
    net.addLink(h2, s4)

    info("*** Adding switch links\n")

    # Upper path
    net.addLink(s1, s2)
    net.addLink(s2, s4)

    # Lower path
    net.addLink(s1, s3)
    net.addLink(s3, s4)

    info("*** Building network\n")
    net.build()

    info("*** Starting controller\n")
    c0.start()

    info("*** Starting switches\n")

    for switch in net.switches:
        switch.start([c0])

    info("*** Network started\n")
    info("*** Two paths exist between h1 and h2:\n")
    info("    h1 - s1 - s2 - s4 - h2\n")
    info("    h1 - s1 - s3 - s4 - h2\n")

    info("*** Opening Mininet CLI\n")
    CLI(net)

    info("*** Stopping network\n")
    net.stop()


if __name__ == '__main__':
    setLogLevel('info')
    run()