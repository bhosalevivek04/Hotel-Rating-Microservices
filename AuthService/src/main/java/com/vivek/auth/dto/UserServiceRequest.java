package com.vivek.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserServiceRequest {
	private String userId;
	private String name;
	private String email;
	private String about;
}
