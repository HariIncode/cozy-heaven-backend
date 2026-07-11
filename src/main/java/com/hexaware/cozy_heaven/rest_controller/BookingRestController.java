package com.hexaware.cozy_heaven.rest_controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hexaware.cozy_heaven.dto.BookingDTO;
import com.hexaware.cozy_heaven.dto.CancelBookingDTO;
import com.hexaware.cozy_heaven.dto.response.BookingResponseDTO;
import com.hexaware.cozy_heaven.service.BookingService;

import jakarta.validation.Valid;

@CrossOrigin(origins = {
	    "http://localhost:5173",
	    "https://d2xp6setbjof39.cloudfront.net",
	    "https://hariincode.github.io/cozy-heaven/",
	    "https://hariincode.github.io"
	})
@RestController
@RequestMapping("/booking")
public class BookingRestController {

	final BookingService service;

	BookingRestController(BookingService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<BookingResponseDTO> addBooking(@Valid @RequestBody BookingDTO bookingDTO) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.createBooking(bookingDTO));
	}

	@PostMapping("/cancel")
	public ResponseEntity<BookingResponseDTO> cancelBooking(@Valid @RequestBody CancelBookingDTO dto) {
		return ResponseEntity.status(HttpStatus.OK).body(service.cancelBooking(dto));
	}
	
	@PutMapping("/complete/{bookingId}")
	public ResponseEntity<BookingResponseDTO> completeBooking(@PathVariable int bookingId) {
		return ResponseEntity.status(HttpStatus.OK).body(service.completeBooking(bookingId));
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<BookingResponseDTO> getBooking(@PathVariable int bookingId) {
		return ResponseEntity.status(HttpStatus.OK).body(service.getBookingById(bookingId));
	}
	
	@PatchMapping("/noshow/{bookingId}")
	public ResponseEntity<BookingResponseDTO> markNoShow(@PathVariable int bookingId) {
	    return ResponseEntity.ok(service.markNoShow(bookingId));
	}

	@GetMapping
	public ResponseEntity<List<BookingResponseDTO>> getAllBooking() {
		return ResponseEntity.status(HttpStatus.OK).body(service.getAllBooking());
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<BookingResponseDTO>> getBookingByUser(@PathVariable int userId) {
		return ResponseEntity.status(HttpStatus.OK).body(service.getBookingsByUser(userId));
	}

	@GetMapping("/hotel/{hotelId}")
	public ResponseEntity<List<BookingResponseDTO>> getBookingByHotel(@PathVariable int hotelId) {
		return ResponseEntity.status(HttpStatus.OK).body(service.getBookingsByHotel(hotelId));
	}

	@DeleteMapping("{bookingId}")
	public ResponseEntity<String> deteleBooking(@PathVariable int bookingId) {
		service.deleteBooking(bookingId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Booking Deleted for Bookingid: " + bookingId);
	}
}
