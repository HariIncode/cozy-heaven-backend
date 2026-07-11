package com.hexaware.cozy_heaven.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hexaware.cozy_heaven.dto.UserDTO;
import com.hexaware.cozy_heaven.entity.User;
import com.hexaware.cozy_heaven.exception.UserException;
import com.hexaware.cozy_heaven.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

	final PasswordEncoder passwordEncoder;

	final UserRepository userRepo;

	UserServiceImpl(PasswordEncoder passwordEncoder, UserRepository userRepo) {
		this.passwordEncoder = passwordEncoder;
		this.userRepo = userRepo;
	}

	private void mapDtoToEntity(UserDTO dto, User user) {

		user.setEmail(dto.getEmail());

		if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
			user.setPassword(passwordEncoder.encode(dto.getPassword()));
		}

		user.setName(dto.getName());
		user.setContactNumber(dto.getContactNumber());
		user.setAddress(dto.getAddress());
		user.setGender(dto.getGender());
		user.setRole(dto.getRole());
	}

	@Override
	public User addUser(UserDTO dto) {

		String email = dto.getEmail();

		log.info("Creating user with email: {}", email);

		if (userRepo.existsByEmail(dto.getEmail())) {

			log.error("User already exists with email: {}", email);

			throw new UserException("User with same Email id Exist: " + email);
		}

		User user = new User();

		mapDtoToEntity(dto, user);

		User savedUser = userRepo.save(user);

		log.info("User created successfully with id: {}", user.getUserId());

		return savedUser;
	}

	@Override
	public User updateUser(int id, UserDTO dto) {

		log.info("Updating user with id: {}", id);

		User existingUser = getUserById(id);

		if (!existingUser.getEmail().equals(dto.getEmail()) && userRepo.existsByEmail(dto.getEmail())) {

			log.error("Email already exists: {}", dto.getEmail());
			throw new UserException("Email already exists: " + dto.getEmail());
		}

		mapDtoToEntity(dto, existingUser);

		User updatedUser = userRepo.save(existingUser);

		log.info("User updated successfully with id: {}", id);

		return updatedUser;
	}

//	Admin Only
	@Override
	public List<User> getAllUsers() {

		List<User> users = userRepo.findAll();

		log.info("Getting all Users from Database.");

		return users;
	}

	@Override
	public User getUserById(int id) {

		log.info("Fetching user with id: {}", id);

		return userRepo.findById(id).orElseThrow(() -> {
			log.error("User not found with id: {}", id);
			return new UserException("No User Found in the given Id: " + id);
		});
	}

	@Override
	public boolean deleteUser(int id) {

		User user = getUserById(id);

		log.info("Deleting User with id: {}", id);

		userRepo.delete(user);

		log.info("User with id: {} deleted successfully.", id);

		return true;
	}

	@Override
	public User getUserByEmail(String email) {
		log.info("Getting User Details with email {}", email);
		return userRepo.findByEmail(email).orElseThrow(() -> {
			log.error("User Not found for the given Email Id: {}", email);
			return new UserException("No User Found in the given Email id: " + email);
		});
	}

}
