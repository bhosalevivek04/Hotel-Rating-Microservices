package com.vivek.user.service.external.services;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.vivek.user.service.entities.Rating;

@FeignClient(name = "RatingService")
public interface RatingService {
	
	@PostMapping("/ratings")
	ResponseEntity<Rating> createRating(@RequestBody Rating rating);

	@PutMapping("/ratings/{ratingId}")
	ResponseEntity<Rating> updateRating(@PathVariable String ratingId, @RequestBody Rating rating);

	@DeleteMapping("/ratings/{ratingId}")
	ResponseEntity<Void> deleteRating(@PathVariable String ratingId);
}
