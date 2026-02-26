package com.vivek.hotel.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vivek.hotel.entities.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, String> {

}
