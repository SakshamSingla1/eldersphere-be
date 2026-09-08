#!/usr/bin/env bash
# Starts the ElderSphere backend for local development.
# - Ensures the local Postgres (Docker) is up and healthy.
# - Exports the env vars the app needs (JWT secret, DB connection, allowed frontend origins).
# - Runs the Spring Boot app in the foreground on port 8080.
#
# Usage: ./run.sh
# Override any default by exporting the same env var name before running, e.g.:
#   JWT_SECRET=my-own-secret ./run.sh

set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"

PORT="${SERVER_PORT:-8080}"

if lsof -nP -iTCP:"$PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  echo "Port $PORT is already in use — is the backend already running?"
  lsof -nP -iTCP:"$PORT" -sTCP:LISTEN
  exit 1
fi

echo "==> Ensuring Postgres is up (docker compose)..."
docker compose up -d postgres

echo "==> Waiting for Postgres to become healthy..."
i=0
until docker exec eldersphere-postgres pg_isready -U eldersphere -d eldersphere >/dev/null 2>&1 || [ "$i" -ge 30 ]; do
  sleep 1
  i=$((i + 1))
done
if ! docker exec eldersphere-postgres pg_isready -U eldersphere -d eldersphere >/dev/null 2>&1; then
  echo "Postgres did not become ready in time." >&2
  exit 1
fi
echo "    Postgres is ready."

export DATABASE_URL="${DATABASE_URL:-jdbc:postgresql://localhost:5434/eldersphere}"
export DATABASE_USER="${DATABASE_USER:-eldersphere}"
export DATABASE_PASSWORD="${DATABASE_PASSWORD:-eldersphere_dev_password}"
export JWT_SECRET="${JWT_SECRET:-QmmT91Hz/6m0dOZQYJt645GtdxQnJ+yr9DEj3yj6OrI=}"
export APP_FRONTEND_URL="${APP_FRONTEND_URL:-http://localhost:5174,http://localhost:5173,http://localhost:3000}"

echo "==> Starting ElderSphere backend on port $PORT..."
echo "    Swagger UI will be at http://localhost:$PORT/swagger-ui.html"
echo "    Health check:          http://localhost:$PORT/api/v1/health"
echo ""

exec ./mvnw spring-boot:run
