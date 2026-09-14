
## TODO make the other way around
## please put, if you have not done that yet, h1 ping h2 (should not work)
## Change IPV4_SRC, IPV4_DST and OUTPUT

# you have to change from:
# from this
# flow_dict = {
#    "priority": 55555,
#    "(...)

#to this
#print(f"Response code: {resp.status_code}")   # 201 Created

import requests
import json

BASE = "http://localhost:8181/onos/v1"
AUTH = ("onos", "rocks")


# POST — install a flow rule
flow_dict = {
    "priority": 55555,
    "isPermanent": True,
    "deviceId": "of:0000000000000001",
    "treatment": {"instructions": [{"type": "OUTPUT", "port": ""}]},
    "selector": {"criteria": [
        {"type": "ETH_TYPE", "ethType": "0x0800"}, # this is the ethertype IP, it has to always be related to the IP part. If you don't match those 2 ethType and ip, you cannot program it
        {"type": "IPV4_SRC", "ip": ""}, 
        {"type": "IPV4_DST", "ip": ""}
    ]}
}

text_json = json.dumps(flow_dict)

print(json.dumps(text_json, indent=4)) # this is print on a 

resp = requests.post(
    f"{BASE}/flows/of:0000000000000001",
    json=flow_dict,   # auto-serializes to JSON
    auth=AUTH
)
print(f"Response code: {resp.status_code}")   # 201 Created

## TODO make the other way around
## IPV4_SRC and IPV4_DST you have to change and input OUTPUT 1

# from this
# flow_dict = {
#    "priority": 55555,
#    "(...)

#to this
#print(f"Response code: {resp.status_code}")   # 201 Created