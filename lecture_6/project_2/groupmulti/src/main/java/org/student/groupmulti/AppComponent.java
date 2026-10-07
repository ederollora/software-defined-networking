package org.student.groupmulti;

import org.onlab.packet.Ethernet;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.group.DefaultGroupBucket;
import org.onosproject.net.group.DefaultGroupDescription;
import org.onosproject.net.group.DefaultGroupKey;
import org.onosproject.net.group.Group;
import org.onosproject.net.group.GroupBucket;
import org.onosproject.net.group.GroupBuckets;
import org.onosproject.net.group.GroupDescription;
import org.onosproject.net.group.GroupEvent;
import org.onosproject.net.group.GroupKey;
import org.onosproject.net.group.GroupListener;
import org.onosproject.net.group.GroupService;
import org.onosproject.net.packet.PacketPriority;
import org.onosproject.net.packet.PacketService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component(immediate = true)
public class AppComponent {

    private static final long[] GROUP_1 = {2, 3};
    private static final long[] GROUP_2 = {1, 4};
    private static final long[] GROUP_3 = {1};
    private static final long[] GROUP_4 = {2};

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String APP_NAME = "org.student.groupmulti";
    private static final DeviceId DEVICE_ID =
            DeviceId.deviceId("of:0000000000000001");

    @Reference
    protected CoreService coreService;

    @Reference
    protected FlowRuleService flowRuleService;

    @Reference
    protected GroupService groupService;

    @Reference
    protected PacketService packetService;

    private ApplicationId appId;

    private final GroupListener groupListener = event -> {
        if (event.type() != GroupEvent.Type.GROUP_ADDED) {
            return;
        }

        Group group = event.subject();

        if (!group.deviceId().equals(DEVICE_ID)) {
            return;
        }

        String key = new String(
                group.appCookie().key(),
                StandardCharsets.UTF_8
        );

        if (key.equals("arp-10.0.0.6")) {
            installArpFlow(1, group);
        } else if (key.equals("arp-10.0.0.2")) {
            installArpFlow(2, group);
        } else if (key.equals("arp-10.0.0.3")) {
            installArpFlow(3, group);
        } else if (key.equals("arp-10.0.0.10")) {
            installArpFlow(4, group);
        }
    };

    @Activate
    protected void activate() {
        appId = coreService.registerApplication(APP_NAME);
        groupService.addListener(groupListener);

        /*
         * Try to cancel the ARP -> CONTROLLER packet request
         * belonging to hostprovider.
         */
        ApplicationId arpOwner =
                coreService.getAppId("org.onosproject.hostprovider");

        if (arpOwner != null) {
            TrafficSelector arpSelector =
                    DefaultTrafficSelector.builder()
                            .matchEthType(Ethernet.TYPE_ARP)
                            .build();

            packetService.cancelPackets(
                    arpSelector,
                    PacketPriority.CONTROL,
                    arpOwner
            );

            dropOtherArp();

            log.info("Started {}", APP_NAME);
        }

        /*
         * h1 -> h2, h3, h4
         * h2 -> h1, h3, h4
         * h3 -> h1, h2, h4
         * h4 -> h1, h2, h3
         */
        createArp("10.0.0.1", GROUP_1);
        createArp("10.0.0.2", GROUP_2);
        createArp("10.0.0.3", GROUP_3);
        createArp("10.0.0.0", GROUP_4);

        log.info("Started {}", APP_NAME);
    }

    private void createArp(String ipAddress, long... outputPorts) {
        List<GroupBucket> buckets = new ArrayList<>();

        for (long port : outputPorts) {
            TrafficTreatment treatment =
                    DefaultTrafficTreatment.builder()
                            .setOutput(PortNumber.portNumber(port))
                            .build();

            buckets.add(
                    DefaultGroupBucket.createAllGroupBucket(treatment)
            );
        }

        GroupKey groupKey =
                new DefaultGroupKey(
                        ("arp-" + ipAddress)
                                .getBytes(StandardCharsets.UTF_8)
                );

        GroupDescription groupDescription =
                new DefaultGroupDescription(
                        DEVICE_ID,
                        Group.Type.ALL,
                        new GroupBuckets(buckets),
                        groupKey,
                        null,
                        appId
                );

        groupService.addGroup(groupDescription);
    }

    private void installArpFlow(long inputPort, Group group) {
        TrafficSelector selector =
                DefaultTrafficSelector.builder()
                        .matchInPort(PortNumber.portNumber(inputPort))
                        .matchEthType(Ethernet.TYPE_ARP)
                        .build();

        TrafficTreatment treatment =
                DefaultTrafficTreatment.builder()
                        .group(group.id())
                        .build();

        FlowRule flowRule =
                DefaultFlowRule.builder()
                        .forDevice(DEVICE_ID)
                        .fromApp(appId)
                        .withSelector(selector)
                        .withTreatment(treatment)
                        .withPriority(50000)
                        .makePermanent()
                        .build();

        flowRuleService.applyFlowRules(flowRule);

        log.info(
                "ARP flow installed: port {} -> group {}",
                inputPort,
                group.id()
        );
    }

    private void dropOtherArp() {

        TrafficSelector selector =
                DefaultTrafficSelector.builder()
                        .matchEthType(Ethernet.TYPE_ARP)
                        .build();

        TrafficTreatment treatment =
                DefaultTrafficTreatment.builder()
                        .drop()
                        .build();

        FlowRule flowRule =
                DefaultFlowRule.builder()
                        .forDevice(DEVICE_ID)
                        .fromApp(appId)
                        .withSelector(selector)
                        .withTreatment(treatment)
                        .withPriority(50000)
                        .makePermanent()
                        .build();

        flowRuleService.applyFlowRules(flowRule);

        log.info("Installed");
    }

    private void removeArp(String ipAddress) {
        GroupKey groupKey =
                new DefaultGroupKey(
                        ("arp-" + ipAddress)
                                .getBytes(StandardCharsets.UTF_8)
                );

        groupService.removeGroup(
                DEVICE_ID,
                groupKey,
                appId
        );
    }

    @Deactivate
    protected void deactivate() {
        flowRuleService.removeFlowRulesById(appId);

        removeArp("10.0.0.1");
        removeArp("10.0.0.2");
        removeArp("10.0.0.3");
        removeArp("10.0.0.4");

        groupService.removeListener(groupListener);

        log.info("Stopped {}", APP_NAME);
    }
}