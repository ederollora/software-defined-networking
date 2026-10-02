#!/usr/bin/python

from mininet.net import Mininet
from mininet.node import Controller, RemoteController, OVSController
from mininet.node import CPULimitedHost, Host, Node
from mininet.node import OVSKernelSwitch, UserSwitch
from mininet.node import IVSSwitch
from mininet.cli import CLI
from mininet.log import setLogLevel, info
from mininet.link import TCLink, Intf
from subprocess import call

def myNetwork():

    net = Mininet( topo=None,
                   build=False,
                   ipBase='10.0.0.0/8')

    info( '*** Adding controller\n' )
    c0=net.addController(name='c0',
                      controller=RemoteController,
                      ip='127.0.0.1',
                      protocol='tcp',
                      port=6653)

    info( '*** Add switches\n')

    s1 = net.addSwitch('s1', cls=OVSKernelSwitch, protocols='OpenFlow13')
    s2 = net.addSwitch('s2', cls=OVSKernelSwitch, protocols='OpenFlow13')
    s3 = net.addSwitch('s3', cls=OVSKernelSwitch, protocols='OpenFlow13')
    s4 = net.addSwitch('s4', cls=OVSKernelSwitch, protocols='OpenFlow13')
    s5 = net.addSwitch('s5', cls=OVSKernelSwitch, protocols='OpenFlow13')
    info( '*** Add hosts\n')
    h1 = net.addHost('h1', cls=Host, ip='10.0.0.1', mac='00:00:00:00:00:01', defaultRoute=None)
    h2 = net.addHost('h2', cls=Host, ip='10.0.0.2', mac='00:00:00:00:00:02', defaultRoute=None)

    info( '*** Add links\n')
    h1s1 = {'bw':200}
    net.addLink(h1, s1, cls=TCLink , **h1s1)
    s4h2 = {'bw':100}
    net.addLink(s4, h2, cls=TCLink , **s4h2)
    s1s2 = {'bw':100}
    net.addLink(s1, s2, cls=TCLink , **s1s2)
    s1s3 = {'bw':100}
    net.addLink(s1, s3, cls=TCLink , **s1s3)
    s3s4 = {'bw':100}
    net.addLink(s3, s4, cls=TCLink , **s3s4)
    s2s4 = {'bw':100}
    net.addLink(s2, s4, cls=TCLink , **s2s4)
    
        
    s1s5 = {'bw':100}
    net.addLink(s1, s5, cls=TCLink , **s1s5)
    s2s5 = {'bw':100}
    net.addLink(s2, s5, cls=TCLink , **s2s5)
    s3s5 = {'bw':100}
    net.addLink(s3, s5, cls=TCLink , **s3s5)
    s4s5 = {'bw':100}
    net.addLink(s4, s5, cls=TCLink , **s4s5)
    
    
              
    info( '*** Starting network\n')
    net.build()
    info( '*** Starting controllers\n')
    for controller in net.controllers:
        controller.start()
        print(controller)

    info( '*** Starting switches\n')
    net.get('s1').start([c0])
    net.get('s2').start([c0])
    net.get('s3').start([c0])
    net.get('s4').start([c0])
    #net.get('s5').start([c0])
    
      
    net.configLinkStatus('s1', 's5', 'down')
    net.configLinkStatus('s2', 's5', 'down')
    net.configLinkStatus('s3', 's5', 'down')
    net.configLinkStatus('s4', 's5', 'down')
      
    
    info( '*** Post configure switches and hosts\n')

    CLI(net)
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )
    myNetwork()
