package com.vivek.auth.external;

import com.vivek.auth.dto.UserServiceRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {
	
	@Override
	public ResponseEntity<Object> createUser(UserServiceRequest request) {
		log.error("FALLBACK: Failed to sync user to UserService - userId: {}, email: {}", 
				request.getUserId(), request.getEmail());
		log.error("Possible reasons: UserService is down, not registered in Eureka, or network issue");
		
		// Return error response
		throw new RuntimeException("UserService is unavailable - sync failed");
	}
}
