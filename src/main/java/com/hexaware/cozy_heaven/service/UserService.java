package com.hexaware.cozy_heaven.service;

import java.util.List;

import com.hexaware.cozy_heaven.dto.UserDTO;
import com.hexaware.cozy_heaven.entity.User;

public interface UserService {

	User addUser(UserDTO dto);
	
	User updateUser(int id, UserDTO dto);
	
	User getUserById(int id);
	
	List<User> getAllUsers();
	
	boolean deleteUser(int id); 
	
	User getUserByEmail(String email);
	
}
