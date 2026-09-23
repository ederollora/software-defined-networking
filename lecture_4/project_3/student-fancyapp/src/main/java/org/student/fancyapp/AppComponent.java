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
package org.student.fancyapp;

import org.apache.felix.scr.annotations.*;
import org.onlab.packet.*;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Host;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.*;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.packet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class AppComponent {

    private ApplicationId appId;
    private static String H1_MAC = "00:00:00:00:00:01";
    private static String H2_MAC = "00:00:00:00:00:02";
    //private static String H3_MAC = "00:00:00:00:00:03";
    private static String S1_ID = "of:0000000000000001";
    private static long H1_S1_PORT = 1;
    private static long H2_S1_PORT = 2;
    //private static long H3_S1_PORT = 3;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    FlowObjectiveService flowObjectiveService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    private ReactivePacketProcessor processor = new ReactivePacketProcessor();

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Activate
    protected void activate() {
        log.info("Started");
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
        selector.matchEthType(Ethernet.TYPE_IPV4);
        appId = coreService.registerApplication("org.student.fancyapp");
        packetService.requestPackets(selector.build(), PacketPriority.REACTIVE, appId);
        packetService.addProcessor(processor, PacketProcessor.director(2));
    }

    @Deactivate
    protected void deactivate() {
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
        selector.matchEthType(Ethernet.TYPE_IPV4);
        packetService.cancelPackets(selector.build(), PacketPriority.REACTIVE, appId);
        flowRuleService.removeFlowRulesById(appId);
        packetService.removeProcessor(processor);
        processor = null;
        log.info("Stopped");
    }

    private class ReactivePacketProcessor implements PacketProcessor {
        @Override
        public void process(PacketContext context) {
            InboundPacket pkt = context.inPacket();
            Ethernet ethPkt = pkt.parsed();
            //Discard if packet is null.
            if (ethPkt == null) return;
            // We care only for IPV4 packets, discard the rest. ARP is handled by the proxyARP app of ONOS.
            if (ethPkt.getEtherType() == Ethernet.TYPE_IPV4){
                TrafficTreatment.Builder treatmentBuilder = DefaultTrafficTreatment.builder();
                String dstMac=null;
                PortNumber outPort = null;
                // Set the dstMAC and outPort
                if (ethPkt.getDestinationMAC().toString().equals(H2_MAC)) {
                    log.info("Forwarding packet to H2");
                    dstMac = H2_MAC;
                    outPort = PortNumber.portNumber(H2_S1_PORT);
                    treatmentBuilder.setOutput(outPort).build(); //this is defining the action that we wwant in the entry in the flow table of the switch
                } else if (ethPkt.getDestinationMAC().toString().equals(H1_MAC)) {
                    log.info("Forwarding packet to H1");
                    dstMac = H1_MAC;
                    outPort = PortNumber.portNumber(H1_S1_PORT);
                    treatmentBuilder.setOutput(outPort).build(); //this is defining the action that we wwant in the entry in the flow table of the switch
                }
                /*else if (ethPkt.getDestinationMAC().toString().equals(H3_MAC)){
                   log.info("Forwarding packet to H3");
                   dstMac = H3_MAC;
                   outPort = PortNumber.portNumber(H3_S1_PORT);
                   treatmentBuilder.setOutput(outPort).build();
                }*/
                else{
                    log.info("Unknown destination host, ignoring");
                    return;
                }
                //Then define WHICH traffic we want to process - this will be the matching criteria in the flow table entry!
                TrafficSelector.Builder trafficSelectorBuilder = DefaultTrafficSelector.builder();
                trafficSelectorBuilder.matchEthDst(ethPkt.getDestinationMAC());
                //Finally generate the flow objective and push it to the switch -- this will define the flow entry in the switch with all the previous components
                ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
                        .withSelector(trafficSelectorBuilder.build())
                        .withTreatment(treatmentBuilder.build())
                        .withPriority(100)
                        .withFlag(ForwardingObjective.Flag.VERSATILE)
                        .fromApp(appId)
                        .makeTemporary(5)
                        .add();
                flowObjectiveService.forward(DeviceId.deviceId(S1_ID),forwardingObjective); //this creates the flow entry in the switch
                context.treatmentBuilder().addTreatment(treatmentBuilder.build()); //this deals with the initial packet
                context.send();
            }
            else return;
        }
    }
}
