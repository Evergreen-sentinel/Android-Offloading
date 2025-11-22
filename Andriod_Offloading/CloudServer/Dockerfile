# Use a small official Python image
FROM python:3.11-slim

WORKDIR /app

# copy files
COPY app.py /app
COPY requirements.txt /app

# install deps
RUN pip install --no-cache-dir -r requirements.txt

EXPOSE 5000

# For quick prototyping we use Flask's built-in server; replace with gunicorn for production
CMD ["python", "app.py"]
