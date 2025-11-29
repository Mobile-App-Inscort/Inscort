# app.py
from flask import Flask, request, jsonify
from services.crawler_service import CrawlerService
from dotenv import load_dotenv

load_dotenv()

app = Flask(__name__)

@app.route("/crawl", methods=["POST"])
def crawl():
    data = request.json
    url = data.get("url")

    if not url:
        return jsonify({"error": "url is required"}), 400

    try:
        result = CrawlerService.crawl_and_upload(url)
        return jsonify(result)
    except Exception as e:
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=9000, debug=True)
