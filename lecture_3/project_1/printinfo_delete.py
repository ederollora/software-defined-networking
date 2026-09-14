import requests


resp = requests.delete(
    f"{BASE}/flows/application/org.onosproject.rest",
    auth=AUTH
)
print(resp.status_code)   