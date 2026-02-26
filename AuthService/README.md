# AuthService - Authentication Microservice

Centralized authentication service for the microservices architecture.

## Features

- User Registration
- User Login with JWT token generation
- Token Validation endpoint for API Gateway
- Password encryption using BCrypt
- Role-based access control (USER, ADMIN, MODERATOR)
- Account status management (enabled/disabled, locked/unlocked)

## Database

- **Database Name**: `auth_service_db`
- **Port**: 3306
- **Tables**: `users`

### Create Database

```sql
CREATE DATABASE auth_service_db;
```

## API Endpoints

### 1. Register User

**POST** `/auth/register`

```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123",
  "name": "John Doe",
  "about": "Software Developer"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": "uuid-here",
  "username": "john_doe",
  "email": "john@example.com",
  "role": "USER",
  "message": "User registered successfully"
}
```

### 2. Login

**POST** `/auth/login`

```json
{
  "username": "john_doe",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": "uuid-here",
  "username": "john_doe",
  "email": "john@example.com",
  "role": "USER",
  "message": "Login successful"
}
```

### 3. Validate Token (Internal - used by API Gateway)

**POST** `/auth/validate`

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response:**
```json
{
  "valid": true,
  "username": "john_doe",
  "userId": "uuid-here",
  "role": "USER"
}
```

### 4. Health Check

**GET** `/auth/health`

**Response:** `AuthService is running`

## Configuration

### Environment Variables

```bash
JWT_SECRET=your-secret-key-minimum-32-characters
JWT_EXPIRATION=36000000
```

### application.yml

```yaml
server:
  port: 8086

spring:
  application:
    name: AuthService
  datasource:
    url: jdbc:mysql://localhost:3306/auth_service_db
    username: root
    password: your-password
```

## Running the Service

1. Create the database:
   ```sql
   CREATE DATABASE auth_service_db;
   ```

2. Start the service:
   ```bash
   mvn spring-boot:run
   ```

3. Service will be available at: `http://localhost:8086`

## Integration with API Gateway

The API Gateway validates JWT tokens by calling the `/auth/validate` endpoint before forwarding requests to downstream services.

**Flow:**
1. User registers/logs in via AuthService
2. User receives JWT token
3. User sends requests to API Gateway with token in Authorization header
4. API Gateway validates token with AuthService
5. If valid, request is forwarded to the target microservice with user info in headers

## User Roles

- **USER**: Default role for registered users
- **ADMIN**: Administrative privileges
- **MODERATOR**: Moderation privileges

## Security

- Passwords are encrypted using BCrypt
- JWT tokens expire after 10 hours (configurable)
- Account can be disabled or locked
- All endpoints except `/auth/**` require authentication via API Gateway
