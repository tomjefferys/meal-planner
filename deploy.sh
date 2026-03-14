#!/usr/bin/env bash
# Deploy script for Meal Planner
# Builds the app and deploys it to a Raspberry Pi, then restarts the service

set -e

# --- Configuration ---
PI_USER="${PI_USER:-pi}"
PI_HOST="${PI_HOST:-raspberrypi.local}"
PI_DEPLOY_DIR="${PI_DEPLOY_DIR:-/opt/meal-planner}"
PI_SERVICE="${PI_SERVICE:-meal-planner}"
JAR_NAME="meal-planner-1.0.0.jar"
# ---------------------

echo "🍽️  Deploying Meal Planner to ${PI_USER}@${PI_HOST}..."

# Build
echo "🔨 Building..."
./build.sh

JAR_PATH="backend/target/${JAR_NAME}"

if [ ! -f "$JAR_PATH" ]; then
  echo "❌ JAR not found at ${JAR_PATH}"
  exit 1
fi

# Upload
echo "📤 Uploading ${JAR_NAME} to ${PI_USER}@${PI_HOST}:${PI_DEPLOY_DIR}/"
ssh "${PI_USER}@${PI_HOST}" "mkdir -p ${PI_DEPLOY_DIR}"
scp "${JAR_PATH}" "${PI_USER}@${PI_HOST}:${PI_DEPLOY_DIR}/${JAR_NAME}"

# Restart service
echo "🔄 Restarting service '${PI_SERVICE}'..."
ssh "${PI_USER}@${PI_HOST}" "sudo systemctl restart ${PI_SERVICE}"

echo ""
echo "✅ Deployment complete!"
echo "Service status:"
ssh "${PI_USER}@${PI_HOST}" "sudo systemctl status ${PI_SERVICE} --no-pager -l"
