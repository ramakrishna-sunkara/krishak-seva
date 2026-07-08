#!/usr/bin/env bash
# Deploy Krishak Seva web to Google Cloud Run
set -euo pipefail

PROJECT_ID="${GCP_PROJECT_ID:-}"
REGION="${GCP_REGION:-asia-south1}"
SERVICE_NAME="${GCP_SERVICE_NAME:-krishak-seva-web}"
IMAGE="gcr.io/${PROJECT_ID}/${SERVICE_NAME}"

if [[ -z "${PROJECT_ID}" ]]; then
  echo "Set GCP_PROJECT_ID to your Google Cloud project ID."
  echo "Example: export GCP_PROJECT_ID=my-farming-app"
  exit 1
fi

if ! command -v gcloud >/dev/null 2>&1; then
  echo "Install Google Cloud SDK: https://cloud.google.com/sdk/docs/install"
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

echo "==> Enabling required APIs..."
gcloud services enable run.googleapis.com cloudbuild.googleapis.com artifactregistry.googleapis.com \
  --project="${PROJECT_ID}"

echo "==> Building container with Cloud Build..."
gcloud builds submit "${SCRIPT_DIR}" \
  --tag "${IMAGE}" \
  --project="${PROJECT_ID}"

echo "==> Deploying to Cloud Run (${REGION})..."
gcloud run deploy "${SERVICE_NAME}" \
  --image "${IMAGE}" \
  --platform managed \
  --region "${REGION}" \
  --allow-unauthenticated \
  --memory 1Gi \
  --cpu 1 \
  --timeout 300 \
  --min-instances 0 \
  --max-instances 3 \
  --set-env-vars "GROQ_API_KEY=${GROQ_API_KEY:-}" \
  --set-env-vars "TWILIO_ACCOUNT_SID=${TWILIO_ACCOUNT_SID:-}" \
  --set-env-vars "TWILIO_AUTH_TOKEN=${TWILIO_AUTH_TOKEN:-}" \
  --set-env-vars "TWILIO_PHONE_NUMBER=${TWILIO_PHONE_NUMBER:-}" \
  --project="${PROJECT_ID}"

echo ""
echo "Deployed. Service URL:"
gcloud run services describe "${SERVICE_NAME}" \
  --region "${REGION}" \
  --project="${PROJECT_ID}" \
  --format='value(status.url)'
