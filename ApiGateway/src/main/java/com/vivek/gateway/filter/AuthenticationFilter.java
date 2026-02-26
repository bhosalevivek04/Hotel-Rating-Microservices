package com.vivek.gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

	@Autowired
	private WebClient.Builder webClientBuilder;

	public AuthenticationFilter() {
		super(Config.class);
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();

			// Check if Authorization header is present
			if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
				return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
			}

			String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
			if (authHeader != null && authHeader.startsWith("Bearer ")) {
				authHeader = authHeader.substring(7);
			} else {
				return onError(exchange, "Invalid authorization header format", HttpStatus.UNAUTHORIZED);
			}

			String token = authHeader;

			// Call AuthService to validate token
			return webClientBuilder.build()
					.post()
					.uri("http://AuthService/auth/validate")
					.bodyValue(Map.of("token", token))
					.retrieve()
					.bodyToMono(Map.class)
					.flatMap(response -> {
						Boolean valid = (Boolean) response.get("valid");
						
						if (valid != null && valid) {
							// Add user info to request headers
							ServerHttpRequest modifiedRequest = exchange.getRequest()
									.mutate()
									.header("X-User-Name", (String) response.get("username"))
									.header("X-User-Id", (String) response.get("userId"))
									.header("X-User-Role", (String) response.get("role"))
									.build();
							
							return chain.filter(exchange.mutate().request(modifiedRequest).build());
						} else {
							return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
						}
					})
					.onErrorResume(e -> onError(exchange, "Token validation failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED));
		};
	}

	private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(httpStatus);
		return response.setComplete();
	}

	public static class Config {
	}
}
