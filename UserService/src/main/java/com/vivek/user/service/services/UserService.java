package com.vivek.user.service.services;

import java.util.*;

import com.vivek.user.service.entities.User;

public interface UserService {
	User saveUser(User user);
	List<User> getAllUser();
	List<User> getAllUserWithoutExternalCalls();
	User getUser(String userId);
}
