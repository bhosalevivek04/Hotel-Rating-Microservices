package com.vivek.hotel.services.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.vivek.hotel.entities.Hotel;
import com.vivek.hotel.exceptions.ResourceNotFoundException;
import com.vivek.hotel.repositories.HotelRepository;
import com.vivek.hotel.services.HotelService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

	private final HotelRepository hotelRepository;
	
	@Override
	public Hotel create(Hotel hotel) {
		String hotelId = UUID.randomUUID().toString();
		hotel.setId(hotelId);
		return hotelRepository.save(hotel);
	}

	@Override
	public List<Hotel> getAll() {
		return hotelRepository.findAll();
	}

	@Override
	public Hotel get(String id) {
		return hotelRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Hotel with given id not found"));
	}

	@Override
	public Hotel updateHotel(String hotelId, Hotel hotel) {
		Hotel existingHotel = hotelRepository.findById(hotelId)
				.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + hotelId));
		
		existingHotel.setName(hotel.getName());
		existingHotel.setLocation(hotel.getLocation());
		existingHotel.setAbout(hotel.getAbout());
		
		return hotelRepository.save(existingHotel);
	}

	@Override
	public void deleteHotel(String hotelId) {
		hotelRepository.deleteById(hotelId);
	}

}
