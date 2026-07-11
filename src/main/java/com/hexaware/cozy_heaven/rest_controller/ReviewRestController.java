package com.hexaware.cozy_heaven.rest_controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexaware.cozy_heaven.dto.ReviewDTO;
import com.hexaware.cozy_heaven.dto.response.ReviewResponseDTO;
import com.hexaware.cozy_heaven.service.ReviewService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/review")
public class ReviewRestController {

	final ReviewService service;

	ReviewRestController(ReviewService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<ReviewResponseDTO> addReview(@Valid @RequestBody ReviewDTO reviewDTO) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.addReview(reviewDTO));
	}

	@PutMapping("/{reviewId}")
	public ResponseEntity<ReviewResponseDTO> updateReview(@PathVariable int reviewId, @Valid @RequestBody ReviewDTO reviewDTO) {
		return ResponseEntity.status(HttpStatus.OK).body(service.updateReview(reviewId, reviewDTO));
	}

	@GetMapping("/{reviewId}")
	public ResponseEntity<ReviewResponseDTO> getReviewById(@PathVariable int reviewId) {
		return ResponseEntity.status(HttpStatus.OK).body(service.getReviewById(reviewId));
	}

	@GetMapping
	public ResponseEntity<List<ReviewResponseDTO>> getAllReviews() {
		return ResponseEntity.status(HttpStatus.OK).body(service.getAllReviews());
	}

	@GetMapping("/hotel/{hotelId}")
	public ResponseEntity<List<ReviewResponseDTO>> getReviewsByHotel(@PathVariable int hotelId) {
		return ResponseEntity.status(HttpStatus.OK).body(service.getReviewsByHotel(hotelId));
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<ReviewResponseDTO>> getReviewsByUser(@PathVariable int userId) {
		return ResponseEntity.status(HttpStatus.OK).body(service.getReviewsByUser(userId));
	}

	@DeleteMapping("/{reviewId}")
	public ResponseEntity<String> deleteReview(@PathVariable int reviewId) {
		service.deleteReview(reviewId);
		return ResponseEntity.status(HttpStatus.OK).body("Review Deleted Successfully. Id: " + reviewId);
	}
}
