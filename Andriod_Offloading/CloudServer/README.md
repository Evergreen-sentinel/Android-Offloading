# CloudServer (Flask grayscale image)

## Build & run locally (quick test)
1. Create venv:
   python3 -m venv venv
   source venv/bin/activate

2. Install:
   pip install -r requirements.txt

3. Run:
   python app.py

4. Test:
   curl -X POST -F "file=@sample.jpg" http://127.0.0.1:5000/upload --output output.jpg

## Production Deployment

### Deploy to Render
The app is now configured for production deployment on Render:

1. **Automatic Port Binding**: The app uses `os.environ.get('PORT', 5000)` to automatically use Render's assigned port
2. **Production WSGI Server**: Gunicorn is included in requirements.txt for production deployment
3. **Procfile**: A Procfile is provided for easy Render deployment

To deploy on Render:
1. Connect your repository to Render
2. Use the following build command: `pip install -r requirements.txt`
3. Use the start command: `gunicorn --bind 0.0.0.0:$PORT app:app`

### Local Development with Gunicorn
To test with Gunicorn locally:
```bash
pip install -r requirements.txt
gunicorn --bind 0.0.0.0:5000 app:app
```

## Docker: build and run
docker build -t gray-offloader .
docker run -d -p 5000:5000 --name gray-offloader gray-offloader

Visit: http://<HOST_IP>:5000/health

## API Endpoints

- `GET /health` - Health check endpoint
- `POST /upload` - Upload image file, returns grayscale version

## Legacy Deployment Options

### Deploy to Google Cloud VM (summary)
1. Create a VM (Ubuntu) and enable firewall rule to allow TCP 5000.
2. SSH into VM.
3. Install Docker:
   sudo apt update
   sudo apt install -y docker.io
   sudo usermod -aG docker $USER   # then re-login or use sudo docker

4. Copy project to VM:
   git clone <your-repo> or scp files

5. Build and run container:
   docker build -t gray-offloader .
   docker run -d -p 5000:5000 gray-offloader

6. Test from your local machine:
   curl -X POST -F "file=@sample.jpg" http://<VM_EXTERNAL_IP>:5000/upload --output output.jpg
