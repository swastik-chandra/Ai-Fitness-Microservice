# 🏋️ AI Fitness Platform — Enterprise SaaS

> Cloud-native, AI-powered fitness platform built with Spring Boot Microservices, Next.js 15, Apache Kafka, Kubernetes, and Google Gemini AI.

[![CI/CD](https://github.com/fitai/fitness-platform/actions/workflows/ci.yml/badge.svg)](https://github.com/fitai/fitness-platform/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 🏗️ Architecture

```
┌──────────────────┐     ┌──────────────────────┐
│   Next.js 15     │────▸│   API Gateway :8080   │
│   Frontend :3000 │     │   JWT + Rate Limiting  │
└──────────────────┘     └──────────┬─────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
              ┌─────▾───┐    ┌─────▾───┐    ┌──────▾──┐
              │Auth :8082│    │User :8081│    │AI :8083 │
              │JWT+Redis │    │PostgreSQL│    │Gemini AI│
              └─────┬────┘    └─────────┘    └────┬────┘
                    │                              │
              ┌─────▾────────────────────────┐    │
              │      Apache Kafka            │◂───┘
              │  activity.tracked            │
              │  user.registered             │
              └──┬──────────┬────────────┬───┘
                 │          │            │
           ┌─────▾──┐ ┌────▾────┐ ┌─────▾────┐
           │Activity │ │Notif.   │ │Analytics │
           │:8084    │ │:8085    │ │:8086     │
           │MongoDB  │ │WebSocket│ │Redis     │
           └─────────┘ └─────────┘ └──────────┘
```

## 🚀 Quick Start

### Prerequisites
- Java 25+ (Temurin)
- Node.js 22+
- Docker and Docker Compose
- Maven 3.9+

### Development
```bash
# 1. Start infrastructure
docker-compose -f docker-compose.dev.yml up -d

# 2. Start all services
./scripts/deploy.sh dev dev

# 3. Check health
./scripts/deploy.sh dev health
```

### Production (Docker)
```bash
# Start everything containerized
docker-compose -f docker-compose.prod.yml up -d --build
```

### Production (Kubernetes)
```bash
# Deploy to K8s cluster
kubectl apply -f k8s/
# Or use Helm
helm install fitness ./helm/fitness-platform -n fitness-platform --create-namespace
```

### AWS Deployment
```bash
# Provision infrastructure
cd terraform
terraform init
terraform apply -var-file="terraform.tfvars"
```

---

## 📋 Services

| Service | Port | Tech | Description |
|---------|------|------|-------------|
| **Frontend** | 3000 | Next.js 15, TypeScript, Tailwind | Dashboard, AI chat, analytics UI |
| **API Gateway** | 8080 | Spring Cloud Gateway | JWT validation, rate limiting, routing |
| **Auth Service** | 8082 | Spring Security, Redis | OAuth2, refresh tokens, token blocklist |
| **User Service** | 8081 | Spring Data JPA, PostgreSQL | User profile CRUD |
| **Activity Service** | 8084 | Spring Data MongoDB, Kafka | Workout tracking, event publishing |
| **AI Service** | 8083 | Google Gemini AI | Chatbot, workout plans, nutrition |
| **Notification Service** | 8085 | WebSocket (STOMP), Kafka | Real-time push notifications |
| **Analytics Service** | 8086 | MongoDB, Redis | Aggregated stats, streaks, trends |
| **Eureka** | 8761 | Spring Cloud Netflix | Service discovery |

---

## 🧪 Testing

```bash
# Backend unit tests (per service)
cd authService && ./mvnw test
cd activityService && ./mvnw test

# Frontend lint + type check
cd frontend && npm run lint && npx tsc --noEmit

# E2E tests (Playwright)
cd e2e && npx playwright test

# Run all tests
./scripts/deploy.sh dev test
```

| Layer | Framework | Test Count |
|-------|-----------|------------|
| Unit (Backend) | JUnit 5 + Mockito | 25+ |
| Controller | @WebMvcTest + MockMvc | 6 |
| Integration | @SpringBootTest | 3 |
| E2E | Playwright (3 browsers) | 16 |

---

## 📊 Monitoring

```bash
# Start monitoring stack
docker-compose -f docker-compose.monitoring.yml up -d
```

| Tool | Port | Purpose |
|------|------|---------|
| Prometheus | 9090 | Metrics collection |
| Grafana | 3001 | Dashboards (admin/admin) |
| Zipkin | 9411 | Distributed tracing |
| Elasticsearch | 9200 | Log storage |
| Kibana | 5601 | Log visualization |

---

## 🔒 Security

- **Authentication**: JWT access tokens (15 min) + refresh tokens (7 days)
- **Token Management**: Redis-backed blocklist for instant logout
- **Password Hashing**: BCrypt
- **API Security**: Rate limiting via Redis at Gateway
- **Secrets**: AWS Secrets Manager / K8s Secrets (env vars locally)
- **HTTPS**: Enforced via Ingress/CloudFront in production
- **Container Security**: Non-root users, Trivy scanning in CI/CD

---

## 🌐 Cloud Architecture (AWS)

```
CloudFront CDN ──▸ ALB ──▸ EKS Cluster (2-6 nodes)
                            ├── Gateway pods (HPA 2-10)
                            ├── Auth Service pods
                            ├── User Service pods
                            ├── Activity Service pods
                            ├── AI Service pods
                            ├── Notification Service pods
                            └── Analytics Service pods

                          RDS PostgreSQL (Multi-AZ)
                          ElastiCache Redis (2-node)
                          MongoDB Atlas
                          MSK / Self-hosted Kafka
```

**Infrastructure as Code**: Terraform (`terraform/`) provisions VPC, EKS, RDS, ElastiCache, S3, CloudFront, Secrets Manager.

---

## 📁 Project Structure

```
fitness-microservice/
├── eureka/                 # Service Discovery
├── gateway/                # API Gateway
├── authService/            # Authentication
├── userservice/            # User Management
├── activityService/        # Activity Tracking
├── aiService/              # AI Features
├── notificationService/    # Notifications
├── analyticsService/       # Analytics
├── frontend/               # Next.js Frontend
├── k8s/                    # Kubernetes Manifests
├── helm/                   # Helm Charts
├── terraform/              # AWS Infrastructure
├── monitoring/             # Prometheus/Grafana/Logstash
├── e2e/                    # Playwright E2E Tests
├── scripts/                # Deployment Scripts
├── .github/workflows/      # CI/CD Pipelines
├── docker-compose.dev.yml  # Dev Infrastructure
├── docker-compose.prod.yml # Production Stack
└── docker-compose.monitoring.yml
```

---

## 🤖 AI Features

| Feature | Endpoint | Description |
|---------|----------|-------------|
| Chatbot Coach | `POST /api/ai/chat` | Multi-turn AI fitness coaching |
| Workout Plans | `POST /api/ai/workout-plan/generate` | 7-day personalized plans |
| Nutrition | `POST /api/ai/nutrition/generate` | Meal plans with macros |
| Recommendations | Auto-generated | Per-activity AI analysis |

---

## 📄 License

MIT
