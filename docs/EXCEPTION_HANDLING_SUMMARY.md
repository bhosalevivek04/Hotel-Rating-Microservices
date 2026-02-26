# Global Exception Handling - Implementation Summary

## ✅ What Was Added

Global exception handlers have been added to all microservices for consistent error handling across the entire application.

---

## 📋 Services Updated

### 1. ✅ UserService (Enhanced)
**Location**: `UserService/src/main/java/com/vivek/user/service/exceptions/`

**Files**:
- `GlobalExceptionHandler.java` (Enhanced)
- `ResourceNotFoundException.java` (Already existed)
- `ApiResponse.java` (Already existed)

**Handles**:
- ✅ ResourceNotFoundException → 404 NOT_FOUND
- ✅ FeignException → 503 SERVICE_UNAVAILABLE
- ✅ MethodArgumentNotValidException → 400 BAD_REQUEST (NEW)
- ✅ IllegalArgumentException → 400 BAD_REQUEST (NEW)
- ✅ Exception → 500 INTERNAL_SERVER_ERROR

---

### 2. ✅ HotelService (Enhanced)
**Location**: `HotelService/src/main/java/com/vivek/hotel/`

**Files Created**:
- `payload/ApiResponse.java` (NEW)
- `exceptions/GlobalExceptionHandler.java` (Enhanced)
- `exceptions/ResourceNotFoundException.java` (Already existed)

**Handles**:
- ✅ ResourceNotFoundException → 404 NOT_FOUND
- ✅ MethodArgumentNotValidException → 400 BAD_REQUEST (NEW)
- ✅ IllegalArgumentException → 400 BAD_REQUEST (NEW)
- ✅ Exception → 500 INTERNAL_SERVER_ERROR (NEW)

**Improvements**:
- Fixed raw type warnings
- Added proper generic types
- Consistent response structure

---

### 3. ✅ RatingService (NEW)
**Location**: `RatingService/src/main/java/com/vivek/rating/`

**Files Created**:
- `exceptions/GlobalExceptionHandler.java` (NEW)
- `exceptions/ResourceNotFoundException.java` (NEW)
- `payload/ApiResponse.java` (NEW)

**Handles**:
- ✅ ResourceNotFoundException → 404 NOT_FOUND
- ✅ MethodArgumentNotValidException → 400 BAD_REQUEST
- ✅ IllegalArgumentException → 400 BAD_REQUEST
- ✅ Exception → 500 INTERNAL_SERVER_ERROR

---

### 4. ✅ AuthService (NEW)
**Location**: `AuthService/src/main/java/com/vivek/auth/`

**Files Created**:
- `exception/GlobalExceptionHandler.java` (NEW)
- `exception/ResourceNotFoundException.java` (NEW)
- `exception/UserAlreadyExistsException.java` (NEW)
- `exception/InvalidCredentialsException.java` (NEW)
- `payload/ApiResponse.java` (NEW)

**Handles**:
- ✅ ResourceNotFoundException → 404 NOT_FOUND
- ✅ UserAlreadyExistsException → 409 CONFLICT
- ✅ InvalidCredentialsException → 401 UNAUTHORIZED
- ✅ BadCredentialsException → 401 UNAUTHORIZED
- ✅ MethodArgumentNotValidException → 400 BAD_REQUEST
- ✅ FeignException → 503 SERVICE_UNAVAILABLE
- ✅ IllegalArgumentException → 400 BAD_REQUEST
- ✅ Exception → 500 INTERNAL_SERVER_ERROR

---

## 📊 Exception Types & HTTP Status Codes

| Exception | HTTP Status | Code | Use Case |
|-----------|-------------|------|----------|
| ResourceNotFoundException | NOT_FOUND | 404 | User/Hotel/Rating not found |
| UserAlreadyExistsException | CONFLICT | 409 | Duplicate user registration |
| InvalidCredentialsException | UNAUTHORIZED | 401 | Wrong username/password |
| BadCredentialsException | UNAUTHORIZED | 401 | Spring Security auth failure |
| MethodArgumentNotValidException | BAD_REQUEST | 400 | Validation errors (@Valid) |
| IllegalArgumentException | BAD_REQUEST | 400 | Invalid input parameters |
| FeignException | SERVICE_UNAVAILABLE | 503 | Service communication failure |
| Exception | INTERNAL_SERVER_ERROR | 500 | Unexpected errors |

---

## 🎯 Response Format

### Success Response (from controllers):
```json
{
  "userId": "abc-123",
  "name": "John Doe",
  "email": "john@example.com"
}
```

### Error Response (from exception handler):
```json
{
  "message": "User with given id is not found on server",
  "success": false,
  "status": "NOT_FOUND"
}
```

### Validation Error Response:
```json
{
  "message": "Validation failed",
  "success": false,
  "status": "BAD_REQUEST",
  "errors": {
    "name": "Name is required",
    "email": "Invalid email format",
    "rating": "Rating must be between 1 and 5"
  }
}
```

---

## 🧪 Testing Exception Handlers

### Test 1: Resource Not Found (404)
```bash
curl -X GET http://localhost:8084/users/invalid-id \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response**:
```json
{
  "message": "User with given id is not found on server",
  "success": false,
  "status": "NOT_FOUND"
}
```

---

### Test 2: Validation Error (400)
```bash
curl -X POST http://localhost:8084/ratings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "userId": "",
    "hotelId": "",
    "rating": 10,
    "feedback": ""
  }'
```

**Expected Response**:
```json
{
  "message": "Validation failed",
  "success": false,
  "status": "BAD_REQUEST",
  "errors": {
    "userId": "User ID is required",
    "hotelId": "Hotel ID is required",
    "rating": "Rating must be between 1 and 5"
  }
}
```

---

### Test 3: User Already Exists (409)
```bash
curl -X POST http://localhost:8084/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john.doe@example.com",
    "password": "Test123!",
    "name": "John Doe",
    "about": "Test"
  }'
```

**Expected Response** (if user exists):
```json
{
  "message": "Username already exists",
  "success": false,
  "status": "CONFLICT"
}
```

---

### Test 4: Invalid Credentials (401)
```bash
curl -X POST http://localhost:8084/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "wrong_password"
  }'
```

**Expected Response**:
```json
{
  "message": "Invalid username or password",
  "success": false,
  "status": "UNAUTHORIZED"
}
```

---

### Test 5: Service Unavailable (503)
Stop RatingService and try:
```bash
curl -X GET http://localhost:8084/users/YOUR_USER_ID \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response**:
```json
{
  "message": "Service communication failed: [503] during [GET] to [http://RatingService/ratings/users/YOUR_USER_ID]",
  "success": false,
  "status": "SERVICE_UNAVAILABLE"
}
```

---

### Test 6: Internal Server Error (500)
Any unexpected error will return:
```json
{
  "message": "An error occurred: Unexpected error message",
  "success": false,
  "status": "INTERNAL_SERVER_ERROR"
}
```

---

## 🔧 How to Use in Controllers

### Example 1: Throw ResourceNotFoundException
```java
@GetMapping("/{userId}")
public ResponseEntity<User> getUser(@PathVariable String userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    return ResponseEntity.ok(user);
}
```

### Example 2: Use @Valid for Validation
```java
@PostMapping
public ResponseEntity<Rating> createRating(@Valid @RequestBody CreateRatingRequest request) {
    // Validation happens automatically
    // If validation fails, MethodArgumentNotValidException is thrown
    Rating rating = ratingService.createRating(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(rating);
}
```

### Example 3: Throw Custom Exception in AuthService
```java
public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
        throw new UserAlreadyExistsException("Username already exists");
    }
    
    if (userRepository.existsByEmail(request.getEmail())) {
        throw new UserAlreadyExistsException("Email already exists");
    }
    
    // Continue with registration...
}
```

---

## 📝 Adding Validation to DTOs

### Example: CreateRatingRequest
```java
package com.vivek.rating.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateRatingRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Hotel ID is required")
    private String hotelId;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;
    
    @Size(max = 500, message = "Feedback cannot exceed 500 characters")
    private String feedback;
}
```

### Example: CreateUserRequest
```java
package com.vivek.user.service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateUserRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @Size(max = 200, message = "About section cannot exceed 200 characters")
    private String about;
}
```

---

## 🎨 Benefits

### 1. Consistency
- ✅ All services return errors in the same format
- ✅ Same HTTP status codes for same error types
- ✅ Predictable API behavior

### 2. Better Developer Experience
- ✅ Clear error messages
- ✅ Validation errors show which fields are invalid
- ✅ Easy to debug issues

### 3. Production Ready
- ✅ Proper HTTP status codes
- ✅ No stack traces exposed to clients
- ✅ Graceful error handling

### 4. Maintainability
- ✅ Centralized error handling
- ✅ Easy to add new exception types
- ✅ Consistent across all services

---

## 🚀 Next Steps

### 1. Add Validation Annotations
Add `@Valid` and validation constraints to your DTOs:
- CreateUserRequest
- CreateHotelRequest
- CreateRatingRequest
- RegisterRequest
- LoginRequest

### 2. Use Custom Exceptions
Replace generic exceptions with specific ones:
```java
// Before
if (user == null) {
    throw new RuntimeException("User not found");
}

// After
if (user == null) {
    throw new ResourceNotFoundException("User not found with id: " + userId);
}
```

### 3. Test All Error Scenarios
- Test each exception type
- Verify HTTP status codes
- Check error message format

### 4. Add Logging
Enhance exception handlers with logging:
```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ApiResponse> handleGlobalException(Exception ex) {
    logger.error("Unexpected error occurred", ex);
    // Return response...
}
```

---

## 📚 Common Validation Annotations

| Annotation | Description | Example |
|------------|-------------|---------|
| @NotNull | Field cannot be null | `@NotNull Integer rating` |
| @NotBlank | String cannot be null/empty/whitespace | `@NotBlank String name` |
| @NotEmpty | Collection/Array cannot be empty | `@NotEmpty List<String> tags` |
| @Size | String/Collection size constraints | `@Size(min=2, max=50) String name` |
| @Min | Minimum numeric value | `@Min(1) Integer rating` |
| @Max | Maximum numeric value | `@Max(5) Integer rating` |
| @Email | Valid email format | `@Email String email` |
| @Pattern | Regex pattern match | `@Pattern(regexp="^[A-Z].*") String name` |
| @Past | Date must be in the past | `@Past LocalDate birthDate` |
| @Future | Date must be in the future | `@Future LocalDate eventDate` |

---

## ✅ Summary

All microservices now have:
- ✅ Global exception handlers
- ✅ Consistent error response format
- ✅ Proper HTTP status codes
- ✅ Validation support
- ✅ Custom exception types
- ✅ Service-specific exceptions (AuthService)

**Your microservices now handle errors professionally and consistently!** 🎉
