# Microservices Project - Complete API Documentation

## Architecture Overview

This is a complete microservices architecture with the following services:

```
┌─────────────────────────────────────────────────────────────────┐
│                         API Gateway                              │
│                      (Port: 8084)                                │
│              - Routes all requests                               │
│              - JWT Authentication                                │
└─────────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
┌───────▼────────┐   ┌────────▼────────┐   ┌───────▼────────┐
│  AuthService   │   │  UserService    │   │  HotelService  │
│  (Port: 8086)  │   │  (Port: 8081)   │   │  (Port: 8082)  │
│  - Register    │   │  - User CRUD    │   │  - Hotel CRUD  │
│  - Login       │   │  - Get Ratings  │   │                │
│  - Validate    │   │  - Get Hotels   │   │                │
└────────────────┘   └─────────────────┘   └────────────────┘
                              │
                     ┌────────▼────────┐
                     │  RatingService  │
                     │  (Port: 8083)   │
                     │  - Rating CRUD  │
                     └─────────────────┘
                              │
                     ┌────────▼────────┐
                     │ Service Registry│
                     │  (Port: 8761)   │
                     │  - Eureka       │
                     └─────────────────┘
```

## Services & Ports

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| Service Registry (Eureka) | 8761 | - | Service Discovery |
| API Gateway | 8084 | - | Entry point, routing, authentication |
| AuthService | 8086 | auth_service_db (MySQL) | User authentication & JWT |
| UserService | 8081 | microservices (MySQL) | User management |
| HotelService | 8082 | microservice (PostgreSQL) | Hotel management |
| RatingService | 8083 | rating_service (MySQL) | Rating management |

## Database Architecture

Each microservice has its own database (Database per Service pattern):

- **AuthService**: `auth_service_db` - Stores user credentials (hashed passwords)
- **UserService**: `microservices` - Stores user profile data (NO passwords)
- **HotelService**: `microservice` - Stores hotel information
- **RatingService**: `rating_service` - Stores ratings

## Complete API Flow

### Step 1: Register a New User

**Endpoint**: `POST http://localhost:8086/auth/register`

**Request Body**:
```json
{
  "username": "john_doe",
  "password": "password123",
  "email": "john@example.com",
  "name": "John Doe",
  "about": "Software Developer"
}
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "userId": "b52060ac-a385-4d83-be0b-990480cf912c",
  "username": "john_doe",
  "email": "john@example.com",
  "role": "USER",
  "message": "User registered successfully"
}
```

**What Happens**:
1. User created in AuthService database with hashed password
2. JWT token generated
3. User automatically synced to UserService with same userId (NO password)

---

### Step 2: Login (if already registered)

**Endpoint**: `POST http://localhost:8086/auth/login`

**Request Body**:
```json
{
  "username": "john_doe",
  "password": "password123"
}
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "userId": "b52060ac-a385-4d83-be0b-990480cf912c",
  "username": "john_doe",
  "email": "john@example.com",
  "role": "USER",
  "message": "Login successful"
}
```

---

### Step 3: Create a Hotel

**Endpoint**: `POST http://localhost:8082/hotels`

**Headers**:
```
Authorization: Bearer <your-jwt-token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "name": "Grand Hotel",
  "location": "Mumbai",
  "about": "Luxury 5-star hotel"
}
```

**Response**:
```json
{
  "id": "00b75c43-1669-4da6-a017-b336c82e02ff",
  "name": "Grand Hotel",
  "location": "Mumbai",
  "about": "Luxury 5-star hotel"
}
```

---

### Step 4: Create a Rating

**Endpoint**: `POST http://localhost:8083/ratings`

**Headers**:
```
Authorization: Bearer <your-jwt-token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "userId": "b52060ac-a385-4d83-be0b-990480cf912c",
  "hotelId": "00b75c43-1669-4da6-a017-b336c82e02ff",
  "rating": 5,
  "feedback": "Excellent hotel! Great service."
}
```

**Response**:
```json
{
  "ratingId": "25a93e0f-77d3-461a-9f77-7f28e197d8e2",
  "userId": "b52060ac-a385-4d83-be0b-990480cf912c",
  "hotelId": "00b75c43-1669-4da6-a017-b336c82e02ff",
  "rating": "5",
  "feedback": "Excellent hotel! Great service."
}
```

---

### Step 5: Get User with All Ratings & Hotels

**Endpoint**: `GET http://localhost:8081/users/{userId}`

**Headers**:
```
Authorization: Bearer <your-jwt-token>
```

**Response**:
```json
{
  "userId": "b52060ac-a385-4d83-be0b-990480cf912c",
  "name": "John Doe",
  "email": "john@example.com",
  "about": "Software Developer",
  "ratings": [
    {
      "ratingId": "25a93e0f-77d3-461a-9f77-7f28e197d8e2",
      "userId": "b52060ac-a385-4d83-be0b-990480cf912c",
      "hotelId": "00b75c43-1669-4da6-a017-b336c82e02ff",
      "rating": "5",
      "feedback": "Excellent hotel! Great service.",
      "hotel": {
        "id": "00b75c43-1669-4da6-a017-b336c82e02ff",
        "name": "Grand Hotel",
        "location": "Mumbai",
        "about": "Luxury 5-star hotel"
      }
    }
  ]
}
```

**What Happens**:
1. UserService fetches user from database
2. Calls RatingService to get all ratings for this user
3. For each rating, calls HotelService to get hotel details
4. Returns complete aggregated data

---

## All Available Endpoints

### AuthService (Port: 8086)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/register` | Register new user | No |
| POST | `/auth/login` | Login user | No |
| POST | `/auth/validate` | Validate JWT token | No |

### UserService (Port: 8081)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/users` | Create user | Yes |
| GET | `/users/{userId}` | Get user with ratings & hotels | Yes |
| GET | `/users` | Get all users | Yes |

### UserService - Resilient Endpoints (Port: 8081)

| Method | Endpoint | Description | Features |
|--------|----------|-------------|----------|
| GET | `/resilient/users/{userId}` | Get user (with resilience) | Circuit Breaker, Retry, Rate Limiter |
| GET | `/resilient/users` | Get all users (with resilience) | Circuit Breaker, Retry, Rate Limiter |

**Resilience Configuration**:
- Circuit Breaker: Opens after 50% failure rate (min 5 calls)
- Retry: 3 attempts with 2s delay
- Rate Limiter: 2 requests per 4 seconds

### HotelService (Port: 8082)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/hotels` | Create hotel | Yes |
| GET | `/hotels/{hotelId}` | Get hotel by ID | Yes |
| GET | `/hotels` | Get all hotels | Yes |
| PUT | `/hotels/{hotelId}` | Update hotel | Yes |
| DELETE | `/hotels/{hotelId}` | Delete hotel | Yes |

### RatingService (Port: 8083)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/ratings` | Create rating | Yes |
| GET | `/ratings/{ratingId}` | Get rating by ID | Yes |
| GET | `/ratings` | Get all ratings | Yes |
| GET | `/ratings/users/{userId}` | Get ratings by user | Yes |
| GET | `/ratings/hotels/{hotelId}` | Get ratings by hotel | Yes |
| PUT | `/ratings/{ratingId}` | Update rating | Yes |
| DELETE | `/ratings/{ratingId}` | Delete rating | Yes |

---

## Testing with PowerShell

### 1. Register User
```powershell
$registerBody = @{
    username = "test_user"
    password = "password123"
    email = "test@example.com"
    name = "Test User"
    about = "Testing"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8086/auth/register" -Method POST -ContentType "application/json" -Body $registerBody
$token = $response.token
$userId = $response.userId

Write-Host "Token: $token"
Write-Host "UserId: $userId"
```

### 2. Create Hotel
```powershell
$hotelBody = @{
    name = "Test Hotel"
    location = "Mumbai"
    about = "Great hotel"
} | ConvertTo-Json

$hotel = Invoke-RestMethod -Uri "http://localhost:8082/hotels" -Method POST -ContentType "application/json" -Headers @{Authorization="Bearer $token"} -Body $hotelBody
$hotelId = $hotel.id

Write-Host "Hotel ID: $hotelId"
```

### 3. Create Rating
```powershell
$ratingBody = @{
    userId = $userId
    hotelId = $hotelId
    rating = 5
    feedback = "Excellent!"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8083/ratings" -Method POST -ContentType "application/json" -Headers @{Authorization="Bearer $token"} -Body $ratingBody
```

### 4. Get Complete User Data
```powershell
$userData = Invoke-RestMethod -Uri "http://localhost:8081/users/$userId" -Method GET -Headers @{Authorization="Bearer $token"}
$userData | ConvertTo-Json -Depth 10
```

---

## Key Features Implemented

### 1. Microservices Architecture
- Service Registry (Eureka) for service discovery
- API Gateway for routing and authentication
- Independent services with separate databases

### 2. Security
- JWT-based authentication
- Password hashing with BCrypt
- Token validation at API Gateway
- Passwords stored ONLY in AuthService

### 3. Resilience Patterns (Resilience4j)
- Circuit Breaker: Prevents cascading failures
- Retry: Automatic retry on transient failures
- Rate Limiter: Prevents system overload

### 4. Inter-Service Communication
- Feign Client for declarative REST calls
- Load-balanced RestTemplate
- Service-to-service communication via service names

### 5. Exception Handling
- Global exception handlers in all services
- Consistent error response format
- Proper HTTP status codes

### 6. Observability
- Centralized logging with ELK Stack
- Request/Response logging
- Structured JSON logs

---

## Service URLs

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8084
- **AuthService**: http://localhost:8086
- **UserService**: http://localhost:8081
- **HotelService**: http://localhost:8082
- **RatingService**: http://localhost:8083
- **Kibana (Logs)**: http://localhost:5601

---

## Common Issues & Solutions

### 1. Service Not Registered in Eureka
- Wait 30 seconds after starting service
- Check Eureka dashboard at http://localhost:8761

### 2. Authentication Failed
- Ensure token is included in Authorization header
- Token format: `Bearer <token>`
- Token expires after 10 hours

### 3. Service Unavailable
- Check if all services are running
- Verify service registration in Eureka
- Check service logs for errors

### 4. Circuit Breaker Open
- Wait 6 seconds for circuit to half-open
- Reduce request rate
- Check downstream service health

---

## Testing Checklist

- [ ] Start Service Registry (Eureka)
- [ ] Start all microservices
- [ ] Verify all services in Eureka dashboard
- [ ] Register a new user
- [ ] Login with credentials
- [ ] Create a hotel
- [ ] Create a rating
- [ ] Fetch user with complete data
- [ ] Test resilient endpoints
- [ ] Check logs in Kibana

---

## Next Steps for Enhancement

1. Add API documentation with Swagger/OpenAPI
2. Implement distributed tracing with Zipkin
3. Add metrics with Prometheus & Grafana
4. Implement API rate limiting at gateway
5. Add caching with Redis
6. Implement event-driven architecture with Kafka
7. Add integration tests
8. Implement CQRS pattern
9. Add health checks and monitoring
10. Deploy to Kubernetes
