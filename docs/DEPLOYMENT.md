# Cloud Deployment Guide (AWS)

## Prerequisites
- AWS CLI configured with appropriate credentials
- Terraform >= 1.9.0
- kubectl
- Helm 3.x
- Docker

## Architecture

```
Internet → CloudFront CDN → ALB → EKS Cluster
                                    ├── Gateway (HPA 2-10)
                                    ├── Auth Service (2 replicas)
                                    ├── User Service (2 replicas)
                                    ├── Activity Service (2 replicas)
                                    ├── AI Service (2 replicas)
                                    ├── Notification Service (1 replica)
                                    ├── Analytics Service (2 replicas)
                                    └── Frontend (2 replicas)

Data Layer:
  ├── RDS PostgreSQL (Multi-AZ, encrypted)
  ├── ElastiCache Redis (2-node replication)
  └── MongoDB Atlas (external)
```

## Step 1: Provision Infrastructure

```bash
cd terraform

# Initialize
terraform init

# Review plan
terraform plan \
  -var="db_username=fitnessadmin" \
  -var="db_password=YOUR_SECURE_PASSWORD" \
  -var="jwt_secret=YOUR_JWT_SECRET" \
  -var="gemini_api_key=YOUR_GEMINI_KEY"

# Apply
terraform apply
```

### Resources Created
| Resource | Specification | Monthly Cost (est.) |
|----------|--------------|---------------------|
| EKS Cluster | 3x t3.medium nodes | ~$200 |
| RDS PostgreSQL | db.t3.medium, Multi-AZ | ~$70 |
| ElastiCache Redis | cache.t3.medium, 2 nodes | ~$50 |
| NAT Gateway | 3 AZs | ~$100 |
| CloudFront | Pay-per-request | ~$10 |
| **Total** | | **~$430/month** |

## Step 2: Build & Push Images

```bash
# Build all images
./scripts/deploy.sh prod build

# Tag for registry
export REGISTRY=your-ecr-or-dockerhub
./scripts/deploy.sh prod push
```

## Step 3: Configure kubectl

```bash
aws eks update-kubeconfig --name fitness-platform-eks --region us-east-1
kubectl get nodes  # Verify connectivity
```

## Step 4: Deploy with Helm

```bash
helm upgrade --install fitness-platform ./helm/fitness-platform \
  -f ./helm/fitness-platform/values-prod.yaml \
  --namespace fitness-platform \
  --create-namespace \
  --set secrets.dbPassword=YOUR_RDS_PASSWORD \
  --set secrets.jwtSecret=YOUR_JWT_SECRET \
  --set secrets.geminiApiKey=YOUR_GEMINI_KEY \
  --set image.registry=YOUR_REGISTRY \
  --wait --timeout 10m
```

## Step 5: Verify Deployment

```bash
kubectl get pods -n fitness-platform
kubectl get svc -n fitness-platform
kubectl get hpa -n fitness-platform
kubectl logs -f deployment/gateway -n fitness-platform
```

## Step 6: Setup MongoDB Atlas

1. Create cluster at [mongodb.com/atlas](https://mongodb.com/atlas)
2. Whitelist EKS NAT Gateway IPs
3. Update ConfigMap:
```bash
kubectl edit configmap app-config -n fitness-platform
# Set SPRING_DATA_MONGODB_URI to Atlas connection string
```

## Step 7: Setup DNS & TLS

1. Create ACM certificate for `fitai.dev` and `api.fitai.dev`
2. Update Ingress with ACM ARN annotation
3. Point DNS to ALB/CloudFront

## Monitoring

```bash
# Access Grafana (port-forward)
kubectl port-forward svc/grafana 3001:3000 -n monitoring

# Access Prometheus
kubectl port-forward svc/prometheus 9090:9090 -n monitoring
```

## Scaling

```bash
# Manual scale
kubectl scale deployment ai-service --replicas=5 -n fitness-platform

# HPA is auto-configured for gateway (2-10 pods at 70% CPU)
kubectl get hpa -n fitness-platform
```

## Rollback

```bash
# Helm rollback
helm rollback fitness-platform 1 -n fitness-platform

# K8s rollback
kubectl rollout undo deployment/gateway -n fitness-platform
```
