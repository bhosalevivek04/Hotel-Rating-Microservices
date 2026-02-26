# Quick Start Guide - Microservices Project

## Prerequisites

- Java 17+
- MySQL
- PostgreSQL
- Maven
- Docker (optional, for ELK Stack)

## Step 1: Start Databases

### MySQL (for AuthService, UserService, RatingService)
```sql
CREATE DATABASE auth_service_db;
CREATE DATABASE microservices;
CREATE DATABASE rating_service;
```

### PostgreSQL (for HotelService)
```sql
CREATE DATABASE microservice;
```

## Step 2: Start Services (in this order)

### 1. Service Registry (Eureka)
```bash
cd ServiceRegistry
mvn spring-boot:run
```
Wait for: `Started Eureka Server` message
Access: http://localhost:8761

### 2. Config Server (if using)
```bash
cd ConfigServer
mvn spring-boot:run
```

### 3. AuthService
```bash
cd AuthService
mvn spring-boot:run
```
Wait for: `Started AuthServiceApplication` message

### 4. UserService
```bash
cd UserService
mvn spring-boot:run
```
Wait for: `Started UserServiceApplication` message

### 5. HotelService
```bash
cd HotelService
mvn spring-boot:run
```
Wait for: `Started HotelServiceApplication` message

### 6. RatingService
```bash
cd RatingService
mvn spring-boot:run
```
Wait for: `Started RatingServiceApplication` message

### 7. API Gateway
```bash
cd ApiGateway
mvn spring-boot:run
```
Wait for: `Started ApiGatewayApplication` message

## Step 3: Verify All Services

Open Eureka Dashboard: http://localhost:8761

You should see all services registered:
- AUTHSERVICE
- USERSERVICE
- HOTELSERVICE
- RATINGSERVICE
- APIGATEWAY

## Step 4: Test the Complete Flow

### Using PowerShell

#### 1. Register a User
```powershell
$registerBody = @{
    username = "john_doe"
    password = "password123"
    email = "john@example.com"
    name = "John Doe"
    about = "Software Developer"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8086/auth/register" -Method POST -ContentType "application/json" -Body $registerBody

# Save token and userId
$token = $response.token
$userId = $response.userId

Write-Host "✓ User registered successfully"
Write-Host "Token: $token"
Write-Host "UserId: $userId"
```

#### 2. Create a Hotel
```powershell
$hotelBody = @{
    name = "Grand Hotel"
    location = "Mumbai"
    about = "Luxury 5-star hotel"
} | ConvertTo-Json

$hotel = Invoke-RestMethod -Uri "http://localhost:8082/hotels" -Method POST -ContentType "application/json" -Headers @{Authorization="Bearer $token"} -Body $hotelBody

$hotelId = $hotel.id
Write-Host "✓ Hotel created successfully"
Write-Host "Hotel ID: $hotelId"
```

#### 3. Create a Rating
```powershell
$ratingBody = @{
    userId = $userId
    hotelId = $hotelId
    rating = 5
    feedback = "Excellent hotel! Great service."
} | ConvertTo-Json

$rating = Invoke-RestMethod -Uri "http://localhost:8083/ratings" -Method POST -ContentType "application/json" -Headers @{Authorization="Bearer $token"} -Body $ratingBody

Write-Host "✓ Rating created successfully"
```

#### 4. Get Complete User Data
```powershell
$userData = Invoke-RestMethod -Uri "http://localhost:8081/users/$userId" -Method GET -Headers @{Authorization="Bearer $token"}

Write-Host "✓ User data retrieved successfully"
$userData | ConvertTo-Json -Depth 10
```

### Using Postman

1. Import the collection: `Microservices_Postman_Collection.json`
2. Run the "Complete Flow Test" folder
3. All steps will execute automatically with tests

### Using cURL (Linux/Mac)

#### 1. Register User
```bash
curl -X POST http://localhost:8086/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123",
    "email": "john@example.com",
    "name": "John Doe",
    "about": "Software Developer"
  }'
```

#### 2. Save the token and userId from response
```bash
TOKEN="<your-token-here>"
USER_ID="<your-user-id-here>"
```

#### 3. Create Hotel
```bash
curl -X POST http://localhost:8082/hotels \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Grand Hotel",
    "location": "Mumbai",
    "about": "Luxury 5-star hotel"
  }'
```

#### 4. Save hotel ID and create rating
```bash
HOTEL_ID="<your-hotel-id-here>"

curl -X POST http://localhost:8083/ratings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "userId": "'$USER_ID'",
    "hotelId": "'$HOTEL_ID'",
    "rating": 5,
    "feedback": "Excellent hotel!"
  }'
```

#### 5. Get complete user data
```bash
curl -X GET http://localhost:8081/users/$USER_ID \
  -H "Authorization: Bearer $TOKEN"
```

## Step 5: Start ELK Stack (Optional)

```bash
docker-compose -f docker-compose-elk.yml up -d
```

Wait for all services to be healthy:
```bash
docker-compose -f docker-compose-elk.yml ps
```

Access Kibana: http://localhost:5601

Create index pattern: `microservices-logs-*`

## Troubleshooting

### Service Not Starting

1. Check if port is already in use:
```bash
netstat -ano | findstr :<port>
```

2. Check database connection in `application.yml`

3. Check logs in console output

### Service Not Registered in Eureka

1. Wait 30 seconds after service starts
2. Check `application.yml` for correct Eureka URL
3. Verify Service Registry is running

### Authentication Failed

1. Ensure token is valid (not expired)
2. Check token format: `Bearer <token>`
3. Verify AuthService is running

### Database Connection Failed

1. Verify database is running
2. Check credentials in `application.yml`
3. Ensure database exists

## Service Health Checks

Check if services are running:

```powershell
# Service Registry
Invoke-WebRequest -Uri "http://localhost:8761" -Method GET

# AuthService
Invoke-WebRequest -Uri "http://localhost:8086/actuator/health" -Method GET

# UserService
Invoke-WebRequest -Uri "http://localhost:8081/actuator/health" -Method GET

# HotelService
Invoke-WebRequest -Uri "http://localhost:8082/actuator/health" -Method GET

# RatingService
Invoke-WebRequest -Uri "http://localhost:8083/actuator/health" -Method GET

# API Gateway
Invoke-WebRequest -Uri "http://localhost:8084/actuator/health" -Method GET
```

## Complete Test Script (PowerShell)

Save this as `test-microservices.ps1`:

```powershell
Write-Host "=== Microservices Complete Flow Test ===" -ForegroundColor Green

# 1. Register User
Write-Host "`n1. Registering user..." -ForegroundColor Yellow
$registerBody = @{
    username = "test_user_$(Get-Date -Format 'yyyyMMddHHmmss')"
    password = "password123"
    email = "test$(Get-Date -Format 'yyyyMMddHHmmss')@example.com"
    name = "Test User"
    about = "Testing complete flow"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8086/auth/register" -Method POST -ContentType "application/json" -Body $registerBody
    $token = $response.token
    $userId = $response.userId
    Write-Host "✓ User registered: $userId" -ForegroundColor Green
} catch {
    Write-Host "✗ Registration failed: $_" -ForegroundColor Red
    exit 1
}

# 2. Create Hotel
Write-Host "`n2. Creating hotel..." -ForegroundColor Yellow
$hotelBody = @{
    name = "Test Hotel $(Get-Date -Format 'HHmmss')"
    location = "Mumbai"
    about = "Test hotel"
} | ConvertTo-Json

try {
    $hotel = Invoke-RestMethod -Uri "http://localhost:8082/hotels" -Method POST -ContentType "application/json" -Headers @{Authorization="Bearer $token"} -Body $hotelBody
    $hotelId = $hotel.id
    Write-Host "✓ Hotel created: $hotelId" -ForegroundColor Green
} catch {
    Write-Host "✗ Hotel creation failed: $_" -ForegroundColor Red
    exit 1
}

# 3. Create Rating
Write-Host "`n3. Creating rating..." -ForegroundColor Yellow
$ratingBody = @{
    userId = $userId
    hotelId = $hotelId
    rating = 5
    feedback = "Excellent!"
} | ConvertTo-Json

try {
    $rating = Invoke-RestMethod -Uri "http://localhost:8083/ratings" -Method POST -ContentType "application/json" -Headers @{Authorization="Bearer $token"} -Body $ratingBody
    Write-Host "✓ Rating created" -ForegroundColor Green
} catch {
    Write-Host "✗ Rating creation failed: $_" -ForegroundColor Red
    exit 1
}

# 4. Get Complete User Data
Write-Host "`n4. Fetching complete user data..." -ForegroundColor Yellow
try {
    $userData = Invoke-RestMethod -Uri "http://localhost:8081/users/$userId" -Method GET -Headers @{Authorization="Bearer $token"}
    Write-Host "✓ User data retrieved successfully" -ForegroundColor Green
    Write-Host "`nComplete User Data:" -ForegroundColor Cyan
    $userData | ConvertTo-Json -Depth 10
} catch {
    Write-Host "✗ Failed to fetch user data: $_" -ForegroundColor Red
    exit 1
}

Write-Host "`n=== All Tests Passed! ===" -ForegroundColor Green
```

Run it:
```powershell
.\test-microservices.ps1
```

## Next Steps

1. Read `API_DOCUMENTATION.md` for complete API reference
2. Import `Microservices_Postman_Collection.json` into Postman
3. Check `ELK_SETUP_GUIDE.md` for logging setup
4. Review `EXCEPTION_HANDLING_SUMMARY.md` for error handling
5. See `OBSERVABILITY_IMPLEMENTATION_GUIDE.md` for monitoring

## Support

For issues or questions:
1. Check service logs
2. Verify Eureka dashboard
3. Check database connections
4. Review application.yml configurations
