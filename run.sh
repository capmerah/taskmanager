#!/usr/bin/env bash
set -e

# Optional: run tests before starting containers
./gradlew clean test

# Build and start the app + mysql
docker compose up --build
