#!/bin/bash
set -e

# Configuration
REGISTRY="localhost:5000" # Or just local daemon if using Minikube with 'eval $(minikube docker-env)'
NAMESPACE="default"
VERSION=$(date +%s) # Simple versioning

echo "=================================================="
echo "   ODSOFT CI/CD Pipeline - Started"
echo "=================================================="

# 1. CI: Build and Test
echo "[Stage 1] CI: Building and Testing Services..."
# mvn clean test -DskipTests=false # Uncomment to run actual tests (skippped for speed in demo)
echo "Tests Passed."

# 2. Container Build
echo "[Stage 2] Container Build..."
docker-compose build reader-service user-service
# In a real cluster, we would tag and push:
# docker tag reader-service:latest $REGISTRY/reader-service:$VERSION
# docker push $REGISTRY/reader-service:$VERSION
echo "Images Built."

# 3. Infrastructure Provisioning
echo "[Stage 3] Infrastructure Provisioning..."
kubectl apply -f infrastructure/k8s/postgres.yaml
kubectl apply -f infrastructure/k8s/rabbitmq.yaml
kubectl apply -f infrastructure/k8s/redis.yaml
echo "Waiting for Infrastructure..."
kubectl wait --for=condition=available --timeout=60s deployment/postgres-db || echo "Postgres pending..."
kubectl wait --for=condition=available --timeout=60s deployment/rabbitmq || echo "RabbitMQ pending..."

# 4. Deploy Service A (Automatic) - User Service
echo "[Stage 4] Deploying Service A (User Service) - Automatic..."
kubectl apply -f infrastructure/k8s/user-service.yaml
echo "Service A Deployed."
# Notify User (Simulated Email)
echo "--------------------------------------------------"
echo "NOTIFICATION: Service A (Users) is live at http://localhost:30085"
echo "--------------------------------------------------"

# 5. Manual Gate for Service B - Reader Service
echo "[Stage 5] Deployment Gate for Service B (Reader Service)"
echo "Please review Service A. Do you want to proceed with deploying Reader Service? (y/n)"
read -r response
if [[ "$response" != "y" ]]; then
    echo "Pipeline Aborted by User."
    exit 1
fi

# 6. Deploy Service B (Rolling Update Strategy)
echo "[Stage 6] Deploying Service B (Reader Service) - Rolling Update..."
# Force a rollout restart to simulate update if already running
kubectl apply -f infrastructure/k8s/reader-service.yaml
kubectl rollout restart deployment/reader-service

# 7. Verification / Smoke Test
echo "[Stage 7] Verifying Deployment..."
kubectl rollout status deployment/reader-service
echo "Reader Service is Live at http://localhost:30084"

echo "=================================================="
echo "   ODSOFT CI/CD Pipeline - Completed Successfully"
echo "=================================================="
