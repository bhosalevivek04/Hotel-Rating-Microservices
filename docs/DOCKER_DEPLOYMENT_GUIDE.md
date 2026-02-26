# Docker Deployment Guide

## Overview

This guide explains how to deploy the entire microservices architecture using Docker and Docker Compose.

## Prerequisites

- Docker Desktop installed (Windows/Mac) or Docker Engine (Linux)
- Docker Compose installed (usually comes with Docker Desktop)
- At least 8GB RAM available for Docker
- At least 20GB free disk space

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Docker Network                            │
│                 (microservices-network)                      │
│                                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  MySQL   │  │PostgreSQL│  │  Eureka  │  │   Auth   │   │
│  │  :3306   │  │  :5432   │  │  :8761   │  │  :8086   │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│                                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │   User   │  │  Hotel   │  │  Rating  │  │ Gateway  │   │
│  │  :8081   │  │  :8082   │  │  :8083   │  │  :8084   │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## Quick Start

### 1. Build and Start All Services

```bash
# Build and start all services
docker-compose up -d --build

# This will:
# - Build Docker images for all services
# - Start MySQL and PostgreSQL databases
# - Start Service Registry (Eureka)
# - Start all microservices
# - Create a Docker network for inter-service communication
```

### 2. Check Service Status

```bash
# View all running containers
docker-compose ps

# View logs from all services
docker-compose logs -f

# View logs from specific service
docker-compose logs -f auth-service
docker-compose logs -f user-service
```

### 3. Wait for Services to Start

Services start in this order:
1. Databases (MySQL, PostgreSQL) - ~30 seconds
2. Service Registry (Eureka) - ~40 seconds
3. Microservices (Auth, User, Hotel, Rating) - ~60 seconds
4. API Gateway - ~70 seconds

**Total startup time: ~2-3 minutes**

### 4. Verify Services

```bash
# Check Eureka Dashboard
# Open: http://localhost:8761

# Check if all services are registered:
# - AUTHSERVICE
# - USERSERVICE
# - HOTELSERVICE
# - RATINGSERVICE
# - APIGATEWAY
```

### 5. Test the Application

```powershell
# Register a user
$registerBody = @{
    username = "docker_user"
    password = "password123"
    email = "docker@example.com"
    name = "Docker User"
    about = "Testing Docker deployment"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8086/auth/register" -Method POST -ContentType "application/json" -Body $registerBody

$token = $response.token
$userId = $response.userId

Write-Host "User registered successfully!"
Write-Host "Token: $token"
Write-Host "UserId: $userId"
```

## Docker Commands

### Starting Services

```bash
# Start all services
docker-compose up -d

# Start specific service
docker-compose up -d auth-service

# Start with build (rebuild images)
docker-compose up -d --build

# Start without detached mode (see logs)
docker-compose up
```

### Stopping Services

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: deletes all data)
docker-compose down -v

# Stop specific service
docker-compose stop auth-service
```

### Viewing Logs

```bash
# View all logs
docker-compose logs

# Follow logs (real-time)
docker-compose logs -f

# View logs from specific service
docker-compose logs auth-service

# View last 100 lines
docker-compose logs --tail=100

# View logs with timestamps
docker-compose logs -t
```

### Scaling Services

```bash
# Scale user-service to 3 instances
docker-compose up -d --scale user-service=3

# Scale hotel-service to 2 instances
docker-compose up -d --scale hotel-service=2
```

### Restarting Services

```bash
# Restart all services
docker-compose restart

# Restart specific service
docker-compose restart auth-service

# Restart with rebuild
docker-compose up -d --build --force-recreate
```

### Inspecting Services

```bash
# View running containers
docker-compose ps

# View container details
docker inspect auth-service

# Execute command in container
docker-compose exec auth-service bash

# View container resource usage
docker stats
```

## Service URLs

When running in Docker:

| Service | Internal URL | External URL |
|---------|-------------|--------------|
| Service Registry | http://service-registry:8761 | http://localhost:8761 |
| API Gateway | http://api-gateway:8084 | http://localhost:8084 |
| AuthService | http://auth-service:8086 | http://localhost:8086 |
| UserService | http://user-service:8081 | http://localhost:8081 |
| HotelService | http://hotel-service:8082 | http://localhost:8082 |
| RatingService | http://rating-service:8083 | http://localhost:8083 |
| MySQL | mysql:3306 | localhost:3306 |
| PostgreSQL | postgres:5432 | localhost:5432 |

## Database Access

### MySQL

```bash
# Connect to MySQL container
docker-compose exec mysql mysql -u root -proot

# List databases
SHOW DATABASES;

# Use specific database
USE auth_service_db;

# View tables
SHOW TABLES;
```

### PostgreSQL

```bash
# Connect to PostgreSQL container
docker-compose exec postgres psql -U postgres -d microservice

# List databases
\l

# Connect to database
\c microservice

# List tables
\dt
```

## Troubleshooting

### Service Won't Start

```bash
# Check logs
docker-compose logs service-name

# Check if port is already in use
netstat -ano | findstr :8081

# Restart service
docker-compose restart service-name

# Rebuild and restart
docker-compose up -d --build --force-recreate service-name
```

### Database Connection Issues

```bash
# Check if database is healthy
docker-compose ps

# Check database logs
docker-compose logs mysql
docker-compose logs postgres

# Restart database
docker-compose restart mysql
```

### Service Not Registered in Eureka

```bash
# Wait 30-60 seconds after service starts
# Check service logs
docker-compose logs auth-service

# Check Eureka logs
docker-compose logs service-registry

# Restart service
docker-compose restart auth-service
```

### Out of Memory

```bash
# Check Docker resource usage
docker stats

# Increase Docker memory in Docker Desktop settings
# Recommended: 8GB RAM minimum

# Stop unnecessary services
docker-compose stop rating-service
```

### Clean Start

```bash
# Stop all services
docker-compose down

# Remove all volumes (WARNING: deletes all data)
docker-compose down -v

# Remove all images
docker-compose down --rmi all

# Rebuild and start
docker-compose up -d --build
```

## Environment Variables

You can override environment variables in `docker-compose.yml`:

```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/auth_service_db
  SPRING_DATASOURCE_USERNAME: root
  SPRING_DATASOURCE_PASSWORD: root
  JWT_SECRET: your-secret-key
  JWT_EXPIRATION: 36000000
```

Or create a `.env` file:

```env
MYSQL_ROOT_PASSWORD=root
POSTGRES_PASSWORD=root
JWT_SECRET=your-secret-key
JWT_EXPIRATION=36000000
```

## Production Deployment

### Docker Swarm

```bash
# Initialize swarm
docker swarm init

# Deploy stack
docker stack deploy -c docker-compose.yml microservices

# List services
docker service ls

# Scale service
docker service scale microservices_user-service=3

# Remove stack
docker stack rm microservices
```

### Kubernetes

For Kubernetes deployment, you'll need to:
1. Create Kubernetes manifests (Deployments, Services, ConfigMaps)
2. Use Helm charts for easier management
3. Configure Ingress for external access
4. Set up persistent volumes for databases

See `KUBERNETES_DEPLOYMENT_GUIDE.md` (to be created) for details.

## Performance Optimization

### Multi-stage Builds

Our Dockerfiles use multi-stage builds:
- Stage 1: Build with Maven (large image)
- Stage 2: Run with JRE only (small image)

This reduces final image size by ~60%.

### Image Size Comparison

```
maven:3.8.5-openjdk-17    ~700MB
openjdk:17-jdk-slim       ~400MB
Final service image       ~450MB (with app)
```

### Build Cache

```bash
# Use build cache for faster builds
docker-compose build --parallel

# Clear build cache
docker builder prune
```

## Monitoring

### Container Health

```bash
# Check container health
docker-compose ps

# View health check logs
docker inspect --format='{{json .State.Health}}' auth-service
```

### Resource Usage

```bash
# View resource usage
docker stats

# View specific container
docker stats auth-service
```

## Backup and Restore

### Database Backup

```bash
# Backup MySQL
docker-compose exec mysql mysqldump -u root -proot --all-databases > backup.sql

# Backup PostgreSQL
docker-compose exec postgres pg_dumpall -U postgres > backup.sql
```

### Database Restore

```bash
# Restore MySQL
docker-compose exec -T mysql mysql -u root -proot < backup.sql

# Restore PostgreSQL
docker-compose exec -T postgres psql -U postgres < backup.sql
```

## Security Considerations

### Production Recommendations

1. **Change default passwords**
   ```yaml
   environment:
     MYSQL_ROOT_PASSWORD: ${MYSQL_PASSWORD}
     POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
   ```

2. **Use secrets management**
   ```yaml
   secrets:
     db_password:
       external: true
   ```

3. **Limit exposed ports**
   ```yaml
   # Don't expose database ports in production
   # ports:
   #   - "3306:3306"
   ```

4. **Use private registry**
   ```bash
   docker tag auth-service:latest registry.example.com/auth-service:latest
   docker push registry.example.com/auth-service:latest
   ```

5. **Enable TLS/SSL**
   - Use HTTPS for all external communication
   - Configure SSL certificates in API Gateway

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Build and Deploy

on:
  push:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Build images
        run: docker-compose build
      
      - name: Push to registry
        run: |
          docker-compose push
      
      - name: Deploy
        run: |
          docker-compose up -d
```

## Complete Deployment Checklist

- [ ] Install Docker and Docker Compose
- [ ] Clone repository
- [ ] Review and update environment variables
- [ ] Build images: `docker-compose build`
- [ ] Start services: `docker-compose up -d`
- [ ] Wait for services to start (~3 minutes)
- [ ] Check Eureka dashboard (http://localhost:8761)
- [ ] Verify all services are registered
- [ ] Test API endpoints
- [ ] Check logs for errors
- [ ] Set up monitoring (optional)
- [ ] Configure backups (production)

## Support

For issues:
1. Check logs: `docker-compose logs -f`
2. Check service health: `docker-compose ps`
3. Restart services: `docker-compose restart`
4. Clean start: `docker-compose down -v && docker-compose up -d --build`

## Next Steps

- [ ] Add ELK Stack to docker-compose.yml
- [ ] Create Kubernetes manifests
- [ ] Set up CI/CD pipeline
- [ ] Configure monitoring with Prometheus
- [ ] Add distributed tracing with Zipkin
- [ ] Implement blue-green deployment
