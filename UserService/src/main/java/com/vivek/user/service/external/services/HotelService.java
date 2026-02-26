package com.vivek.user.service.external.services;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.vivek.user.service.entities.Hotel;

@FeignClient(name = "HotelService")
public interface HotelService {
	
	@GetMapping("/hotels/{hotelId}")
	Hotel getHotel(@PathVariable String hotelId);
	
	@PostMapping("/hotels")
	ResponseEntity<Hotel> createHotel(@RequestBody Hotel hotel);
	
	@PutMapping("/hotels/{hotelId}")
	ResponseEntity<Hotel> updateHotel(@PathVariable String hotelId, @RequestBody Hotel hotel);
	
	@DeleteMapping("/hotels/{hotelId}")
	ResponseEntity<Void> deleteHotel(@PathVariable String hotelId);
}
