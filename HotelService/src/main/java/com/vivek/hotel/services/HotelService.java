package com.vivek.hotel.services;

import java.util.List;

import com.vivek.hotel.entities.Hotel;

public interface HotelService {
	Hotel create(Hotel hotel);
	List<Hotel> getAll();
	Hotel get(String id);
	Hotel updateHotel(String hotelId, Hotel hotel);
	void deleteHotel(String hotelId);
}
