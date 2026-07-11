package com.hexaware.cozy_heaven.rest_controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexaware.cozy_heaven.dto.LoginRequestDTO;
import com.hexaware.cozy_heaven.dto.LoginResponseDTO;
import com.hexaware.cozy_heaven.model.Role;
import com.hexaware.cozy_heaven.repository.UserRepository;
import com.hexaware.cozy_heaven.security.CustomUserDetails;
import com.hexaware.cozy_heaven.security.JwtService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = {
	    "http://localhost:5173",
	    "https://d2xp6setbjof39.cloudfront.net",
	    "https://hariincode.github.io/cozy-heaven/",
	    "https://hariincode.github.io"
	})
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

	final AuthenticationManager authenticationManager;

	final JwtService jwtService;

	final UserRepository repo;

	AuthController(UserRepository repo, AuthenticationManager authenticationManager, JwtService jwtService) {
		this.repo = repo;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

		log.info("Username in AuthController: {}", userDetails.getUsername());

		String token = jwtService.generateToken(userDetails);
		LoginResponseDTO response = new LoginResponseDTO(token, userDetails.getUserId(), // already available!
				userDetails.getEmail(), userDetails.getUsername(), Role.valueOf(userDetails.getRole()));

		return ResponseEntity.ok(response);

	}

}
