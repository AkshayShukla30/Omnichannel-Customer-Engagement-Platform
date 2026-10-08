#!/usr/bin/env bash
# usage: scripts/simulate.sh [count] [host]
# logs in as priya and fires the simulator, handy when you want to feed the queue from a terminal
set -euo pipefail

COUNT="${1:-5}"
HOST="${2:-http://localhost:8080}"
JAR="$(mktemp)"
trap 'rm -f "$JAR"' EXIT

curl -sf -c "$JAR" -H 'Content-Type: application/json' \
  -d '{"username":"priya","password":"agent123"}' "$HOST/api/auth/login" > /dev/null

# go offline first, otherwise this login would steal the first interaction
curl -sf -b "$JAR" -H 'Content-Type: application/json' \
  -d '{"status":"OFFLINE"}' "$HOST/api/desk/status" > /dev/null

curl -sf -b "$JAR" -H 'Content-Type: application/json' \
  -d "{\"count\": $COUNT}" "$HOST/api/simulator/interactions"
echo
