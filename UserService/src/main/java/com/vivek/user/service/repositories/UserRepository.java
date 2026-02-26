package com.vivek.user.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vivek.user.service.entities.User;

public interface UserRepository extends JpaRepository<User, String>{
	
}
