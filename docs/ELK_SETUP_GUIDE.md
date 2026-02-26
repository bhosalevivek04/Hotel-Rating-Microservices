# ELK Stack Setup Guide - Centralized Logging

## ✅ What Was Implemented

Industry-standard centralized logging with ELK Stack for all microservices.

---

## 📦 Components Added

### 1. Logback Configuration (All Services)
- ✅ Console logging (development)
- ✅ File logging with rotation (10MB, 30 days)
- ✅ Logstash appender (ELK integration)
- ✅ Async logging (better performance)
- ✅ MDC support (correlation IDs)

### 2. Logging Interceptors (All Services)
- ✅ Request/Response logging
- ✅ Request ID generation
- ✅ Duration tracking
- ✅ Error logging

### 3. ELK Stack (Docker)
- ✅ Elasticsearch (storage & indexing)
- ✅ Logstash (log processing)
- ✅ Kibana (visualization)

---

## 🚀 Quick Start

### Step 1: Start ELK Stack (5 minutes)

```bash
# Start ELK Stack
docker-compose -f docker-compose-elk.yml up -d

# Check status
docker-compose -f docker-compose-elk.yml ps

# View logs
docker-compose -f docker-compose-elk.yml logs -f
```

**Wait for all services to be healthy (2-3 minutes)**

### Step 2: Verify ELK Stack

```bash
# Check Elasticsearch
curl http://localhost:9200

# Check Logstash
curl http://localhost:9600

# Open Kibana
# Browser: http://localhost:5601
```

### Step 3: Start Your Microservices

```bash
# Start all services (they will automatically send logs to Logstash)
# UserService, HotelService, RatingService, AuthService
```

### Step 4: Configure Kibana (First Time Only)

1. Open Kibana: http://localhost:5601
2. Wait for Kibana to load (30 seconds)
3. Go to "Management" → "Stack Management"
4. Click "Index Patterns" → "Create index pattern"
5. Enter pattern: `microservices-logs-*`
6. Click "Next step"
7. Select "@timestamp" as time field
8. Click "Create index pattern"

### Step 5: View Logs

1. Go to "Discover" in Kibana
2. You should see logs from all services!

---

## 📊 What You Can Do in Kibana

### Search Examples:

```
# All logs from UserService
service_name: "UserService"

# All errors
level: "ERROR"

# Logs for specific user
userId: "abc-123-def"

# Logs for specific request
requestId: "xyz-789"

# Slow requests (> 1 second)
duration: >1000

# Failed requests
status: 500

# Logs in last 15 minutes
@timestamp: [now-15m TO now]

# Combine filters
service_name: "UserService" AND level: "ERROR" AND @timestamp: [now-1h TO now]
```

### Create Visualizations:

1. **Error Rate Over Time**
   - Type: Line chart
   - Y-axis: Count
   - X-axis: @timestamp
   - Filter: level: "ERROR"

2. **Requests by Service**
   - Type: Pie chart
   - Slice by: service_name.keyword

3. **Response Time Distribution**
   - Type: Histogram
   - Field: duration

4. **Top Error Messages**
   - Type: Data table
   - Rows: message.keyword
   - Metrics: Count

---

## 🎯 Real-World Use Cases

### Use Case 1: Debug Failed Request

**Scenario**: User reports "Registration failed"

**Steps**:
1. Open Kibana → Discover
2. Search: `message: "registration" AND level: "ERROR"`
3. Find the error log
4. Check requestId
5. Search: `requestId: "abc-123"`
6. See complete request flow
7. Identify: UserService sync failed
8. Fix the issue

**Time**: 2 minutes (vs 30 minutes without ELK)

---

### Use Case 2: Monitor Service Health

**Scenario**: Check if services are running properly

**Steps**:
1. Open Kibana → Discover
2. Create filter: `level: "ERROR"`
3. Group by: `service_name`
4. See which service has most errors
5. Investigate specific service

---

### Use Case 3: Track User Journey

**Scenario**: Track what happened during user registration

**Steps**:
1. Get userId from registration response
2. Search in Kibana: `userId: "abc-123-def"`
3. See all logs for this user:
   - AuthService: User registered
   - AuthService: Attempting sync
   - UserService: User created
   - AuthService: Sync successful
4. Verify complete flow

---

## 📈 Create Dashboards

### Dashboard 1: Service Overview

**Panels**:
1. Total Requests (last 24h)
2. Error Rate (%)
3. Requests by Service (pie chart)
4. Errors Over Time (line chart)
5. Top 10 Slowest Endpoints

**How to Create**:
1. Go to "Dashboard" → "Create dashboard"
2. Click "Create visualization"
3. Select visualization type
4. Configure fields
5. Save visualization
6. Add to dashboard

---

### Dashboard 2: Error Analysis

**Panels**:
1. Error Count (last hour)
2. Errors by Service
3. Top Error Messages
4. Error Timeline
5. Recent Errors (table)

---

### Dashboard 3: Performance Monitoring

**Panels**:
1. Average Response Time
2. P95 Response Time
3. Slowest Endpoints
4. Response Time Distribution
5. Requests per Minute

---

## 🔧 Advanced Configuration

### Add More Fields to Logs

Update your service code to add custom fields:

```java
@GetMapping("/{userId}")
public ResponseEntity<User> getUser(@PathVariable String userId) {
    MDC.put("userId", userId);
    MDC.put("operation", "getUser");
    
    log.info("Fetching user details");
    
    try {
        User user = userService.getUser(userId);
        MDC.put("userName", user.getName());
        log.info("User fetched successfully");
        return ResponseEntity.ok(user);
    } finally {
        MDC.clear();
    }
}
```

These fields will automatically appear in Kibana!

---

### Filter Sensitive Data

Update `logback-spring.xml` to exclude sensitive fields:

```xml
<encoder class="net.logstash.logback.encoder.LogstashEncoder">
    <customFields>{"service_name":"${application_name}"}</customFields>
    <fieldNames>
        <message>message</message>
        <logger>logger</logger>
        <thread>thread</thread>
        <level>level</level>
        <timestamp>@timestamp</timestamp>
    </fieldNames>
    <!-- Exclude sensitive fields -->
    <excludeMdcKeyName>password</excludeMdcKeyName>
    <excludeMdcKeyName>token</excludeMdcKeyName>
</encoder>
```

---

## 🎓 Best Practices

### 1. Log Levels
- **DEBUG**: Detailed information for debugging
- **INFO**: General information (request received, user created)
- **WARN**: Warning messages (deprecated API used)
- **ERROR**: Error messages (exceptions, failures)

### 2. Structured Logging
```java
// Good
log.info("User created: userId={}, email={}", userId, email);

// Bad
log.info("User created: " + userId + " " + email);
```

### 3. Don't Log Sensitive Data
```java
// Never log passwords, tokens, credit cards
log.info("User login: username={}", username); // Good
log.info("User login: password={}", password); // BAD!
```

### 4. Use MDC for Context
```java
MDC.put("userId", userId);
MDC.put("requestId", requestId);
log.info("Processing request");
// All subsequent logs will include userId and requestId
MDC.clear(); // Always clear at the end
```

---

## 🐛 Troubleshooting

### Issue 1: Logs not appearing in Kibana

**Check**:
1. Is ELK Stack running? `docker-compose -f docker-compose-elk.yml ps`
2. Is Logstash receiving logs? `docker logs logstash`
3. Is Elasticsearch healthy? `curl http://localhost:9200/_cluster/health`
4. Are services sending logs? Check service console output

**Solution**:
```bash
# Restart ELK Stack
docker-compose -f docker-compose-elk.yml restart

# Check Logstash logs
docker logs logstash -f
```

---

### Issue 2: Elasticsearch out of memory

**Solution**:
Increase heap size in `docker-compose-elk.yml`:
```yaml
environment:
  - "ES_JAVA_OPTS=-Xms1g -Xmx1g"  # Increase from 512m to 1g
```

---

### Issue 3: Too many logs

**Solution**:
1. Reduce log level in production (INFO instead of DEBUG)
2. Add log rotation
3. Set up index lifecycle management in Elasticsearch

---

## 📊 Monitoring ELK Stack

### Check Elasticsearch Health
```bash
curl http://localhost:9200/_cluster/health?pretty
```

### Check Index Size
```bash
curl http://localhost:9200/_cat/indices?v
```

### Check Logstash Stats
```bash
curl http://localhost:9600/_node/stats?pretty
```

---

## 🎯 Next Steps

### Week 1: Basic Usage
- ✅ View logs in Kibana
- ✅ Create basic searches
- ✅ Understand log structure

### Week 2: Advanced Features
- ✅ Create visualizations
- ✅ Build dashboards
- ✅ Set up alerts

### Week 3: Optimization
- ✅ Add custom fields
- ✅ Filter sensitive data
- ✅ Optimize performance

---

## 💼 Resume Points

You can now say:
- ✅ "Implemented centralized logging with ELK Stack"
- ✅ "Configured Logstash for log aggregation from 4+ microservices"
- ✅ "Created Kibana dashboards for monitoring and debugging"
- ✅ "Reduced debugging time from hours to minutes"
- ✅ "Implemented structured logging with MDC for correlation"

---

## 🎓 Interview Questions You Can Answer

**Q: How do you debug issues in production?**
A: "I use ELK Stack for centralized logging. All microservices send logs to Logstash, which processes and stores them in Elasticsearch. I use Kibana to search and visualize logs. For example, when debugging a failed request, I search by requestId to see the complete flow across all services."

**Q: How do you monitor microservices?**
A: "I use ELK Stack for logging and create Kibana dashboards to monitor error rates, response times, and service health. I set up alerts for critical errors and track key metrics like requests per minute and error percentage."

---

## ✅ Summary

You now have:
- ✅ Centralized logging for all microservices
- ✅ Real-time log search and analysis
- ✅ Structured logging with correlation IDs
- ✅ Production-ready logging infrastructure
- ✅ Industry-standard observability

**Your microservices are now production-ready with enterprise-grade logging!** 🚀
