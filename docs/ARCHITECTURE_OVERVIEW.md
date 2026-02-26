# Microservices Architecture - Complete Overview

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              CLIENT                                      │
│                    (Browser / Mobile / Postman)                          │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                                 │ HTTP Requests
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          API GATEWAY (8084)                              │
│  ┌────────────────────────────────────────────────────────────────┐    │
│  │  • Routes requests to microservices                             │    │
│  │  • JWT Token validation via AuthService                         │    │
│  │  • Load balancing                                               │    │
│  │  • Service discovery integration                                │    │
│  └────────────────────────────────────────────────────────────────┘    │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                ┌────────────────┼────────────────┐
                │                │                │
                ▼                ▼                ▼
┌───────────────────┐  ┌───────────────────┐  ┌───────────────────┐
│   AuthService     │  │   UserService     │  │   HotelService    │
│   (Port: 8086)    │  │   (Port: 8081)    │  │   (Port: 8082)    │
│                   │  │                   │  │                   │
│  • Register       │  │  • User CRUD      │  │  • Hotel CRUD     │
│  • Login          │  │  • Fetch Ratings  │  │  • Hotel Info     │
│  • JWT Generate   │  │  • Fetch Hotels   │  │                   │
│  • JWT Validate   │  │  • Resilience4j   │  │                   │
└─────────┬─────────┘  └─────────┬─────────┘  └───────────────────┘
          │                      │
          │                      │ Feign Client
          │                      ▼
          │            ┌───────────────────┐
          │            │  RatingService    │
          │            │  (Port: 8083)     │
          │            │                   │
          │            │  • Rating CRUD    │
          │            │  • User Ratings   │
          │            │  • Hotel Ratings  │
          │            └─────────┬─────────┘
          │                      │
          │                      │
┌─────────▼──────────────────────▼─────────────────────────────────┐
│              SERVICE REGISTRY (Eureka - Port: 8761)               │
│  • Service Discovery                                              │
│  • Health Monitoring                                              │
│  • Load Balancing                                                 │
└───────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                         DATABASE LAYER                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐│
│  │ auth_service │  │ microservices│  │ microservice │  │rating_service││
│  │     _db      │  │   (MySQL)    │  │ (PostgreSQL) │  │   (MySQL)    ││
│  │   (MySQL)    │  │              │  │              │  │              ││
│  │              │  │              │  │              │  │              ││
│  │ AuthService  │  │ UserService  │  │ HotelService │  │RatingService ││
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘│
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                    OBSERVABILITY LAYER (ELK Stack)                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                  │
│  │Elasticsearch │  │   Logstash   │  │    Kibana    │                  │
│  │  (Port:9200) │  │ (Port: 5000) │  │ (Port: 5601) │                  │
│  │              │  │              │  │              │                  │
│  │ Log Storage  │  │ Log Pipeline │  │ Visualization│                  │
│  └──────────────┘  └──────────────┘  └──────────────┘                  │
└─────────────────────────────────────────────────────────────────────────┘
```

## Data Flow - Complete User Journey

### 1. User Registration Flow
```
Client → API Gateway → AuthService
                         │
                         ├─ Save to auth_service_db (with password)
                         │
                         └─ Feign Client → UserService
                                            │
                                            └─ Save to microservices (NO password)
```

### 2. User Login Flow
```
Client → API Gateway → AuthService
                         │
                         ├─ Verify credentials
                         │
                         └─ Generate JWT Token → Return to Client
```

### 3. Get User with Ratings & Hotels Flow
```
Client → API Gateway → UserService
                         │
                         ├─ Fetch user from database
                         │
                         ├─ RestTemplate → RatingService
                         │                   │
                         │                   └─ Get all ratings for user
                         │
                         └─ For each rating:
                             │
                             └─ Feign Client → HotelService
                                                 │
                                                 └─ Get hotel details
                         
                         Return: User + Ratings + Hotels (aggregated)
```

### 4. Create Rating Flow
```
Client → API Gateway → RatingService
                         │
                         └─ Save rating to rating_service database
```

## Key Design Patterns Implemented

### 1. Microservices Patterns

#### API Gateway Pattern
- Single entry point for all clients
- Request routing to appropriate services
- Authentication and authorization
- Load balancing

#### Service Registry Pattern (Eureka)
- Dynamic service discovery
- Health checking
- Load balancing
- Fault tolerance

#### Database per Service Pattern
- Each service has its own database
- Data isolation and independence
- Technology diversity (MySQL + PostgreSQL)

### 2. Resilience Patterns (Resilience4j)

#### Circuit Breaker
```
Configuration:
- Failure Rate Threshold: 50%
- Minimum Calls: 5
- Wait Duration in Open State: 6 seconds
- Sliding Window Size: 10

States:
CLOSED → OPEN → HALF_OPEN → CLOSED
```

#### Retry Pattern
```
Configuration:
- Max Attempts: 3
- Wait Duration: 2 seconds
- Exponential Backoff: Enabled
```

#### Rate Limiter
```
Configuration:
- Limit: 2 requests
- Refresh Period: 4 seconds
- Timeout: 0 seconds
```

### 3. Security Patterns

#### JWT Authentication
```
Flow:
1. User logs in with credentials
2. AuthService validates and generates JWT
3. Client includes JWT in Authorization header
4. API Gateway validates JWT with AuthService
5. Request forwarded to target service
```

#### Password Security
- BCrypt hashing (strength: 10)
- Passwords stored ONLY in AuthService
- UserService has NO access to passwords

### 4. Communication Patterns

#### Synchronous Communication
- REST APIs for client-service communication
- Feign Client for service-to-service calls
- RestTemplate with load balancing

#### Service Discovery
- Services register with Eureka on startup
- Services discover each other via service names
- No hardcoded URLs

## Technology Stack

### Backend Framework
- **Spring Boot 3.2.5**: Core framework
- **Spring Cloud 2023.0.1**: Microservices infrastructure

### Service Discovery
- **Netflix Eureka**: Service registry and discovery

### API Gateway
- **Spring Cloud Gateway**: Reactive gateway with WebFlux

### Security
- **Spring Security**: Authentication and authorization
- **JWT (jjwt 0.12.3)**: Token-based authentication
- **BCrypt**: Password hashing

### Resilience
- **Resilience4j**: Circuit breaker, retry, rate limiter

### Inter-Service Communication
- **OpenFeign**: Declarative REST client
- **RestTemplate**: HTTP client with load balancing

### Databases
- **MySQL**: AuthService, UserService, RatingService
- **PostgreSQL**: HotelService
- **Spring Data JPA**: ORM and data access

### Observability
- **Elasticsearch**: Log storage and search
- **Logstash**: Log aggregation and processing
- **Kibana**: Log visualization and analysis
- **Logback**: Logging framework
- **Logstash Encoder**: JSON log formatting

### Build Tool
- **Maven**: Dependency management and build

## Service Details

### AuthService (Port: 8086)
**Purpose**: Centralized authentication and authorization

**Responsibilities**:
- User registration
- User login
- JWT token generation
- JWT token validation
- Password management (BCrypt)
- User synchronization to UserService

**Database**: `auth_service_db` (MySQL)

**Key Endpoints**:
- POST `/auth/register` - Register new user
- POST `/auth/login` - Login user
- POST `/auth/validate` - Validate JWT token

### UserService (Port: 8081)
**Purpose**: User profile management and data aggregation

**Responsibilities**:
- User CRUD operations
- Fetch user ratings from RatingService
- Fetch hotel details from HotelService
- Aggregate user data with ratings and hotels
- Resilience patterns implementation

**Database**: `microservices` (MySQL)

**Key Endpoints**:
- POST `/users` - Create user
- GET `/users/{userId}` - Get user with ratings & hotels
- GET `/users` - Get all users
- GET `/resilient/users/{userId}` - Resilient endpoint
- GET `/resilient/users` - Resilient endpoint

**External Dependencies**:
- RatingService (via RestTemplate)
- HotelService (via Feign Client)

### HotelService (Port: 8082)
**Purpose**: Hotel information management

**Responsibilities**:
- Hotel CRUD operations
- Hotel information storage
- Hotel search and retrieval

**Database**: `microservice` (PostgreSQL)

**Key Endpoints**:
- POST `/hotels` - Create hotel
- GET `/hotels/{hotelId}` - Get hotel by ID
- GET `/hotels` - Get all hotels
- PUT `/hotels/{hotelId}` - Update hotel
- DELETE `/hotels/{hotelId}` - Delete hotel

### RatingService (Port: 8083)
**Purpose**: Rating and review management

**Responsibilities**:
- Rating CRUD operations
- User ratings retrieval
- Hotel ratings retrieval
- Rating statistics

**Database**: `rating_service` (MySQL)

**Key Endpoints**:
- POST `/ratings` - Create rating
- GET `/ratings/{ratingId}` - Get rating by ID
- GET `/ratings` - Get all ratings
- GET `/ratings/users/{userId}` - Get ratings by user
- GET `/ratings/hotels/{hotelId}` - Get ratings by hotel
- PUT `/ratings/{ratingId}` - Update rating
- DELETE `/ratings/{ratingId}` - Delete rating

### API Gateway (Port: 8084)
**Purpose**: Single entry point and request routing

**Responsibilities**:
- Route requests to appropriate services
- JWT token validation
- Load balancing
- Service discovery integration

**Routes**:
- `/auth/**` → AuthService
- `/users/**` → UserService
- `/resilient/**` → UserService
- `/hotels/**` → HotelService
- `/ratings/**` → RatingService

### Service Registry (Port: 8761)
**Purpose**: Service discovery and health monitoring

**Responsibilities**:
- Service registration
- Service discovery
- Health checking
- Load balancing information

**Dashboard**: http://localhost:8761

## Exception Handling

All services implement global exception handling with consistent error responses:

```json
{
  "message": "Error description",
  "success": false,
  "status": "HTTP_STATUS"
}
```

**HTTP Status Codes**:
- 200 OK - Success
- 201 CREATED - Resource created
- 400 BAD_REQUEST - Validation error
- 401 UNAUTHORIZED - Authentication failed
- 404 NOT_FOUND - Resource not found
- 409 CONFLICT - Duplicate resource
- 429 TOO_MANY_REQUESTS - Rate limit exceeded
- 500 INTERNAL_SERVER_ERROR - Server error
- 503 SERVICE_UNAVAILABLE - Service down

## Logging Architecture

### Log Levels
- **ERROR**: System errors, exceptions
- **WARN**: Warning conditions, fallbacks
- **INFO**: Important business events
- **DEBUG**: Detailed debugging information

### Log Format (JSON)
```json
{
  "@timestamp": "2024-02-26T10:30:00.000Z",
  "level": "INFO",
  "service": "UserService",
  "logger": "com.vivek.user.service",
  "message": "User fetched successfully",
  "userId": "123-456",
  "thread": "http-nio-8081-exec-1"
}
```

### Log Aggregation Flow
```
Services → Logback → Logstash → Elasticsearch → Kibana
```

## Scalability Considerations

### Horizontal Scaling
- Each service can be scaled independently
- Load balancing via Eureka and Ribbon
- Stateless services (JWT tokens)

### Database Scaling
- Separate databases per service
- Can use different database technologies
- Read replicas for read-heavy services

### Caching (Future Enhancement)
- Redis for frequently accessed data
- Cache user profiles
- Cache hotel information

## Security Features

### Authentication
- JWT-based token authentication
- Token expiration (10 hours)
- Secure password storage (BCrypt)

### Authorization
- Role-based access control (USER role)
- Token validation at API Gateway
- Service-level security

### Data Protection
- Passwords never exposed in APIs
- Passwords stored only in AuthService
- Encrypted database connections

## Monitoring & Observability

### Logging
- Centralized logging with ELK Stack
- Structured JSON logs
- Request/Response logging
- Error tracking

### Health Checks
- Actuator endpoints for health monitoring
- Eureka health checks
- Database connection monitoring

### Metrics (Future Enhancement)
- Prometheus for metrics collection
- Grafana for visualization
- Custom business metrics

## Deployment Architecture

### Development
- Local development with multiple ports
- Embedded databases
- Console logging

### Production (Recommended)
- Docker containers for each service
- Kubernetes for orchestration
- External databases (RDS, Cloud SQL)
- Centralized logging (ELK Stack)
- API Gateway with SSL/TLS
- Load balancers
- Auto-scaling groups

## Future Enhancements

1. **Distributed Tracing**: Zipkin/Jaeger for request tracing
2. **API Documentation**: Swagger/OpenAPI integration
3. **Caching**: Redis for performance optimization
4. **Message Queue**: Kafka for async communication
5. **CQRS**: Command Query Responsibility Segregation
6. **Event Sourcing**: Event-driven architecture
7. **API Versioning**: Support multiple API versions
8. **Rate Limiting**: Advanced rate limiting at gateway
9. **Monitoring**: Prometheus + Grafana
10. **CI/CD**: Jenkins/GitHub Actions pipeline
11. **Container Orchestration**: Kubernetes deployment
12. **Service Mesh**: Istio for advanced traffic management

## Best Practices Followed

1. **Single Responsibility**: Each service has one responsibility
2. **Database per Service**: Data isolation
3. **API Gateway**: Single entry point
4. **Service Discovery**: Dynamic service location
5. **Circuit Breaker**: Fault tolerance
6. **Centralized Logging**: Unified log management
7. **Exception Handling**: Consistent error responses
8. **Security**: JWT authentication, password hashing
9. **Documentation**: Comprehensive API documentation
10. **Testing**: Postman collection for testing
