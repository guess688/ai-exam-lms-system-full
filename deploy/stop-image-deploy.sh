#!/usr/bin/env bash
set -e

if [ ! -f "docker-compose.image.yml" ]; then
  echo "Please run this script from the deploy directory."
  exit 1
fi

docker compose -f docker-compose.image.yml down
