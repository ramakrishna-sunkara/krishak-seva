# Cloud Run container for Krishak Seva web (Flask API + static frontend)
FROM python:3.11-slim

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV PORT=8080

WORKDIR /app/backend

# Audio / ASR dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    libsndfile1 \
    ffmpeg \
    && rm -rf /var/lib/apt/lists/*

COPY backend/requirements.txt ./requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

COPY backend/ ./
COPY frontend/ /app/frontend/

RUN mkdir -p uploads temp_audio static/audio_replies

EXPOSE 8080

CMD exec gunicorn app:app \
    --bind 0.0.0.0:${PORT} \
    --workers 1 \
    --threads 4 \
    --timeout 120
