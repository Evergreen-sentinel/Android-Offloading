from flask import Flask, request, send_file, jsonify
from PIL import Image
import io
import os

app = Flask(__name__)

@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "ok"})

@app.route('/upload', methods=['POST'])
def upload():
    if 'file' not in request.files:
        return "No file part", 400
    file = request.files['file']
    try:
        img = Image.open(file.stream).convert('L')  # convert to grayscale
    except Exception as e:
        return f"Invalid image: {e}", 400

    buf = io.BytesIO()
    img.save(buf, format='JPEG')
    buf.seek(0)
    return send_file(buf, mimetype='image/jpeg')

if __name__ == '__main__':
    # Use PORT environment variable for production (Render) or default to 5000 for local development
    port = int(os.environ.get('PORT', 5000))
    # For debug/prototype only - use Gunicorn for production
    app.run(host='0.0.0.0', port=port)
