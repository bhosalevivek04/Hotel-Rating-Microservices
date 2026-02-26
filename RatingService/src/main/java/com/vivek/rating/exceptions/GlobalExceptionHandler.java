package com.vivek.rating.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;

import com.vivek.rating.payload.ApiResponse;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
		ApiResponse response = ApiResponse.builder()
				.message(ex.getMessage())
				.success(false)
				.status(HttpStatus.NOT_FOUND)
				.build();
		return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
		Map<String, Object> response = new HashMap<>();
		Map<String, String> errors = new HashMap<>();
		
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});
		
		response.put("message", "Validation failed");
		response.put("success", false);
		response.put("status", HttpStatus.BAD_REQUEST);
		response.put("errors", errors);
		
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
		ApiResponse response = ApiResponse.builder()
				.message(ex.getMessage())
				.success(false)
				.status(HttpStatus.BAD_REQUEST)
				.build();
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse> handleGlobalException(Exception ex) {
		ApiResponse response = ApiResponse.builder()
				.message("An error occurred: " + ex.getMessage())
				.success(false)
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.build();
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
