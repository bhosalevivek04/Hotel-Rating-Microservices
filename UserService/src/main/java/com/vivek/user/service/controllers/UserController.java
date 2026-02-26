package com.vivek.user.service.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vivek.user.service.entities.User;
import com.vivek.user.service.services.UserService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
	private final UserService userService;
	private Logger logger = LoggerFactory.getLogger(UserController.class);
	private int retryCount = 1;

	@PostMapping
	public ResponseEntity<User> createUser(@RequestBody User user) {
		User saveUser = userService.saveUser(user);
		return ResponseEntity.status(HttpStatus.CREATED).body(saveUser);
	}

	@GetMapping("/{userId}")
	public ResponseEntity<User> getSingleUser(@PathVariable String userId) {
		logger.info("Get user request for userId: {}", userId);
		User user = userService.getUser(userId);
		return ResponseEntity.ok(user);
	}

	public ResponseEntity<User> ratingHotelFallback(String userId, Exception ex) {
		logger.warn("Fallback triggered for userId: {} - Exception: {}", userId, ex.getClass().getSimpleName());
		logger.info("Fallback is executed because service is down: {}", ex.getMessage());
		
		// Check if it's a rate limiter exception
		if (ex.getClass().getSimpleName().equals("RequestNotPermitted")) {
			User user = User.builder().email("dummy@gmail.com").name("Dummy")
					.about("Rate limit exceeded - too many requests").userId("141234").build();
			return new ResponseEntity<User>(user, HttpStatus.TOO_MANY_REQUESTS); // 429
		}
		
		// For other exceptions (circuit breaker, service down, etc.)
		User user = User.builder().email("dummy@gmail.com").name("Dummy")
				.about("This user is created dummy because some service is down").userId("141234").build();
		return new ResponseEntity<User>(user, HttpStatus.SERVICE_UNAVAILABLE); // 503
	}

	@GetMapping
	public ResponseEntity<List<User>> getAllUser() {
		logger.info("Fetching all users");
		List<User> allUser = userService.getAllUser();
		return ResponseEntity.ok(allUser);
	}

	public ResponseEntity<List<User>> ratingHotelFallbackForAll(Exception ex) {
		logger.info("Fallback is executed for getAllUsers because service is down: {}", ex.getMessage());
		
		// Check if it's a rate limiter exception
		if (ex.getClass().getSimpleName().equals("RequestNotPermitted")) {
			return new ResponseEntity<>(List.of(), HttpStatus.TOO_MANY_REQUESTS); // 429
		}
		
		// Return users without ratings when services are down
		List<User> usersWithoutRatings = userService.getAllUserWithoutExternalCalls();
		logger.info("Returning {} users without ratings/hotels due to service unavailability",
				usersWithoutRatings.size());
		return new ResponseEntity<>(usersWithoutRatings, HttpStatus.SERVICE_UNAVAILABLE); // 503
	}

}
