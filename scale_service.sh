#!/bin/bash
# scale_service.sh
# Usage: ./scale_service.sh <deployment_name> <replicas>

DEPLOYMENT=$1
REPLICAS=$2
NAMESPACE="default"

if [ -z "$DEPLOYMENT" ] || [ -z "$REPLICAS" ]; then
    echo "Usage: ./scale_service.sh <deployment> <replicas>"
    exit 1
fi

echo "⚖️  Auto-Scaling triggered for $DEPLOYMENT..."
echo "Current Status:"
kubectl get deployment $DEPLOYMENT -n $NAMESPACE

echo "Scaling to $REPLICAS replicas..."
kubectl scale deployment/$DEPLOYMENT --replicas=$REPLICAS -n $NAMESPACE

# Wait for scale out
kubectl rollout status deployment/$DEPLOYMENT --timeout=30s

echo "✅ Scaling complete. New status:"
kubectl get deployment $DEPLOYMENT -n $NAMESPACE
