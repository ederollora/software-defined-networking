from flask import Flask, request, render_template
from onos.flows import install_firewall_rule

app = Flask(__name__)
firewall_rules: list[dict] = []


@app.get("/")
def index():
    return render_template("index.html", rules=firewall_rules, message=None)


@app.post("/add_rule")
def add_rule():
    ip1 = request.form.get("ip1", "").strip()
    ip2 = request.form.get("ip2", "").strip()

    if not ip1 or not ip2:
        return render_template("index.html", rules=firewall_rules,
                               message="⚠ Both IP addresses are required."), 400

    ok, status = install_firewall_rule(ip1, ip2)
    firewall_rules.append({"ip1": ip1, "ip2": ip2, "ok": ok, "status": status})

    return render_template("index.html", rules=firewall_rules,
                           message=f"Rule {ip1} ↔ {ip2}: {status}")


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=8090, debug=True)