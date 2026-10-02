/*
 * Copyright 2016 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.student.acl;

import org.apache.felix.scr.annotations.*;
import org.onlab.packet.*;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.flow.*;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.packet.*;
import org.slf4j.Logger;
import java.util.ArrayList;

import static org.slf4j.LoggerFactory.getLogger;

@Component(immediate = true)
public class AppComponent {

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowObjectiveService flowObjectiveService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    private ReactivePacketProcessor processor = new ReactivePacketProcessor();

    private ApplicationId appId;

    ArrayList<TrafficSelector> aclRules  =  new ArrayList<>();

    @Activate
    protected void activate() {
        appId = coreService.registerApplication("org.student.acl");
        packetService.addProcessor(processor, PacketProcessor.director(1));
        //Specify a priority of 1 in our packet processor, so we handle the packets before the Forwarding app.
        defineAclRules();
        log.info("Started", appId.id());
    }

    @Deactivate
    protected void deactivate() {
        flowRuleService.removeFlowRulesById(appId);
        packetService.removeProcessor(processor);
        processor = null;
        log.info("Stopped");
    }

    /**
     * Use this function to define custom ACL rules and push them in the aclRules ArrayList.
     * This function is called on activate().
     */
    public void defineAclRules(){
        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
        selectorBuilder.matchEthType(Ethernet.TYPE_IPV4);
        selectorBuilder.matchIPDst(IpPrefix.valueOf(IpAddress.valueOf("10.0.0.2"),IpPrefix.MAX_INET_MASK_LENGTH));
        selectorBuilder.matchIPProtocol(IPv4.PROTOCOL_TCP);
        selectorBuilder.matchTcpDst(TpPort.tpPort(23));
        aclRules.add(selectorBuilder.build());

        //Put here the ICMP traffic new rule to block ICMP traffic
    }

    /**
     * Our custom Packet Processor, which overrides the default  process() function.
     */
    private class ReactivePacketProcessor implements PacketProcessor {

        @Override
        public void process(PacketContext context) {
            InboundPacket pkt = context.inPacket();
            Ethernet ethPkt = pkt.parsed();

            //Discard if  packet is null.
            if (ethPkt == null) {
                return;
            }

            IPv4 ipv4Packet;
            //We only care for IPv4 packets, discard the rest.
            switch (EthType.EtherType.lookup(ethPkt.getEtherType())) {
                case IPV4:
                    ipv4Packet = (IPv4) ethPkt.getPayload();
                    break;
                default:
                    return;
            }
            //Generate the traffic selector based on the packet that arrived. We will use it to compare with the one in the ACL
            TrafficSelector.Builder packetSelector = DefaultTrafficSelector.builder();
            packetSelector.matchEthType(Ethernet.TYPE_IPV4);
            packetSelector.matchIPProtocol(ipv4Packet.getProtocol());
            packetSelector.matchIPDst(IpPrefix.valueOf(IpAddress.valueOf(ipv4Packet.getDestinationAddress()),IpPrefix.MAX_INET_MASK_LENGTH));

            //Handle TCP packets here.
            if (ipv4Packet.getProtocol() == IPv4.PROTOCOL_TCP) {
                TCP tcpPkt = (TCP) ipv4Packet.getPayload();
                packetSelector.matchTcpDst(TpPort.tpPort(tcpPkt.getDestinationPort()));
            }
            //Handle UPD packets here.
            else if (ipv4Packet.getProtocol() == IPv4.PROTOCOL_UDP) {
                UDP udpPkt = (UDP) ipv4Packet.getPayload();
                packetSelector.matchUdpDst(TpPort.tpPort(udpPkt.getDestinationPort()));
            }// Handle ICMP packets here.
            else if (ipv4Packet.getProtocol() == IPv4.PROTOCOL_ICMP) {
                // ICMP packets don't have TCP/UDP ports, so no need to match ports.
                log.info("ICMP packet detected. Checking ACL rules.");
            }

            // If the current packet's selector matches any of the ACL rules, DROP the packet and its flow.
            for (TrafficSelector selector:aclRules){
                if (selector.equals(packetSelector.build())){
                    log.info("Flow should be dropped"); //since it matches the ACL
                    dropFlow(packetSelector.build(),context);
                    context.block();    //Since we already handled the packet, BLOCK any access to it by other ONOS apps (e.g. the Forwarding app)
                    return;
                }
            }
        }
        /**
         * This function creates and install the DROP rule for the specified flow
         * @param selector
         * @param context
         */
        public void dropFlow(TrafficSelector selector,PacketContext context){
            TrafficTreatment treatment = DefaultTrafficTreatment.builder().drop().build();
            ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
                    .withSelector(selector)
                    .withTreatment(treatment)
                    .withPriority(1000)
                    .withFlag(ForwardingObjective.Flag.VERSATILE)
                    .fromApp(appId)
                    .makeTemporary(5)
                    .add();
            flowObjectiveService.forward(context.inPacket().receivedFrom().deviceId(), forwardingObjective);
            return;
        }
    }
}
