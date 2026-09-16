from flask import Flask
from datetime import datetime

app = Flask(__name__)

@app.route("/", methods=["GET"]) # http//localhost:8080/
def get_time():
    current_time = datetime.now()

    return f"""
    <html>
        <head>
            <title>Time Application</title>
        </head>
        <body>
            <h1>Current Time</h1>
            <p>{current_time.strftime('%H:%M:%S')}</p>
        </body>
    </html>
    """

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=8011)