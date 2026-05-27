# Security Architecture

## Authentication & Authorization

### JWT Token Flow
```
User → POST /api/auth/login → Auth Service → JWT Access Token (15 min) + Refresh Token (7 days)
User → Authorization: Bearer <token> → Gateway → JWT Validation → Backend Service
```

### Token Types
| Token | TTL | Storage | Purpose |
|-------|-----|---------|---------|
| Access Token | 15 minutes | Client-side | API authentication |
| Refresh Token | 7 days | Redis | Token rotation |

### Token Blocklist (Logout)
- On `POST /api/auth/logout`, the access token is added to Redis with its remaining TTL
- Gateway's `JwtAuthenticationFilter` checks the Redis blocklist on every request
- Refresh tokens are deleted from Redis on logout

## API Security

### Rate Limiting
- Gateway uses Redis-backed rate limiting via Spring Cloud Gateway `RequestRateLimiter`
- NGINX Ingress adds L7 rate limiting (100 req/min) in Kubernetes

### CORS Policy
```yaml
allowedOrigins: [http://localhost:3000, https://fitai.dev]
allowedMethods: [GET, POST, PUT, DELETE, OPTIONS]
allowCredentials: true
```

## Secret Management

| Environment | Solution |
|-------------|----------|
| Local Dev | Environment variables / `.env` file |
| Docker Compose | `${VAR}` interpolation from `.env` |
| Kubernetes | K8s Secrets (base64 encoded) |
| AWS Production | AWS Secrets Manager (injected via Terraform) |

### Secrets Inventory
| Secret | Used By |
|--------|---------|
| `JWT_SECRET` | Auth Service, Gateway, User Service |
| `DB_USERNAME` / `DB_PASSWORD` | Auth Service, User Service |
| `GEMINI_API_KEY` | AI Service |
| `DOCKER_PASSWORD` | CI/CD Pipeline |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | Terraform, CI/CD |

## Container Security
- All Dockerfiles use non-root users (`spring` / `nextjs`)
- Trivy scans in CI/CD for CRITICAL/HIGH vulnerabilities
- JRE-only production images (no JDK, no build tools)
- No secrets baked into images

## Network Security
- Private subnets for EKS worker nodes, RDS, ElastiCache
- Security groups restrict DB access to EKS cluster only
- TLS termination at CloudFront / Ingress
- ElastiCache Redis with encryption in-transit and at-rest

## HTTPS
- **Local**: HTTP (development only)
- **Kubernetes**: TLS termination at NGINX Ingress
- **AWS**: CloudFront enforces HTTPS redirect, ACM certificates
