package com.vivek.auth.controller;

import com.vivek.auth.dto.UserServiceRequest;
import com.vivek.auth.entity.User;
import com.vivek.auth.external.UserServiceClient;
import com.vivek.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
@Slf4j
public class SyncController {
	
	private final UserRepository userRepository;
	private final UserServiceClient userServiceClient;
	
	/**
	 * Manually sync a user from AuthService to UserService
	 * Useful for fixing sync failures
	 */
	@PostMapping("/user/{userId}")
	public ResponseEntity<Map<String, Object>> syncUser(@PathVariable String userId) {
		Map<String, Object> response = new HashMap<>();
		
		try {
			// Find user in AuthService
			User user = userRepository.findById(userId)
					.orElseThrow(() -> new RuntimeException("User not found in AuthService"));
			
			log.info("Found user in AuthService: userId={}, email={}", user.getUserId(), user.getEmail());
			
			// Create sync request
			UserServiceRequest request = UserServiceRequest.builder()
					.userId(user.getUserId())
					.name(user.getName())
					.email(user.getEmail())
					.about(user.getAbout())
					.build();
			
			log.info("Attempting manual sync to UserService...");
			
			// Sync to UserService
			userServiceClient.createUser(request);
			
			log.info("✓ Manual sync successful: userId={}", user.getUserId());
			
			response.put("success", true);
			response.put("message", "User synced successfully");
			response.put("userId", user.getUserId());
			response.put("email", user.getEmail());
			
			return ResponseEntity.ok(response);
			
		} catch (Exception e) {
			log.error("✗ Manual sync failed: userId={}, error={}", userId, e.getMessage(), e);
			
			response.put("success", false);
			response.put("message", "Sync failed: " + e.getMessage());
			response.put("userId", userId);
			
			return ResponseEntity.status(500).body(response);
		}
	}
	
	/**
	 * Check sync status - verify if user exists in both databases
	 */
	@GetMapping("/status/{userId}")
	public ResponseEntity<Map<String, Object>> checkSyncStatus(@PathVariable String userId) {
		Map<String, Object> response = new HashMap<>();
		
		// Check AuthService
		boolean existsInAuth = userRepository.existsById(userId);
		response.put("existsInAuthService", existsInAuth);
		
		if (existsInAuth) {
			User user = userRepository.findById(userId).get();
			response.put("authServiceData", Map.of(
				"userId", user.getUserId(),
				"username", user.getUsername(),
				"email", user.getEmail(),
				"name", user.getName()
			));
		}
		
		// Note: We can't directly check UserService from here without another Feign call
		response.put("message", "Check UserService database manually: SELECT * FROM micro_users WHERE id = '" + userId + "'");
		
		return ResponseEntity.ok(response);
	}
}
