#!/bin/bash
set -e

echo "Deploying Fresh Farm Juba..."

# Apply Kubernetes configurations
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml
kubectl apply -f kubernetes/ingress.yaml

# Wait for rollout
kubectl rollout status deployment/freshfarmjuba

echo "Deployment completed successfully!"
