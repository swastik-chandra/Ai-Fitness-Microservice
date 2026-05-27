#!/bin/bash
# ============================================
# Fitness Platform — Automated Deployment
# ============================================
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() { echo -e "${BLUE}[DEPLOY]${NC} $1"; }
success() { echo -e "${GREEN}[✓]${NC} $1"; }
warn() { echo -e "${YELLOW}[!]${NC} $1"; }
error() { echo -e "${RED}[✗]${NC} $1"; exit 1; }

ENVIRONMENT="${1:-dev}"
DOCKER_REGISTRY="${DOCKER_REGISTRY:-docker.io}"
IMAGE_PREFIX="${IMAGE_PREFIX:-fitai}"
TAG="${TAG:-latest}"

SERVICES=(eureka gateway userservice authService activityService aiService notificationService analyticsService frontend)
PORTS=(8761 8080 8081 8082 8084 8083 8085 8086 3000)

# ============================================
# Functions
# ============================================

check_prerequisites() {
    log "Checking prerequisites..."
    for cmd in docker docker-compose java mvn node npm; do
        if ! command -v $cmd &>/dev/null; then
            warn "$cmd not found — some features may not work"
        else
            success "$cmd available"
        fi
    done
}

start_dev() {
    log "Starting development environment..."
    docker-compose -f docker-compose.dev.yml up -d
    success "Infrastructure started (PostgreSQL, MongoDB, Kafka, Redis)"

    log "Waiting for services to be healthy..."
    sleep 10

    log "Starting backend services..."
    for i in "${!SERVICES[@]}"; do
        svc="${SERVICES[$i]}"
        port="${PORTS[$i]}"
        if [ "$svc" = "frontend" ]; then
            log "Starting frontend..."
            cd frontend && npm run dev &
            cd ..
        else
            log "Starting $svc on port $port..."
            cd "$svc" && ./mvnw spring-boot:run -Dspring-boot.run.profiles=default &
            cd ..
        fi
    done

    success "All services starting. Check ports: ${PORTS[*]}"
}

start_prod() {
    log "Starting production environment..."
    docker-compose -f docker-compose.prod.yml up -d --build
    success "Production stack deployed"
}

start_monitoring() {
    log "Starting monitoring stack..."
    docker-compose -f docker-compose.monitoring.yml up -d
    success "Monitoring started: Prometheus(:9090), Grafana(:3001), Zipkin(:9411), Kibana(:5601)"
}

build_images() {
    log "Building Docker images..."
    for svc in "${SERVICES[@]}"; do
        log "Building $svc..."
        docker build -t "${IMAGE_PREFIX}-${svc}:${TAG}" "./${svc}"
        success "Built ${IMAGE_PREFIX}-${svc}:${TAG}"
    done
}

push_images() {
    log "Pushing images to ${DOCKER_REGISTRY}..."
    for svc in "${SERVICES[@]}"; do
        docker tag "${IMAGE_PREFIX}-${svc}:${TAG}" "${DOCKER_REGISTRY}/${IMAGE_PREFIX}-${svc}:${TAG}"
        docker push "${DOCKER_REGISTRY}/${IMAGE_PREFIX}-${svc}:${TAG}"
        success "Pushed ${svc}"
    done
}

deploy_k8s() {
    log "Deploying to Kubernetes..."
    kubectl apply -f k8s/namespace.yaml
    kubectl apply -f k8s/configmaps/
    kubectl apply -f k8s/secrets/
    kubectl apply -f k8s/deployments/
    kubectl apply -f k8s/ingress/
    success "Kubernetes deployment applied"

    log "Waiting for rollout..."
    kubectl rollout status deployment/gateway -n fitness-platform --timeout=5m
    success "Deployment complete"
    kubectl get pods -n fitness-platform
}

deploy_helm() {
    local VALUES_FILE="values.yaml"
    if [ "$ENVIRONMENT" = "prod" ]; then
        VALUES_FILE="values-prod.yaml"
    fi

    log "Deploying with Helm (${ENVIRONMENT})..."
    helm upgrade --install fitness-platform ./helm/fitness-platform \
        -f "./helm/fitness-platform/${VALUES_FILE}" \
        --namespace fitness-platform \
        --create-namespace \
        --set image.tag="${TAG}" \
        --wait --timeout 10m
    success "Helm deployment complete"
}

deploy_terraform() {
    log "Provisioning AWS infrastructure with Terraform..."
    cd terraform
    terraform init
    terraform plan -out=tfplan
    warn "Review the plan above. Apply? (y/n)"
    read -r confirm
    if [ "$confirm" = "y" ]; then
        terraform apply tfplan
        success "Terraform infrastructure provisioned"
    else
        warn "Terraform apply skipped"
    fi
    cd ..
}

run_tests() {
    log "Running tests..."
    for svc in "${SERVICES[@]}"; do
        if [ "$svc" = "frontend" ]; then
            log "Running frontend tests..."
            cd frontend && npm run lint && npx tsc --noEmit && npm run build
            cd ..
        else
            log "Testing $svc..."
            cd "$svc" && ./mvnw test -B 2>/dev/null || warn "$svc tests failed"
            cd ..
        fi
    done
    success "Tests complete"
}

health_check() {
    log "Running health checks..."
    for i in "${!SERVICES[@]}"; do
        svc="${SERVICES[$i]}"
        port="${PORTS[$i]}"
        if curl -sf "http://localhost:${port}/actuator/health" >/dev/null 2>&1; then
            success "$svc (:${port}) — healthy"
        elif curl -sf "http://localhost:${port}" >/dev/null 2>&1; then
            success "$svc (:${port}) — responding"
        else
            warn "$svc (:${port}) — not responding"
        fi
    done
}

stop_all() {
    log "Stopping all services..."
    docker-compose -f docker-compose.dev.yml down 2>/dev/null || true
    docker-compose -f docker-compose.prod.yml down 2>/dev/null || true
    docker-compose -f docker-compose.monitoring.yml down 2>/dev/null || true
    pkill -f "spring-boot:run" 2>/dev/null || true
    pkill -f "next-server" 2>/dev/null || true
    success "All services stopped"
}

# ============================================
# Main
# ============================================

case "${2:-help}" in
    dev)        check_prerequisites && start_dev ;;
    prod)       check_prerequisites && start_prod ;;
    monitor)    start_monitoring ;;
    build)      build_images ;;
    push)       push_images ;;
    k8s)        deploy_k8s ;;
    helm)       deploy_helm ;;
    terraform)  deploy_terraform ;;
    test)       run_tests ;;
    health)     health_check ;;
    stop)       stop_all ;;
    all)
        check_prerequisites
        build_images
        push_images
        deploy_k8s
        ;;
    *)
        echo ""
        echo "  🏋️ Fitness Platform Deploy Script"
        echo ""
        echo "  Usage: $0 <environment> <command>"
        echo ""
        echo "  Environments: dev | prod"
        echo ""
        echo "  Commands:"
        echo "    dev        Start development environment"
        echo "    prod       Start production (Docker) environment"
        echo "    monitor    Start monitoring stack"
        echo "    build      Build all Docker images"
        echo "    push       Push images to registry"
        echo "    k8s        Deploy to Kubernetes"
        echo "    helm       Deploy with Helm"
        echo "    terraform  Provision AWS infrastructure"
        echo "    test       Run all tests"
        echo "    health     Check service health"
        echo "    stop       Stop all services"
        echo "    all        Build → Push → Deploy to K8s"
        echo ""
        ;;
esac
