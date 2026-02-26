package com.vivek.user.service.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Rating {
	private String ratingId;
	private String userId;
	private String hotelId;
	private String rating;
	private String feedback;

	private Hotel hotel;
}
