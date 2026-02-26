package com.vivek.rating.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vivek.rating.entities.Rating;
import com.vivek.rating.services.RatingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
public class RatingController {
	private final RatingService ratingService;

	@PostMapping
	public ResponseEntity<Rating> create(@RequestBody Rating rating) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.create(rating));
	}

	@GetMapping
	public ResponseEntity<List<Rating>> getRatings() {
		return ResponseEntity.ok(ratingService.getRatings());
	}

	@GetMapping("/users/{userId}")
	public ResponseEntity<List<Rating>> getRatingByUserId(@PathVariable String userId) {
		return ResponseEntity.ok(ratingService.getRatingByUserId(userId));
	}

	@GetMapping("/hotels/{hotelId}")
	public ResponseEntity<List<Rating>> getRatingByHotelId(@PathVariable String hotelId) {
		return ResponseEntity.ok(ratingService.getRatingByHotelId(hotelId));
	}

	@PutMapping("/{ratingId}")
	public ResponseEntity<Rating> updateRating(@PathVariable String ratingId, @RequestBody Rating rating) {
		return ResponseEntity.ok(ratingService.updateRating(ratingId, rating));
	}

	@DeleteMapping("/{ratingId}")
	public ResponseEntity<Void> deleteRating(@PathVariable String ratingId) {
		ratingService.deleteRating(ratingId);
		return ResponseEntity.noContent().build();
	}
}
