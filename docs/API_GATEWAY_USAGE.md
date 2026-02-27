# 🚪 API Gateway Usage Guide

## Overview

The API Gateway is the **single entry point** for all client requests in production. This document explains how to properly access the microservices.

---

## 🏗️ Architecture

### Production Architecture (Docker/Kubernetes)
```
┌─────────────────────────────────────────────────┐
│                   Internet                       │
└────────────────────┬────────────────────────────┘
                     │
                     ▼
            ┌────────────────┐
            │  API Gateway   │ ← Only Public Entry Point
            │  Port: 8084    │
            └────────┬───────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
        ▼            ▼            ▼
   ┌─────────┐  ┌─────────┐  ┌─────────┐
   │  Auth   │  │  User   │  │  Hotel  │
   │ Service │  │ Service │  │ Service │
   │  :8086  │  │  :8081  │  │  :8082  │
   └─────────┘  └─────────┘  └─────────┘
        │            │            │
        └────────────┼────────────┘
                     │
                     ▼
              ┌──────────────┐
              │    Eureka    │
              │  Port: 8761  │
              └──────────────┘
```

**Key Points:**
- ✅ Clients access only API Gateway (port 8084)
- ✅ Internal services NOT accessible from outside
- ✅ Services communicate within private network
- ✅ Eureka accessible for monitoring (with authentication)

---

## 🔐 Security Benefits

### Why Use API Gateway?

1. **Single Entry Point**
   - Centralized security enforcement
   - Easier to monitor and log all requests
   - Single point for rate limiting and throttling

2. **Service Isolation**
   - Internal services hidden from public internet
   - Prevents direct attacks on individual services
   - Network segmentation

3. **Authentication & Authorization**
   - JWT validation at gateway level
   - No need to implement auth in every service
   - Centralized token management

4. **Load Balancing**
   - Gateway distributes requests across service instances
   - Automatic failover
   - Service discovery integration

---

## 📍 Correct API Endpoints

### ✅ CORRECT - Through API Gateway

**Base URL:** `http://localhost:8084` (or your domain in production)

#### Authentication Endpoints (Public)
```bash
# Register
POST http://localhost:8084/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "password": "SecurePass123!",
  "email": "john@example.com",
  "name": "John Doe",
  "about": "Software Developer"
}

# Login
POST http://localhost:8084/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "SecurePass123!"
}

# Response includes JWT token
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "userId": "uuid",
  "username": "john_doe",
  "role": "USER"
}
```

#### User Endpoints (Requires JWT)
```bash
# Get user by ID
GET http://localhost:8084/users/{userId}
Authorization: Bearer {your-jwt-token}

# Get all users
GET http://localhost:8084/users
Authorization: Bearer {your-jwt-token}

# Create user
POST http://localhost:8084/users
Authorization: Bearer {your-jwt-token}
Content-Type: application/json
```

#### Hotel Endpoints (Requires JWT)
```bash
# Get all hotels
GET http://localhost:8084/hotels
Authorization: Bearer {your-jwt-token}

# Get hotel by ID
GET http://localhost:8084/hotels/{hotelId}
Authorization: Bearer {your-jwt-token}

# Create hotel
POST http://localhost:8084/hotels
Authorization: Bearer {your-jwt-token}
Content-Type: application/json
```

#### Rating Endpoints (Requires JWT)
```bash
# Get all ratings
GET http://localhost:8084/ratings
Authorization: Bearer {your-jwt-token}

# Create rating
POST http://localhost:8084/ratings
Authorization: Bearer {your-jwt-token}
Content-Type: application/json
```

---

### ❌ INCORRECT - Direct Service Access

**These URLs should NOT be used by clients in production:**

```bash
# ❌ DON'T USE - Direct AuthService access
http://localhost:8086/auth/register
http://localhost:8086/auth/login

# ❌ DON'T USE - Direct UserService access
http://localhost:8081/users

# ❌ DON'T USE - Direct HotelService access
http://localhost:8082/hotels

# ❌ DON'T USE - Direct RatingService access
http://localhost:8083/ratings
```

**Why?**
- Bypasses API Gateway security
- No centralized logging
- No rate limiting
- Not available in production (Docker/Kubernetes)

---

## 🔧 Environment-Specific Behavior

### Local Development (Eclipse/IDE)

**Services Running:**
- API Gateway: `http://localhost:8084` ✅ Use this
- AuthService: `http://localhost:8086` ⚠️ Available but don't use
- UserService: `http://localhost:8081` ⚠️ Available but don't use
- HotelService: `http://localhost:8082` ⚠️ Available but don't use
- RatingService: `http://localhost:8083` ⚠️ Available but don't use

**Note:** Services are accessible on localhost for debugging purposes only.

**Best Practice:** Always use API Gateway (8084) even in local development to match production behavior.

---

### Docker Deployment

**Services Running:**
- API Gateway: `http://localhost:8084` ✅ Publicly accessible
- Eureka: `http://localhost:8761` ✅ Publicly accessible (with auth)
- AuthService: Internal only ✅ Not accessible from host
- UserService: Internal only ✅ Not accessible from host
- HotelService: Internal only ✅ Not accessible from host
- RatingService: Internal only ✅ Not accessible from host

**Configuration:**
```yaml
# docker-compose.yml
api-gateway:
  ports:
    - "8084:8084"  # Publicly exposed

auth-service:
  expose:
    - "8086"  # Internal only, not accessible from host
```

---

### Production (Kubernetes)

**Services:**
- API Gateway: LoadBalancer/Ingress (public)
- Internal Services: ClusterIP (private)
- Eureka: ClusterIP (private, accessed via kubectl port-forward)

**Example Kubernetes Service:**
```yaml
# API Gateway - Public
apiVersion: v1
kind: Service
metadata:
  name: api-gateway
spec:
  type: LoadBalancer
  ports:
    - port: 80
      targetPort: 8084

# AuthService - Private
apiVersion: v1
kind: Service
metadata:
  name: auth-service
spec:
  type: ClusterIP  # Internal only
  ports:
    - port: 8086
```

---

## 🧪 Testing Examples

### Using cURL

```bash
# 1. Register a new user
curl -X POST http://localhost:8084/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "test123",
    "email": "test@example.com",
    "name": "Test User",
    "about": "Testing"
  }'

# 2. Login and save token
TOKEN=$(curl -X POST http://localhost:8084/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}' \
  | jq -r '.token')

# 3. Access protected endpoint
curl http://localhost:8084/users \
  -H "Authorization: Bearer $TOKEN"
```

### Using PowerShell

```powershell
# 1. Register
$body = @{
    username = "testuser"
    password = "test123"
    email = "test@example.com"
    name = "Test User"
    about = "Testing"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8084/auth/register" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body

# 2. Login and get token
$loginBody = @{
    username = "testuser"
    password = "test123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:8084/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $loginBody

$token = $loginResponse.token

# 3. Access protected endpoint
$headers = @{
    "Authorization" = "Bearer $token"
}

Invoke-RestMethod -Uri "http://localhost:8084/users" `
    -Method GET `
    -Headers $headers
```

### Using Postman

1. **Import Collection:** `Microservices_Postman_Collection.json`
2. **Set Base URL:** `http://localhost:8084`
3. **Register/Login:** Get JWT token
4. **Set Token:** In Postman environment or Authorization tab
5. **Test Endpoints:** All requests go through Gateway

---

## 🚨 Common Mistakes

### ❌ Mistake 1: Using Direct Service URLs
```bash
# Wrong
POST http://localhost:8086/auth/login

# Correct
POST http://localhost:8084/auth/login
```

### ❌ Mistake 2: Forgetting Authorization Header
```bash
# Wrong
GET http://localhost:8084/users

# Correct
GET http://localhost:8084/users
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### ❌ Mistake 3: Using Wrong Port
```bash
# Wrong
POST http://localhost:8081/users

# Correct
POST http://localhost:8084/users
```

---

## 📊 API Gateway Routes

The API Gateway routes requests based on path:

| Path Pattern | Target Service | Authentication Required |
|-------------|----------------|------------------------|
| `/auth/**` | AuthService | ❌ No (public) |
| `/users/**` | UserService | ✅ Yes (JWT) |
| `/hotels/**` | HotelService | ✅ Yes (JWT) |
| `/ratings/**` | RatingService | ✅ Yes (JWT) |
| `/resilient/users/**` | UserService | ✅ Yes (JWT) |

**Configuration:** `ApiGateway/src/main/resources/application.yml`

---

## 🔍 Monitoring & Debugging

### Check Service Registration
```bash
# Access Eureka Dashboard
http://localhost:8761

# Login with credentials
Username: admin
Password: admin123
```

### View Gateway Routes
```bash
# If actuator is enabled
curl http://localhost:8084/actuator/gateway/routes
```

### Check Service Health
```bash
# Through Gateway
curl http://localhost:8084/auth/health
curl http://localhost:8084/users/actuator/health
```

---

## 📚 Additional Resources

- [API Documentation](API_DOCUMENTATION.md)
- [Security Setup](SECURITY_SETUP.md)
- [Docker Quick Start](DOCKER_QUICK_START.md)
- [Architecture Overview](ARCHITECTURE_OVERVIEW.md)

---

## ✅ Best Practices Summary

1. ✅ **Always use API Gateway** (port 8084) for all client requests
2. ✅ **Never expose internal services** in production
3. ✅ **Include JWT token** in Authorization header for protected endpoints
4. ✅ **Use Docker Compose** for production-like testing
5. ✅ **Monitor via Eureka** dashboard for service health
6. ✅ **Test through Gateway** even in local development

---

**Last Updated:** February 27, 2026  
**Maintained By:** Vivek Bhosale
