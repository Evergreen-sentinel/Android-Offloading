# Use a small official Python image
FROM python:3.11-slim

WORKDIR /app

# copy files
COPY app.py /app
COPY requirements.txt /app

# install deps
RUN pip install --no-cache-dir -r requirements.txt

# Use PORT environment variable or default to 5000
ENV PORT=5000
EXPOSE $PORT

# Use Gunicorn for production
CMD gunicorn --bind 0.0.0.0:$PORT app:app
