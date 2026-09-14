import requests
import json


BASE = "http://localhost:8181/onos/v1"
AUTH = ("onos", "rocks")

# GET — fetch all devices
response = requests.get(
    f"{BASE}/devices",
    auth=AUTH
)

print(f"Response code: {response.status_code}")   # 200

input("Press Enter to continue to next command...")

dict_data = response.json()        # Python JSON (text) to  dict
print(dict_data)
print("\n" * 3)
print(dict_data["devices"])         # list of devices
print("\n" * 3)
print(dict_data["devices"][0])
print("\n" * 3)
print(dict_data["devices"][0]["id"]) # "of:0000000000000001