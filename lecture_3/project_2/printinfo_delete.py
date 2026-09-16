import requests


BASE = "http://localhost:8181/onos/v1"
AUTH = ("onos", "rocks")

resp = requests.delete(
    f"{BASE}/flows/application/org.onosproject.rest",
    auth=AUTH
)
print(resp.status_code)   