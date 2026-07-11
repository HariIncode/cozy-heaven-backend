package com.hexaware.cozy_heaven.rest_controller;

import java.time.LocalDate;
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

import com.hexaware.cozy_heaven.dto.RoomAvailableDTO;
import com.hexaware.cozy_heaven.dto.RoomDTO;
import com.hexaware.cozy_heaven.entity.Room;
import com.hexaware.cozy_heaven.service.RoomService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/rooms")
public class RoomRestController {

	final RoomService service;

	RoomRestController(RoomService service) {
		this.service = service;
	}
	
	@PostMapping("/hotel/{hotelId}")
	public ResponseEntity<Room> addRoom(@PathVariable int hotelId, @Valid @RequestBody RoomDTO roomDTO){
		return ResponseEntity.status(HttpStatus.CREATED).body(service.addRoom(hotelId, roomDTO));
	}
	
	@PutMapping("/{roomId}")
	public ResponseEntity<Room> updateRoom(@PathVariable int roomId, @Valid @RequestBody RoomDTO roomDTO){
		return ResponseEntity.status(HttpStatus.OK).body(service.updateRoom(roomId, roomDTO));
	}
	
	@GetMapping("/{roomId}")
	public ResponseEntity<Room> getRoomById(@PathVariable int roomId){
		return ResponseEntity.status(HttpStatus.OK).body(service.getRoomById(roomId));
	}
	
	@GetMapping
	public ResponseEntity<List<Room>> getAllRooms(){
		return ResponseEntity.status(HttpStatus.OK).body(service.getAllRooms());
	}
	
	@GetMapping("/hotel/{hotelId}")
	public ResponseEntity<List<Room>> getRoomsByHotel(@PathVariable int hotelId){
		return ResponseEntity.status(HttpStatus.OK).body(service.getRoomsByHotel(hotelId));
	}
	
	@PostMapping("/available")
	public ResponseEntity<List<Room>> getAvailableRooms(@Valid @RequestBody RoomAvailableDTO roomAvailableDTO){
		int hotelId = roomAvailableDTO.getHotelId();
		LocalDate checkIn = roomAvailableDTO.getCheckIn();
		LocalDate checkOut = roomAvailableDTO.getCheckOut();
		return ResponseEntity.status(HttpStatus.OK).body(service.getAvailableRooms(hotelId, checkIn, checkOut));
	}
	
	@DeleteMapping("/{roomId}")
	public ResponseEntity<String> deleteRoomById(@PathVariable int roomId){
		service.deleteRoom(roomId);
		return ResponseEntity.status(HttpStatus.OK).body("Room Deleted SuccessFully.");
	}
}
