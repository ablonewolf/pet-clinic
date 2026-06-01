#!/usr/bin/env bash
set -euo pipefail

usage() {
  printf 'Usage: %s <imperative|reactive> [requests] [concurrency]\n' "$0"
  printf '\n'
  printf 'Examples:\n'
  printf '  %s imperative\n' "$0"
  printf '  %s reactive 5000 50\n' "$0"
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

mode="${1:-}"
requests="${2:-5000}"
concurrency="${3:-50}"
base_url="${BASE_URL:-http://localhost:8001}"

case "$mode" in
  imperative)
    path="/pet-clinic/details"
    ;;
  reactive)
    path="/reactive/pet-clinic/details"
    ;;
  *)
    usage >&2
    exit 1
    ;;
esac

if ! command -v jps >/dev/null 2>&1; then
  printf 'Error: jps was not found. Install/use a JDK and retry.\n' >&2
  exit 1
fi

runner=""
if command -v ab >/dev/null 2>&1; then
  runner="ab"
elif command -v wrk >/dev/null 2>&1; then
  runner="wrk"
elif command -v curl >/dev/null 2>&1; then
  runner="curl"
else
  printf 'Error: none of ab, wrk, or curl was found. Install one and retry.\n' >&2
  exit 1
fi

pid="$(jps | awk '/Application|pet-clinic/ { print $1; exit }')"
if [[ -z "$pid" ]]; then
  printf 'Error: could not find a running pet-clinic Java process via jps.\n' >&2
  printf 'Start the Micronaut app first, then rerun this script.\n' >&2
  exit 1
fi

mkdir -p build/benchmarks
timestamp="$(date +%Y%m%d-%H%M%S)"
cpu_log="build/benchmarks/${mode}-cpu-${timestamp}.log"
bench_log="build/benchmarks/${mode}-${runner}-${timestamp}.log"
url="${base_url}${path}"

printf 'Mode: %s\n' "$mode"
printf 'URL: %s\n' "$url"
printf 'PID: %s\n' "$pid"
printf 'Requests: %s\n' "$requests"
printf 'Concurrency: %s\n' "$concurrency"
printf 'CPU log: %s\n' "$cpu_log"
printf 'Runner: %s\n' "$runner"
printf 'Benchmark log: %s\n' "$bench_log"
printf '\n'

cpu_pid=""
if command -v pidstat >/dev/null 2>&1; then
  pidstat -p "$pid" 1 > "$cpu_log" &
  cpu_pid="$!"
else
  printf 'pidstat was not found; CPU sampling is skipped.\n' | tee "$cpu_log"
fi

cleanup() {
  if [[ -n "$cpu_pid" ]] && kill -0 "$cpu_pid" >/dev/null 2>&1; then
    kill "$cpu_pid" >/dev/null 2>&1 || true
    wait "$cpu_pid" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

case "$runner" in
  ab)
    ab -n "$requests" -c "$concurrency" "$url" | tee "$bench_log"
    ;;
  wrk)
    wrk -t4 -c"$concurrency" -d30s "$url" | tee "$bench_log"
    ;;
  curl)
    printf 'Using curl fallback. This is useful for smoke checks, not serious load testing.\n' | tee "$bench_log"
    for ((i = 1; i <= requests; i++)); do
      curl -fsS -o /dev/null "$url"
    done
    printf 'Completed %s sequential curl request(s).\n' "$requests" | tee -a "$bench_log"
    ;;
esac

printf '\nDone. Review these files for comparison:\n'
printf '  %s\n' "$cpu_log"
printf '  %s\n' "$bench_log"
