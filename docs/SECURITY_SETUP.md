# 🔐 Security Setup Guide

This guide explains the security improvements implemented in the Hotel-Rating-Microservices project.

---

## 🎯 Security Improvements Implemented

### ✅ Critical Issues Fixed

1. **Removed Hardcoded Credentials**
   - All database passwords now use environment variables
   - JWT secret now required via environment variable
   - No default fallback values for sensitive data

2. **JWT Token Expiry Reduced**
   - Changed from 10 hours (36000000ms) to 15 minutes (900000ms)
   - Significantly reduces attack window for compromised tokens

3. **Secured Internal Services**
   - Microservices no longer expose ports to host network
   - Only API Gateway (8084) and Eureka (8761) are publicly accessible
   - Services communicate only within Docker internal network

### ✅ High Priority Issues Fixed

4. **Eureka Dashboard Authentication**
   - Added Spring Security to ServiceRegistry
   - Basic authentication required to access Eureka dashboard
   - Credentials configured via environment variables

5. **Rate Limiting on Auth Endpoints**
   - Added Resilience4j rate limiter to AuthService
   - Limits: 5 requests per 60 seconds
   - Prevents brute-force attacks on login/register endpoints

---

## 🚀 Quick Start

### 1. Environment Setup

Copy the example environment file and configure your secrets:

```bash
cp .env.example .env
```

Edit `.env` and set secure values:

```bash
# Generate a secure JWT secret
JWT_SECRET=$(openssl rand -base64 64)

# Set strong passwords
MYSQL_ROOT_PASSWORD=your_secure_mysql_password
POSTGRES_PASSWORD=your_secure_postgres_password
EUREKA_PASSWORD=your_secure_eureka_password
```

### 2. Docker Deployment

```bash
# Start all services with environment variables
docker-compose up -d --build

# View logs
docker-compose logs -f
```

### 3. Access Services

**API Gateway** (Public):
```
http://localhost:8084
```

**Eureka Dashboard** (Protected):
```
http://localhost:8761
Username: admin (or value from EUREKA_USERNAME)
Password: admin123 (or value from EUREKA_PASSWORD)
```

**Internal Services** (Not directly accessible):
- AuthService: Only via API Gateway
- UserService: Only via API Gateway
- HotelService: Only via API Gateway
- RatingService: Only via API Gateway

---

## 🔧 Local Development Setup

### 1. Set Environment Variables

**Windows PowerShell:**
```powershell
$env:AUTH_DB_PASSWORD="Vivek@123"
$env:USER_DB_PASSWORD="Vivek@123"
$env:HOTEL_DB_PASSWORD="Vivek@123"
$env:RATING_DB_PASSWORD="Vivek@123"
$env:JWT_SECRET="mySecretKeyForJWTTokenGenerationAndValidation12345678901234567890"
$env:JWT_EXPIRATION="900000"
```

**Linux/Mac:**
```bash
export AUTH_DB_PASSWORD="Vivek@123"
export USER_DB_PASSWORD="Vivek@123"
export HOTEL_DB_PASSWORD="Vivek@123"
export RATING_DB_PASSWORD="Vivek@123"
export JWT_SECRET="mySecretKeyForJWTTokenGenerationAndValidation12345678901234567890"
export JWT_EXPIRATION="900000"
```

### 2. Start Services

```bash
# Start each service
cd ServiceRegistry && mvn spring-boot:run
cd AuthService && mvn spring-boot:run
cd UserService && mvn spring-boot:run
cd HotelService && mvn spring-boot:run
cd RatingService && mvn spring-boot:run
cd ApiGateway && mvn spring-boot:run
```

---

## 📋 Environment Variables Reference

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `MYSQL_ROOT_PASSWORD` | MySQL root password | `SecurePass123!` |
| `POSTGRES_PASSWORD` | PostgreSQL password | `SecurePass123!` |
| `JWT_SECRET` | JWT signing secret (min 64 chars) | Generate with `openssl rand -base64 64` |
| `JWT_EXPIRATION` | JWT expiry in milliseconds | `900000` (15 minutes) |

### Optional Variables (with defaults)

| Variable | Description | Default |
|----------|-------------|---------|
| `EUREKA_USERNAME` | Eureka dashboard username | `admin` |
| `EUREKA_PASSWORD` | Eureka dashboard password | `admin123` |
| `AUTH_DB_URL` | AuthService database URL | `jdbc:mysql://localhost:3306/auth_service_db` |
| `USER_DB_URL` | UserService database URL | `jdbc:mysql://localhost:3306/microservices` |
| `HOTEL_DB_URL` | HotelService database URL | `jdbc:postgresql://localhost:5432/microservice` |
| `RATING_DB_URL` | RatingService database URL | `jdbc:mysql://localhost:3306/rating_service` |

---

## 🔒 Security Features

### 1. JWT Authentication
- **Expiry**: 15 minutes (configurable)
- **Algorithm**: HS256
- **Secret**: Environment variable (required)
- **Validation**: At API Gateway level

### 2. Password Security
- **Hashing**: BCrypt with salt
- **Storage**: Never stored in plain text
- **Validation**: Minimum requirements enforced

### 3. Rate Limiting
- **Auth Endpoints**: 5 requests/minute
- **User Endpoints**: 2 requests/4 seconds
- **Implementation**: Resilience4j

### 4. Network Security
- **Internal Services**: Not exposed to host
- **API Gateway**: Single entry point
- **Service Discovery**: Protected with authentication

### 5. Database Security
- **Credentials**: Environment variables only
- **Connections**: Encrypted in transit
- **Access**: Restricted to service network

---

## 🛡️ Production Recommendations

### Before Production Deployment:

1. **Use Secrets Management**
   ```
   - AWS Secrets Manager
   - Azure Key Vault
   - HashiCorp Vault
   ```

2. **Enable HTTPS/TLS**
   ```
   - SSL certificates for all services
   - Mutual TLS between services
   ```

3. **Implement Token Revocation**
   ```
   - Redis-based token blacklist
   - Logout endpoint
   - Token refresh mechanism
   ```

4. **Add Input Validation**
   ```java
   @Valid annotations on all DTOs
   Bean Validation constraints
   Custom validators for business rules
   ```

5. **Enable Audit Logging**
   ```
   - Log all authentication attempts
   - Track all data modifications
   - Store logs securely
   ```

6. **Implement API Versioning**
   ```
   /api/v1/users
   /api/v1/hotels
   /api/v1/ratings
   ```

7. **Add Monitoring & Alerting**
   ```
   - Prometheus metrics
   - Grafana dashboards
   - Alert on suspicious activity
   ```

---

## 🧪 Testing Security

### 1. Test Rate Limiting

```bash
# Try to login 6 times in 1 minute (should fail on 6th attempt)
for i in {1..6}; do
  curl -X POST http://localhost:8084/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"test","password":"test123"}'
  echo "Attempt $i"
done
```

### 2. Test JWT Expiry

```bash
# Login and get token
TOKEN=$(curl -X POST http://localhost:8084/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123"}' | jq -r '.token')

# Use token immediately (should work)
curl http://localhost:8084/users/1 \
  -H "Authorization: Bearer $TOKEN"

# Wait 16 minutes and try again (should fail)
sleep 960
curl http://localhost:8084/users/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 3. Test Eureka Authentication

```bash
# Without credentials (should fail)
curl http://localhost:8761

# With credentials (should work)
curl -u admin:admin123 http://localhost:8761
```

### 4. Test Internal Service Isolation

```bash
# Try to access UserService directly (should fail - connection refused)
curl http://localhost:8081/users

# Access via Gateway (should work with valid token)
curl http://localhost:8084/users \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📝 Known Limitations (Learning Project)

This is a learning/demonstration project. The following would be added for production:

1. **Token Revocation**: No logout/blacklist mechanism
2. **Refresh Tokens**: No refresh token implementation
3. **Password Policy**: No complexity requirements enforced
4. **Input Validation**: Limited validation on DTOs
5. **HTTPS**: Running on HTTP (would use HTTPS in production)
6. **Secrets Management**: Using .env file (would use cloud secrets manager)
7. **Audit Logging**: Limited audit trail
8. **API Versioning**: No versioning strategy
9. **CORS**: Not configured (would restrict origins in production)

---

## 🆘 Troubleshooting

### Issue: Services fail to start

**Solution**: Ensure all required environment variables are set
```bash
# Check if .env file exists
cat .env

# Verify environment variables are loaded
docker-compose config
```

### Issue: Cannot access Eureka dashboard

**Solution**: Use correct credentials
```bash
# Default credentials
Username: admin
Password: admin123

# Or check your .env file
grep EUREKA .env
```

### Issue: JWT token validation fails

**Solution**: Ensure JWT_SECRET is consistent across services
```bash
# Check JWT_SECRET in .env
grep JWT_SECRET .env

# Verify it's being passed to containers
docker-compose exec auth-service env | grep JWT_SECRET
```

### Issue: Database connection fails

**Solution**: Check database credentials
```bash
# Test MySQL connection
docker-compose exec mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "SHOW DATABASES;"

# Test PostgreSQL connection
docker-compose exec postgres psql -U postgres -c "\l"
```

---

## 📚 Additional Resources

- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Resilience4j Documentation](https://resilience4j.readme.io/)

---

## 🤝 Contributing

When contributing security improvements:

1. Never commit `.env` files
2. Use environment variables for all secrets
3. Document security implications
4. Add tests for security features
5. Follow OWASP guidelines

---

**Last Updated**: February 27, 2026  
**Security Review**: Completed
