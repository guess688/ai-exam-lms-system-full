#!/usr/bin/env bash
set -e

if [ ! -f "docker-compose.image.yml" ]; then
  echo "Please run this script from the deploy directory."
  exit 1
fi

if [ ! -f ".env" ]; then
  echo "deploy/.env not found. Create and edit it first:"
  echo "cp .env.example .env"
  echo "vim .env"
  exit 1
fi

docker compose -f docker-compose.image.yml up -d
docker compose -f docker-compose.image.yml ps

echo "View backend logs:"
echo "docker compose -f docker-compose.image.yml logs -f backend"
