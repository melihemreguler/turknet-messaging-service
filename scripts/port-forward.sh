#!/usr/bin/env bash
# Open local tunnels to in-cluster turknet services for development access.
#
# Topology (run from a developer machine, e.g. macOS):
#   local kubectl --> 127.0.0.1:6443 (SSH tunnel) --> server K3s API
#   local 27017/8080/5601 --> kubectl port-forward --> in-cluster Services
#
# Requirements:
#   - ~/.ssh/config has a Host entry (default name: `hetzner`) for the K3s node
#   - KUBECONFIG points at a kubeconfig whose `server:` is https://127.0.0.1:6443
#     (the K3s API port 6443 is not exposed publicly; we tunnel it via SSH)
#
# Usage:
#   ./scripts/port-forward.sh                       # foreground, Ctrl+C to stop
#   SSH_HOST=myhetzner ./scripts/port-forward.sh    # override SSH config Host
#   SKIP_SSH_TUNNEL=1 ./scripts/port-forward.sh     # don't manage the 6443 tunnel
#                                                   # (use if you already have one)

set -euo pipefail

NS="${NS:-turknet}"
SSH_HOST="${SSH_HOST:-hetzner}"
API_PORT="${API_PORT:-6443}"
SKIP_SSH_TUNNEL="${SKIP_SSH_TUNNEL:-0}"

declare -a TARGETS=(
  "mongodb:27017:27017"
  "kafka-ui:8080:8080"
  "kibana:5601:5601"
)

pids=()
ssh_pid=""

cleanup() {
  echo
  echo "Stopping port-forwards..."
  for pid in "${pids[@]}"; do
    kill "$pid" 2>/dev/null || true
  done
  if [[ -n "$ssh_pid" ]]; then
    echo "Closing SSH tunnel (pid $ssh_pid)..."
    kill "$ssh_pid" 2>/dev/null || true
  fi
  wait 2>/dev/null || true
}
trap cleanup EXIT INT TERM

# 1. Bring up the SSH tunnel to the K3s API (port 6443), unless one already exists
#    or the user explicitly opted out.
if [[ "$SKIP_SSH_TUNNEL" != "1" ]]; then
  if (echo >/dev/tcp/127.0.0.1/"$API_PORT") 2>/dev/null; then
    echo "127.0.0.1:${API_PORT} already reachable — reusing existing tunnel."
  else
    echo "Opening SSH tunnel: localhost:${API_PORT} -> ${SSH_HOST}:127.0.0.1:${API_PORT}"
    ssh -N -L "${API_PORT}:127.0.0.1:${API_PORT}" "$SSH_HOST" &
    ssh_pid=$!
    # Wait up to ~10s for the tunnel to be ready.
    for _ in {1..50}; do
      if (echo >/dev/tcp/127.0.0.1/"$API_PORT") 2>/dev/null; then break; fi
      sleep 0.2
    done
    if ! (echo >/dev/tcp/127.0.0.1/"$API_PORT") 2>/dev/null; then
      echo "ERROR: SSH tunnel to ${SSH_HOST}:${API_PORT} did not come up." >&2
      exit 1
    fi
  fi
fi

# 2. Start kubectl port-forward for each in-cluster service.
for t in "${TARGETS[@]}"; do
  IFS=":" read -r svc local remote <<<"$t"
  echo "Forwarding svc/${svc}  localhost:${local} -> ${remote}"
  kubectl -n "$NS" port-forward "svc/${svc}" "${local}:${remote}" >/dev/null &
  pids+=("$!")
done

cat <<EOF

Tunnels up. Local endpoints:
  Mongo     : mongodb://<user>:<pass>@localhost:27017/?authSource=admin
  Kafka UI  : http://localhost:8080
  Kibana    : http://localhost:5601

Press Ctrl+C to stop.
EOF

wait
