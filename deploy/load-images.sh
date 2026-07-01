#!/usr/bin/env bash
set -e

IMAGE_ARCHIVE="images/ai-exam-lms-images-1.0.0.tar"

if [ ! -f "docker-compose.image.yml" ]; then
  echo "Please run this script from the deploy directory."
  exit 1
fi

if [ ! -f "$IMAGE_ARCHIVE" ]; then
  echo "Image archive not found: $IMAGE_ARCHIVE"
  exit 1
fi

docker load -i "$IMAGE_ARCHIVE"
docker images | grep -E "ai-exam-lms|mysql|redis|minio"
