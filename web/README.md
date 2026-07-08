# Krishak Seva — Web

Full-stack AI farming assistant (Flask API + vanilla JS frontend).

## Run locally

```bash
cd web/backend
python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env   # add GROQ_API_KEY (required), Twilio keys (optional)
python app.py
```

Open http://localhost:5000

## Deploy to Google Cloud Run (recommended)

**Best for this app** — serverless, HTTPS URL, scales to zero, ~₹0 when idle.

### Prerequisites

1. [Google Cloud account](https://cloud.google.com/free)
2. [Install gcloud CLI](https://cloud.google.com/sdk/docs/install)
3. Create a project and enable billing (free tier covers light demo traffic)

### One-command deploy

```bash
export GCP_PROJECT_ID=your-gcp-project-id
export GROQ_API_KEY=your_groq_key
# optional:
export TWILIO_ACCOUNT_SID=...
export TWILIO_AUTH_TOKEN=...
export TWILIO_PHONE_NUMBER=...

gcloud auth login
gcloud config set project "$GCP_PROJECT_ID"

chmod +x web/deploy-gcp.sh
./web/deploy-gcp.sh
```

The script builds `web/Dockerfile` via Cloud Build and deploys to **Cloud Run** in `asia-south1` (Mumbai). You get a public HTTPS URL like:

`https://krishak-seva-web-xxxxx-el.a.run.app`

### Set secrets after first deploy (alternative)

```bash
gcloud run services update krishak-seva-web \
  --region asia-south1 \
  --set-env-vars "GROQ_API_KEY=...,TWILIO_ACCOUNT_SID=..."
```

### Why Cloud Run fits this stack

| Need | Cloud Run |
|------|-----------|
| Flask + Gunicorn | Native container support |
| File uploads (crop photos) | Ephemeral disk OK for demo |
| Groq API (no local GPU) | Lightweight container (~1 GB RAM) |
| Public HTTPS URL | Automatic SSL |
| India latency | Use `asia-south1` region |

> **Note:** Farmer profiles are stored in `farmer_profiles.json` inside the container — data resets on redeploy. For production, swap to Firestore or Cloud SQL.

## Deploy to Render (alternative)

See root `render.yaml` — connect repo on [render.com](https://render.com).
