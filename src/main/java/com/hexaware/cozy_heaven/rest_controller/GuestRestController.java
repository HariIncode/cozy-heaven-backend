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

import com.hexaware.cozy_heaven.dto.GuestDTO;
import com.hexaware.cozy_heaven.entity.Guest;
import com.hexaware.cozy_heaven.service.GuestService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/guests")
public class GuestRestController {
	
	final GuestService service;

	GuestRestController(GuestService service) {
		this.service = service;
	}
	
	@PostMapping("/{bookingId}")
	public ResponseEntity<Guest> addGuest(@PathVariable int bookingId,@Valid @RequestBody GuestDTO guestDto){
		return ResponseEntity.status(HttpStatus.CREATED).body(service.addGuest(bookingId, guestDto));
	}
	
	@PutMapping("/{guestId}")
	public ResponseEntity<Guest> updateGuest(@PathVariable int guestId,@Valid @RequestBody GuestDTO guestDto){
		return ResponseEntity.status(HttpStatus.OK).body(service.updateGuest(guestId, guestDto));
	}
	
	@GetMapping("/{guestId}")
	public ResponseEntity<Guest> getGuestById(@PathVariable int guestId){
		return ResponseEntity.status(HttpStatus.OK).body(service.getGuestById(guestId));
	}
	
	@GetMapping
	public ResponseEntity<List<Guest>> getAllGuest(){
		return ResponseEntity.status(HttpStatus.OK).body(service.getAllGuest());
	}
	
	@GetMapping("/booking/{bookingId}")
	public ResponseEntity<List<Guest>> getGuestByBooking(@PathVariable int bookingId){
		return ResponseEntity.status(HttpStatus.OK).body(service.getGuestByBooking(bookingId));
	}
	
	@DeleteMapping("/{guestId}")
	public ResponseEntity<String> deleteByGuestId(@PathVariable int guestId){
		service.deleteGuest(guestId);
		return ResponseEntity.status(HttpStatus.OK).body("Guest Deleted Successfully Id: " + guestId);
	}
}
