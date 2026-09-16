import requests

# The first question in the PDF may give you a hint about -
    # how many connections you should expect.
# Be careful with what you print in line 12. The field names "may differ" - 
    # from what you expect from First and Second question on the PDF.
# There are 5 <to_do> items in the code that you need to change.

response = requests.get("http://localhost:8181/onos/v1/<to_do>",
                        auth=("onos", "rocks"))
data = response.json()
for <to_do> in data[<to_do>]:
    print(<to_do>["src"], <to_do>["dst"])