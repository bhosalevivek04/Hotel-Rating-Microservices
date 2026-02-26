package com.vivek.auth.controller;

import com.vivek.auth.dto.*;
import com.vivek.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	
	private final AuthService authService;
	
	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		AuthResponse response = authService.register(request);
		
		if (response.getToken() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
		
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		AuthResponse response = authService.login(request);
		
		if (response.getToken() == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}
		
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/validate")
	public ResponseEntity<ValidateTokenResponse> validateToken(@RequestBody ValidateTokenRequest request) {
		ValidateTokenResponse response = authService.validateToken(request.getToken());
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/health")
	public ResponseEntity<String> health() {
		return ResponseEntity.ok("AuthService is running");
	}
}
