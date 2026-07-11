package com.hexaware.cozy_heaven.rest_controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hexaware.cozy_heaven.dto.UserDTO;
import com.hexaware.cozy_heaven.entity.User;
import com.hexaware.cozy_heaven.service.UserService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/users")
public class UserRestController {

	private UserService service;
	
	public UserRestController(UserService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<User> createUser(@Valid @RequestBody UserDTO userDTO) {

		User user = service.addUser(userDTO);

		return ResponseEntity.status(HttpStatus.CREATED).body(user);
	}


	@PreAuthorize("isAuthenticated()")
	@PutMapping("/{id}")
	public ResponseEntity<User> updateUser(@PathVariable int id, @Valid @RequestBody UserDTO userDTO) {

		User user = service.updateUser(id, userDTO);

		return ResponseEntity.ok(user);
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/{id}")
	public ResponseEntity<User> getUserById(@PathVariable int id) {

		return ResponseEntity.ok(service.getUserById(id));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/find")
	public ResponseEntity<User> getUserByEmail(@RequestParam String email) {

		return ResponseEntity.ok(service.getUserByEmail(email));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping
	public ResponseEntity<List<User>> getAllUsers() {

		return ResponseEntity.ok(service.getAllUsers());
	}


	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteUser(@PathVariable int id) {

		service.deleteUser(id);

		return ResponseEntity.ok("User Deleted Successfully.");
	}

}