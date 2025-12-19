# app.py
from dotenv import load_dotenv
load_dotenv()

import traceback
from flask import Flask, request, jsonify
from services.crawler_service import CrawlerService
from services.s3_service import generate_presigned_url


app = Flask(__name__)

@app.route("/api/crawl", methods=["POST"])
def crawl():
    data = request.json
    url = data.get("url")

    if not url:
        return jsonify({"error": "url is required"}), 400

    try:
        result = CrawlerService.crawl_and_upload(url)
        return jsonify(result)
    except Exception as e:
        print("[ERROR] /api/crawl failed")
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500
    
@app.route("/api/presigned", methods=["POST"])
def presigned():
    data = request.json
    keys = data.get("keys")

    if not keys or not isinstance(keys, list):
        return jsonify({"error": "keys must be a list"}), 400

    try:
        result = {}
        for key in keys:
            url = generate_presigned_url(key)
            result[key] = url

        return jsonify({"urls": result})

    except Exception as e:
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=9000)
