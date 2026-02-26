package com.vivek.rating.services;

import java.util.List;

import com.vivek.rating.entities.Rating;

public interface RatingService {
	Rating create(Rating rating);
	List<Rating> getRatings();
	List<Rating> getRatingByUserId(String userId);
	List<Rating> getRatingByHotelId(String hotelId);
	Rating updateRating(String ratingId, Rating rating);
	void deleteRating(String ratingId);
}
