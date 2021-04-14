#!/usr/bin/env sh
kubectl config use-context court-probation-preprod
kubectl create secret generic perf-test-data-user-credentials \
  --from-literal=username="$1" \
  --from-literal=password="$2"
