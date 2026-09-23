/*
 * Copyright 2017 Open Networking Laboratory
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
package org.student.fancyAppv0;

import org.apache.felix.scr.annotations.*;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IPacket;
import org.onlab.packet.IPv4;
import org.onosproject.net.packet.InboundPacket;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Skeletal ONOS application component.
 */
@Component(immediate = true)
public class AppComponent {
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private ReactivePacketProcessor processor = new ReactivePacketProcessor();
    @Activate
    protected void activate() {
        log.info("FancyApp_v0 Started");
        packetService.addProcessor(processor,PacketProcessor.director(2));
    }

    @Deactivate
    protected void deactivate() {
        packetService.removeProcessor(processor);
        processor = null;
        log.info("FancyApp_v0 Stopped");
    }

    private class ReactivePacketProcessor implements PacketProcessor{

        @Override
        public void process(PacketContext packetContext) {
            InboundPacket packet = packetContext.inPacket();
            Ethernet ethPkt = packet.parsed();
            if (ethPkt == null) return;


            log.info("Packet received");

            short et = ethPkt.getEtherType();
            switch (Short.toUnsignedInt(et)) {
                case 0x0800: log.info("IPv4 (0x0800)"); break;
                case 0x0806: log.info("ARP  (0x0806)"); break;
            }

            //log.info("Type: "+ethPkt.getEtherType());
            log.info("From :"+ethPkt.getSourceMAC().toString());
            log.info("To: "+ethPkt.getDestinationMAC().toString());
        }
    }

}
