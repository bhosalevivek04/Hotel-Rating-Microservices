package com.vivek.user.service.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.vivek.user.service.entities.Hotel;
import com.vivek.user.service.entities.Rating;
import com.vivek.user.service.entities.User;
import com.vivek.user.service.exceptions.ResourceNotFoundException;
import com.vivek.user.service.external.services.HotelService;
import com.vivek.user.service.repositories.UserRepository;
import com.vivek.user.service.services.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;
	private final RestTemplate restTemplate;
	private final HotelService hotelService;
	private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
	
	@Override
	public User saveUser(User user) {
		// If userId is not provided, generate a new one
		// This allows AuthService to provide the userId during sync
		if (user.getUserId() == null || user.getUserId().isEmpty()) {
			String randomUserId = UUID.randomUUID().toString();
			user.setUserId(randomUserId);
		}
		return userRepository.save(user);
	}

	@Override
	public List<User> getAllUser() {
		List<User> users = userRepository.findAll();
		logger.info("Found {} users in database", users.size());

		// Fetch ratings for each user
		for (User user : users) {
			logger.info("Fetching ratings for user: {}", user.getUserId());
			String url = "http://RatingService/ratings/users/" + user.getUserId();
			Rating[] ratingsArray = restTemplate.getForObject(url, Rating[].class);
			
			List<Rating> ratings = ratingsArray != null ? Arrays.asList(ratingsArray) : new ArrayList<>();
			logger.info("Fetched {} ratings for user {}", ratings.size(), user.getUserId());
			
			// Fetch hotel details for each rating using Feign client
			List<Rating> ratingsWithHotels = ratings.stream().map(rating -> {
				logger.info("Fetching hotel for rating: {}", rating.getHotelId());
				Hotel hotel = hotelService.getHotel(rating.getHotelId());
				rating.setHotel(hotel);
				return rating;
			}).collect(Collectors.toList());
			
			user.setRatings(ratingsWithHotels);
		}

		return users;
	}

	@Override
	public List<User> getAllUserWithoutExternalCalls() {
		// Return users from database without fetching ratings/hotels
		return userRepository.findAll();
	}

	@Override
	public User getUser(String userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User with given id is not found on server"));

		// fetch rating of the above user from Rating Service
		String url = "http://RatingService/ratings/users/" + user.getUserId();
		logger.info("Calling Rating Service URL: {}", url);

		Rating[] ratingsArray = restTemplate.getForObject(url, Rating[].class);
		List<Rating> ratings = ratingsArray != null ? Arrays.asList(ratingsArray) : new ArrayList<>();

		logger.info("Fetched {} ratings for user {}", ratings.size(), userId);

		// Fetch hotel details for each rating using Feign client
		List<Rating> ratingsWithHotels = ratings.stream().map(rating -> {
			Hotel hotel = hotelService.getHotel(rating.getHotelId());
			rating.setHotel(hotel);
			return rating;
		}).collect(Collectors.toList());
		
		user.setRatings(ratingsWithHotels);

		return user;
	}

}
