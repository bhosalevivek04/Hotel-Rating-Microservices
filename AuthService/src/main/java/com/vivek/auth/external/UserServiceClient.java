package com.vivek.auth.external;

import com.vivek.auth.dto.UserServiceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
	name = "UserService",
	fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {
	
	@PostMapping("/users")
	ResponseEntity<Object> createUser(@RequestBody UserServiceRequest request);
}
