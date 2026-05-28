#!/usr/bin/env bash
# Open local tunnels to in-cluster turknet services for development access.
# Requires KUBECONFIG to point at a cluster with the `turknet` namespace.
#
# Usage:
#   ./scripts/port-forward.sh            # foreground, Ctrl+C to stop
#   KUBECONFIG=~/.kube/k3s-config ./scripts/port-forward.sh

set -euo pipefail

NS="${NS:-turknet}"

declare -a TARGETS=(
  "mongodb:27017:27017"
  "kafka-ui:8080:8080"
  "kibana:5601:5601"
)

pids=()

cleanup() {
  echo
  echo "Stopping port-forwards..."
  for pid in "${pids[@]}"; do
    kill "$pid" 2>/dev/null || true
  done
  wait 2>/dev/null || true
}
trap cleanup EXIT INT TERM

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
