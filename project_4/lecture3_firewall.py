#!/usr/bin/python

from mininet.topo import Topo
from mininet.net import Mininet
from mininet.node import OVSSwitch
from mininet.node import CPULimitedHost
from mininet.link import TCLink
from mininet.util import dumpNodeConnections
from mininet.util import dumpNetConnections
from mininet.log import setLogLevel
from mininet.cli import CLI
from mininet.node import RemoteController


'''
This is the script to creat the topology for the firewall exercise in Lecture 3 from the 34359 SDN course

'''

class MyTopo(Topo):
      
    def __init__(self, **opts):
        Topo.__init__(self, **opts)

        switches=[]
        hosts = []

        for i in range(2):
            #switches.append(self.addSwitch('s'+str(i + 1)))
            switches.append(self.addSwitch('s'+str(i + 1), cls = OVSSwitch, protocols='OpenFlow13'))
            
        for i in range(4):
            hosts.append(self.addHost('h'+str(i+1)))     

        self.addLink(switches[0], switches[1],bw=100, max_queue_size=1000) 
        
        self.addLink(switches[0], hosts[0], bw=100, max_queue_size=1000) 
        self.addLink(switches[0], hosts[1], bw=100, max_queue_size=1000) 

        self.addLink(switches[1], hosts[2], bw=100, max_queue_size=1000)
        self.addLink(switches[1], hosts[3], bw=100, max_queue_size=1000) 

        

topos = { 'mytopo': ( lambda: MyTopo() ) }  # this makes it possible to run the mininet with the parameter "--topo mytopo"




class ONOSController (RemoteController):

    def __init__ (self):
        RemoteController.__init__(self,'ONOSController','127.0.0.1',6633)


controllers={'onos': ONOSController}   # this makes it possible to run mininet with the parameter "--controller onos"

def perfTest():
    '''
    This function runs only when executing the script with python: "sudo python topo_one.py"
    If instead the script is executed like "sudo mn --custom topo_one.py ...." then you can only use the options added to "topos" and "controllers" list, by specifying parameters to the "sudo mn" command.
    '''
    topo = MyTopo()
        
    net = Mininet(topo=topo,controller=None,link=TCLink, listenPort=6634)
   
    c0 = ONOSController()
    net.addController(c0)   

   
    hosts = net.hosts
    hosts[0].setMAC("00:00:00:00:00:01", intf='h1-eth0')
    hosts[1].setMAC("00:00:00:00:00:02", intf='h2-eth0')
    hosts[2].setMAC("00:00:00:00:00:03", intf='h3-eth0')
    hosts[3].setMAC("00:00:00:00:00:04", intf='h4-eth0')

    switches = net.switches
    switches[0].setMAC("00:00:00:01:00:01", intf='s1-eth1')
    switches[0].setMAC("00:00:00:01:00:02", intf='s1-eth2')
    switches[0].setMAC("00:00:00:01:00:03", intf='s1-eth3')

    switches[1].setMAC("00:00:00:02:00:01", intf='s2-eth1')
    switches[1].setMAC("00:00:00:02:00:02", intf='s2-eth2')
    switches[1].setMAC("00:00:00:02:00:03", intf='s2-eth3')

    net.start()
    print "Dumping host connections"
    dumpNodeConnections(net.hosts)
    dumpNetConnections(net)
    CLI(net)
    net.stop()

if __name__ == '__main__':
    '''
    Whenever the script is executed with python instead of being a parameter to "sudo mn"
    this part is executed. So the perfTest() function is called. 
    '''
    setLogLevel('info')   
    perfTest()
