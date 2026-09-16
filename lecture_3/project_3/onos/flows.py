import json
import requests

ONOS_HOST  = "localhost"
ONOS_PORT  = 8181
ONOS_USER  = "onos"
ONOS_PASS  = "rocks"
ONOS_BASE  = f"http://{ONOS_HOST}:{ONOS_PORT}/onos/v1"

SWITCHES = [
    "of:0000000000000001",   # S1
    "of:0000000000000002",   # S2
]

FW_PRIORITY = 40_000 # This has higher priority and fwd flow rules


def _drop_flow(device_id: str, src_ip: str, dst_ip: str) -> dict:
    return {
        "priority":    FW_PRIORITY,
        "timeout":     0,
        "isPermanent": True,
        "deviceId":    device_id,
        "treatment": {"instructions": []},   # DROP
        "selector": {
            "criteria": [
                {"type": "ETH_TYPE", "ethType": "0x0800"},
                {"type": "IPV4_SRC", "ip": f"{src_ip}/32"},
                {"type": "IPV4_DST", "ip": f"{dst_ip}/32"},
            ]
        }
    }


def install_firewall_rule(ip1: str, ip2: str) -> tuple[bool, str]:
    headers = {"Content-Type": "application/json", "Accept": "application/json"}
    auth    = (ONOS_USER, ONOS_PASS)
    errors  = []

    for device_id in SWITCHES:
        for src, dst in [(ip1, ip2), (ip2, ip1)]:
            flow    = _drop_flow(device_id, src, dst)
            url     = f"{ONOS_BASE}/flows/{device_id}"
            payload = json.dumps(flow)

            try:
                resp = requests.post(url, data=payload, headers=headers,
                                     auth=auth, timeout=5)
                if resp.status_code not in (200, 201):
                    errors.append(
                        f"{device_id} [{src}→{dst}]: HTTP {resp.status_code}"
                    )
            except requests.RequestException as exc:
                errors.append(f"{device_id} [{src}→{dst}]: {exc}")

    if errors:
        return False, "ERRORS: " + " | ".join(errors)
    return True, "OK - DROP rules installed on all switches"