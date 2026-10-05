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

import org.onlab.packet.Ethernet;
import org.onlab.packet.EthType;
import org.onlab.packet.IPv4;
import org.onlab.packet.IpAddress;
import org.onlab.packet.IpPrefix;
import org.onlab.packet.TCP;
import org.onlab.packet.TpPort;
import org.onlab.packet.UDP;

import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;

import org.onosproject.net.Host;
import org.onosproject.net.HostId;

import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;

import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;

import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;

import org.onosproject.net.packet.*;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Set;

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

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    private ReactivePacketProcessor processor =
            new ReactivePacketProcessor();

    private final InternalHostListener hostListener =
            new InternalHostListener();

    private ApplicationId appId;

    private final ArrayList<TrafficSelector> aclRules =
            new ArrayList<>();


    @Activate
    protected void activate() {

        appId = coreService.registerApplication("org.student.acl");

        // Register the packet processor for the reactive ACL
        packetService.addProcessor(
                processor,
                PacketProcessor.director(1)
        );

        // Register the host listener so newly discovered hosts are detected
        hostService.addListener(hostListener);

        // Define/request the reactive ACL rules
        defineAclRules();

        log.info("===== ACL APPLICATION STARTED =====");
        log.info("Application ID: {}", appId.id());
    }


    @Deactivate
    protected void deactivate() {

        // Stop receiving packets requested for the reactive ACL
        for (TrafficSelector selector : aclRules) {
            packetService.cancelPackets(
                    selector,
                    PacketPriority.CONTROL,
                    appId
            );
        }

        // Remove all flow rules installed by this application
        flowRuleService.removeFlowRulesById(appId);

        // Unregister the packet processor
        packetService.removeProcessor(processor);

        // Unregister the host listener
        hostService.removeListener(hostListener);

        log.info("===== ACL APPLICATION STOPPED =====");
        }


    /**
     * Defines the reactive ACL rules.
     *
     * This does NOT directly install a flow rule.
     *
     * It creates a selector which will later be compared
     * against selectors generated from incoming packets.
     */
    public void defineAclRules() {

        TrafficSelector.Builder selectorBuilder =
                DefaultTrafficSelector.builder();

        selectorBuilder.matchEthType(Ethernet.TYPE_IPV4);

        selectorBuilder.matchIPDst(
                IpPrefix.valueOf(
                        IpAddress.valueOf("10.0.0.2"),
                        IpPrefix.MAX_INET_MASK_LENGTH
                )
        );

        selectorBuilder.matchIPProtocol(
                IPv4.PROTOCOL_TCP
        );

        selectorBuilder.matchTcpDst(
                TpPort.tpPort(8882)
        );

        TrafficSelector aclSelector =
                selectorBuilder.build();

        aclRules.add(aclSelector);

        // IMPORTANT:
        // Ask ONOS to send packets matching this selector
        // to the controller.
        packetService.requestPackets(
                aclSelector,
                PacketPriority.CONTROL,
                appId
        );

        log.info("===== ACL RULE ADDED =====");
        log.info("ACL selector: {}", aclSelector);
        log.info("Requested matching packets from switches");
        log.info("==========================");
    }


    /**
     * Receives ONOS host-discovery events.
     */
    private class InternalHostListener
            implements HostListener {

        @Override
        public void event(HostEvent event) {

            Host host = event.subject();

            log.info("===== HOST EVENT =====");
            log.info("Event type: {}", event.type());
            log.info("Host: {}", host);

            /*
             * A host may temporarily exist without any known IP.
             */
            if (host.ipAddresses() == null ||
                    host.ipAddresses().isEmpty()) {

                log.info(
                        "Host has no known IP address yet."
                );

                return;
            }

            for (IpAddress ip : host.ipAddresses()) {

                log.info("Host IP: {}", ip);

                if (ip.equals(IpAddress.valueOf("10.0.0.4"))) {

                    log.info("New host 10.0.0.4 detected!");

                    addProactiveAclRule(ip);
                }
            }
        }
    }


    /**
     * Adds a proactive ACL rule.
     *
     * Current proactive ACL:
     *
     * IPv4
     * destination = discovered host
     * protocol = TCP
     * destination port = 8883
     * action = DROP
     */
    private void addProactiveAclRule(
            IpAddress ipAddress) {

        Set<Host> hosts =
                hostService.getHostsByIp(ipAddress);

        if (hosts == null || hosts.isEmpty()) {

            log.warn(
                    "No host found for IP {}",
                    ipAddress
            );

            return;
        }


        for (Host host : hosts) {
            HostId hostId = host.id();
            log.info(
                    "===== PROACTIVE ACL RULE ====="
            );

            log.info("Host: {}", host);
            log.info("HostId: {}", hostId);
            log.info("IP: {}", ipAddress);
            log.info(
                    "Device: {}",
                    host.location().deviceId()
            );
            TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
            selectorBuilder.matchEthType(Ethernet.TYPE_IPV4);
            selectorBuilder.matchIPDst(
                    IpPrefix.valueOf(
                            ipAddress,
                            IpPrefix.MAX_INET_MASK_LENGTH
                    )
            );
            selectorBuilder.matchIPProtocol(
                    IPv4.PROTOCOL_TCP
            );

            /*
             * PROACTIVE RULE:
             * Block SSH / TCP destination port 22.
             */
            selectorBuilder.matchTcpDst(
                    TpPort.tpPort(8883)
            );
            TrafficSelector selector =
                    selectorBuilder.build();
            TrafficTreatment treatment =
                    DefaultTrafficTreatment
                            .builder()
                            .drop()
                            .build();
            log.info(
                    "Proactive selector: {}",
                    selector
            );

            log.info(
                    "Treatment: DROP"
            );


            ForwardingObjective forwardingObjective =
                    DefaultForwardingObjective
                            .builder()
                            .withSelector(selector)
                            .withTreatment(treatment)
                            .withPriority(1000)
                            .withFlag(
                                    ForwardingObjective.Flag.VERSATILE
                            )
                            .fromApp(appId)
                            .makeTemporary(10000)
                            .add();


            flowObjectiveService.forward(
                    host.location().deviceId(),
                    forwardingObjective
            );


            log.info(
                    "Proactive DROP rule sent to {}",
                    host.location().deviceId()
            );

            log.info(
                    "Lifetime: Permanent"
            );

            log.info(
                    "=============================="
            );
        }
    }


    /**
     * Packet processor used for the reactive ACL.
     */
    private class ReactivePacketProcessor
            implements PacketProcessor {

        @Override
        public void process(PacketContext context) {

            /*
             * Another application may already have handled
             * this packet.
             */
            if (context.isHandled()) {
                return;
            }
            InboundPacket pkt = context.inPacket();
            Ethernet ethPkt = pkt.parsed();


            if (ethPkt == null) {
                return;
            }

            /*
             * Only process IPv4.
             */
            if (EthType.EtherType.lookup(ethPkt.getEtherType()) != EthType.EtherType.IPV4) {
                return;
            }
            IPv4 ipv4Packet = (IPv4) ethPkt.getPayload();

            /*
             * Create a selector using the fields from
             * the actual received packet.
             */
            TrafficSelector.Builder packetSelector =  DefaultTrafficSelector.builder();
            packetSelector.matchEthType(
                    Ethernet.TYPE_IPV4
            );
            packetSelector.matchIPProtocol(
                    ipv4Packet.getProtocol()
            );
            packetSelector.matchIPDst(
                    IpPrefix.valueOf(
                            IpAddress.valueOf(ipv4Packet.getDestinationAddress()),
                            IpPrefix.MAX_INET_MASK_LENGTH
                    )
            );


            /*
             * TCP packet
             */
            if (ipv4Packet.getProtocol() == IPv4.PROTOCOL_TCP) {
                TCP tcpPkt = (TCP) ipv4Packet.getPayload();
                packetSelector.matchTcpDst(
                        TpPort.tpPort(
                                tcpPkt.getDestinationPort()
                        )
                );
            }


            /*
             * UDP packet
             */
            else if (ipv4Packet.getProtocol()
                    == IPv4.PROTOCOL_UDP) {

                UDP udpPkt = (UDP) ipv4Packet.getPayload();

                packetSelector.matchUdpDst(
                        TpPort.tpPort(
                                udpPkt.getDestinationPort()
                        )
                );
            }


            /*
             * BUILD THE SELECTOR ONCE.
             *
             * This is important.
             * We compare TrafficSelector against TrafficSelector,
             * NOT Builder against TrafficSelector.
             */
            TrafficSelector currentPacketSelector = packetSelector.build();

            log.info("===== PACKET SELECTOR =====");
            log.info("Packet selector: {}", currentPacketSelector);
            log.info("===========================");

            for (TrafficSelector selector : aclRules) {

                log.info("===== ACL COMPARISON =====");
                log.info("ACL rule:        {}", selector);
                log.info("Packet selector: {}", currentPacketSelector);
                log.info("Equal?           {}", selector.equals(currentPacketSelector));
                log.info("==========================");

                if (selector.equals(currentPacketSelector)) {

                    log.info("MATCH FOUND - Flow should be dropped");

                    dropFlow(currentPacketSelector, context);

                    context.block();

                    return;
                }

            }
        }


        /**
         * Install reactive DROP rule.
         */
        public void dropFlow(
                TrafficSelector selector,
                PacketContext context) {

            TrafficTreatment treatment =
                    DefaultTrafficTreatment
                            .builder()
                            .drop()
                            .build();


            ForwardingObjective forwardingObjective =
                    DefaultForwardingObjective
                            .builder()
                            .withSelector(selector)
                            .withTreatment(treatment)
                            .withPriority(1000)
                            .withFlag(
                                    ForwardingObjective.Flag.VERSATILE
                            )
                            .fromApp(appId)
                            .makeTemporary(10000)
                           //.makePermanent()
                            .add();


            log.info(
                    "===== INSTALLING REACTIVE DROP ====="
            );

            log.info(
                    "Selector: {}",
                    selector
            );

            log.info(
                    "Device: {}",
                    context.inPacket()
                            .receivedFrom()
                            .deviceId()
            );

            log.info(
                    "Priority: 10000"
            );

            log.info(
                    "Lifetime: 50 seconds"
            );


            flowObjectiveService.forward(
                    context.inPacket()
                            .receivedFrom()
                            .deviceId(),
                    forwardingObjective
            );


            log.info(
                    "Reactive DROP flow sent to switch"
            );

            log.info(
                    "===================================="
            );
        }
    }
}