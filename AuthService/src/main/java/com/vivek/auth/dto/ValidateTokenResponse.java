package com.vivek.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidateTokenResponse {
	private Boolean valid;
	private String username;
	private String userId;
	private String role;
}
