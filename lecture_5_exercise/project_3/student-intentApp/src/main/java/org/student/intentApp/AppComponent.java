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
package org.student.intentApp;

import org.apache.felix.scr.annotations.*;
import org.onlab.packet.*;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.*;
import org.onosproject.net.flow.*;
import org.onosproject.net.host.HostService;
import org.onosproject.net.intent.HostToHostIntent;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.Key;
import org.onosproject.net.packet.*;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;


@Component(immediate = true)
public class AppComponent {

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentService intentService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;
    private ReactivePacketProcessor processor = new ReactivePacketProcessor();

    private ApplicationId appId;
    @Activate
    protected void activate() {
        appId = coreService.registerApplication("org.student.intentApp");
        packetService.addProcessor(processor, PacketProcessor.director(2));
        log.info("Started", appId.id());
    }
    @Deactivate
    protected void deactivate() {
        flowRuleService.removeFlowRulesById(appId);
        packetService.removeProcessor(processor);
        processor = null;
        for (Intent intent:intentService.getIntents()) {
            intentService.withdraw(intent);
        }
        log.info("Stopped");
    }

    private class ReactivePacketProcessor implements PacketProcessor {

        @Override
        public void process(PacketContext context) {
            InboundPacket pkt = context.inPacket();
            Ethernet ethPkt = pkt.parsed();
            //Discard if  packet is null.
            if (ethPkt == null) {
                return;
            }

            HostId host1 = HostId.hostId(ethPkt.getSourceMAC());
            HostId host2 = HostId.hostId(ethPkt.getDestinationMAC());
            if (hostService.getHost(host1)==null) return;
            if (hostService.getHost(host2)==null) return;
            TrafficSelector selector = DefaultTrafficSelector.emptySelector();
            TrafficTreatment treatment = DefaultTrafficTreatment.emptyTreatment();
            Key key;
            if (host1.toString().compareTo(host2.toString()) < 0) {
                key = Key.of(host1.toString() + host2.toString(), appId);
            } else {
                key = Key.of(host2.toString() + host1.toString(), appId);
            }
            HostToHostIntent hostIntent = HostToHostIntent.builder()
                    .appId(appId)
                    .key(key)
                    .one(host1)
                    .two(host2)
                    .selector(selector)
                    .treatment(treatment)
                    .build();
            intentService.submit(hostIntent);

        }
    }
}
