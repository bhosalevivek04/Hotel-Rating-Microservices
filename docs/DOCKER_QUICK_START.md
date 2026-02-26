# Docker Quick Start - Microservices

## 🚀 One Command Deployment

```bash
docker-compose up -d --build
```

That's it! Wait 2-3 minutes and your entire microservices architecture is running.

## 📋 What Gets Deployed

✅ MySQL Database (auth_service_db, microservices, rating_service)
✅ PostgreSQL Database (microservice)
✅ Service Registry (Eureka) - Port 8761
✅ AuthService - Port 8086
✅ UserService - Port 8081
✅ HotelService - Port 8082
✅ RatingService - Port 8083
✅ API Gateway - Port 8084

## 🔍 Check Status

```bash
# View all containers
docker-compose ps

# View logs
docker-compose logs -f

# Check Eureka Dashboard
# Open: http://localhost:8761
```

## 🧪 Test the Deployment

```powershell
# Register a user
$body = @{
    username = "docker_test"
    password = "password123"
    email = "test@example.com"
    name = "Test User"
    about = "Testing Docker"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8086/auth/register" -Method POST -ContentType "application/json" -Body $body

Write-Host "✓ User registered successfully!"
Write-Host "Token: $($response.token)"
Write-Host "UserId: $($response.userId)"
```

## 🛑 Stop Everything

```bash
# Stop all services
docker-compose down

# Stop and remove all data
docker-compose down -v
```

## 📊 Common Commands

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f service-name

# Restart service
docker-compose restart service-name

# Rebuild and restart
docker-compose up -d --build

# Scale service
docker-compose up -d --scale user-service=3

# View resource usage
docker stats
```

## 🔧 Troubleshooting

### Service won't start?
```bash
docker-compose logs service-name
docker-compose restart service-name
```

### Clean start?
```bash
docker-compose down -v
docker-compose up -d --build
```

### Check database?
```bash
# MySQL
docker-compose exec mysql mysql -u root -proot

# PostgreSQL
docker-compose exec postgres psql -U postgres -d microservice
```

## 📚 Full Documentation

See `DOCKER_DEPLOYMENT_GUIDE.md` for complete details.

## 🎯 Service URLs

- Eureka: http://localhost:8761
- API Gateway: http://localhost:8084
- AuthService: http://localhost:8086
- UserService: http://localhost:8081
- HotelService: http://localhost:8082
- RatingService: http://localhost:8083

## ⚡ Quick Tips

1. **First time?** Wait 2-3 minutes for all services to start
2. **Check Eureka** to see if all services are registered
3. **View logs** if something doesn't work: `docker-compose logs -f`
4. **Clean start** if needed: `docker-compose down -v && docker-compose up -d --build`

## 🎉 That's It!

Your complete microservices architecture is now running in Docker!
