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

## Docker: build and run
docker build -t gray-offloader .
docker run -d -p 5000:5000 --name gray-offloader gray-offloader

Visit: http://<HOST_IP>:5000/health

## Deploy to Google Cloud VM (summary)
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




### Personal Notes:
1. export PYTHONPATH="/home/kid_6/Desktop/Andriod_Offloading/CloudServer/venv/lib/python3.13/site-packages:$PYTHONPATH" && cd /home/kid_6/Desktop/Andriod_Offloading/CloudServer && python3 app.py
2. Server Endpoints:

GET /health - Health check
POST /upload - Upload image file, returns grayscale version
