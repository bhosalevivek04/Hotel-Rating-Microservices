package com.vivek.auth.service;

import com.vivek.auth.dto.*;
import com.vivek.auth.entity.User;
import com.vivek.auth.external.UserServiceClient;
import com.vivek.auth.repository.UserRepository;
import com.vivek.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final UserServiceClient userServiceClient;
	
	public AuthResponse register(RegisterRequest request) {
		// Check if username already exists
		if (userRepository.existsByUsername(request.getUsername())) {
			return AuthResponse.builder()
					.message("Username already exists")
					.build();
		}
		
		// Check if email already exists
		if (userRepository.existsByEmail(request.getEmail())) {
			return AuthResponse.builder()
					.message("Email already exists")
					.build();
		}
		
		// Create new user in AuthService
		User user = User.builder()
				.username(request.getUsername())
				.email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword()))
				.name(request.getName())
				.about(request.getAbout())
				.role(User.Role.USER)
				.enabled(true)
				.accountNonLocked(true)
				.build();
		
		User savedUser = userRepository.save(user);
		log.info("User registered in AuthService: {}", savedUser.getUsername());
		
		// Sync user to UserService
		try {
			UserServiceRequest userServiceRequest = UserServiceRequest.builder()
					.userId(savedUser.getUserId())
					.name(savedUser.getName())
					.email(savedUser.getEmail())
					.about(savedUser.getAbout())
					.build();
			
			log.info("Attempting to sync user to UserService - userId: {}, email: {}", 
					savedUser.getUserId(), savedUser.getEmail());
			
			userServiceClient.createUser(userServiceRequest);
			log.info("✓ User successfully synced to UserService: userId={}", savedUser.getUserId());
		} catch (Exception e) {
			log.error("✗ Failed to sync user to UserService: userId={}, error={}", 
					savedUser.getUserId(), e.getMessage(), e);
			// Continue even if sync fails - user is registered in AuthService
		}
		
		// Generate JWT token
		String token = jwtUtil.generateToken(
				savedUser.getUsername(),
				savedUser.getUserId(),
				savedUser.getRole().name()
		);
		
		return AuthResponse.builder()
				.token(token)
				.userId(savedUser.getUserId())
				.username(savedUser.getUsername())
				.email(savedUser.getEmail())
				.role(savedUser.getRole().name())
				.message("User registered successfully")
				.build();
	}
	
	public AuthResponse login(LoginRequest request) {
		// Find user by username
		User user = userRepository.findByUsername(request.getUsername())
				.orElse(null);
		
		if (user == null) {
			log.warn("Login failed: User not found - {}", request.getUsername());
			return AuthResponse.builder()
					.message("Invalid username or password")
					.build();
		}
		
		// Check if account is enabled and not locked
		if (!user.getEnabled()) {
			return AuthResponse.builder()
					.message("Account is disabled")
					.build();
		}
		
		if (!user.getAccountNonLocked()) {
			return AuthResponse.builder()
					.message("Account is locked")
					.build();
		}
		
		// Verify password
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			log.warn("Login failed: Invalid password for user - {}", request.getUsername());
			return AuthResponse.builder()
					.message("Invalid username or password")
					.build();
		}
		
		// Generate JWT token
		String token = jwtUtil.generateToken(
				user.getUsername(),
				user.getUserId(),
				user.getRole().name()
		);
		
		log.info("User logged in successfully: {}", user.getUsername());
		
		return AuthResponse.builder()
				.token(token)
				.userId(user.getUserId())
				.username(user.getUsername())
				.email(user.getEmail())
				.role(user.getRole().name())
				.message("Login successful")
				.build();
	}
	
	public ValidateTokenResponse validateToken(String token) {
		try {
			if (jwtUtil.validateToken(token)) {
				String username = jwtUtil.extractUsername(token);
				String userId = jwtUtil.extractClaim(token, claims -> claims.get("userId", String.class));
				String role = jwtUtil.extractClaim(token, claims -> claims.get("role", String.class));
				
				return ValidateTokenResponse.builder()
						.valid(true)
						.username(username)
						.userId(userId)
						.role(role)
						.build();
			}
		} catch (Exception e) {
			log.error("Token validation failed: {}", e.getMessage());
		}
		
		return ValidateTokenResponse.builder()
				.valid(false)
				.build();
	}
}
