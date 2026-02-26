package com.vivek.rating.services.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vivek.rating.entities.Rating;
import com.vivek.rating.repository.RatingRepository;
import com.vivek.rating.services.RatingService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

	private final RatingRepository ratingRepository;

	@Override
	public Rating create(Rating rating) {
		String randomRatingId = UUID.randomUUID().toString();
		rating.setRatingId(randomRatingId);
		return ratingRepository.save(rating);
	}

	@Override
	public List<Rating> getRatings() {
		return ratingRepository.findAll();
	}

	@Override
	public List<Rating> getRatingByUserId(String userId) {
		return ratingRepository.findByUserId(userId);
	}

	@Override
	public List<Rating> getRatingByHotelId(String hotelId) {
		return ratingRepository.findByHotelId(hotelId);
	}

	@Override
	public Rating updateRating(String ratingId, Rating rating) {
		Rating existingRating = ratingRepository.findById(ratingId)
				.orElseThrow(() -> new RuntimeException("Rating not found with id: " + ratingId));
		
		existingRating.setUserId(rating.getUserId());
		existingRating.setHotelId(rating.getHotelId());
		existingRating.setRating(rating.getRating());
		existingRating.setFeedback(rating.getFeedback());
		
		return ratingRepository.save(existingRating);
	}

	@Override
	public void deleteRating(String ratingId) {
		ratingRepository.deleteById(ratingId);
	}

}
