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

import com.hexaware.cozy_heaven.dto.HotelDTO;
import com.hexaware.cozy_heaven.dto.SearchHotelDTO;
import com.hexaware.cozy_heaven.dto.response.HotelResponseDTO;
import com.hexaware.cozy_heaven.service.HotelService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/hotels")
@Slf4j
public class HotelRestController {

	final HotelService service;

	HotelRestController(HotelService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<HotelResponseDTO> createHotel(@Valid @RequestBody HotelDTO hotelDTO) {
		log.info("Inside Add Hotel");
		HotelResponseDTO hotel = service.addHotel(hotelDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(hotel);
	}

	@PutMapping("/{hotelId}")
	public ResponseEntity<HotelResponseDTO> updateHotel(@PathVariable int hotelId, @Valid @RequestBody HotelDTO hotelDTO) {
		HotelResponseDTO hotel = service.updateHotel(hotelId, hotelDTO);
		return ResponseEntity.status(HttpStatus.OK).body(hotel);
	}

	@GetMapping("/{hotelId}")
	public ResponseEntity<HotelResponseDTO> getHotelById(@PathVariable int hotelId) {
		HotelResponseDTO hotel = service.getHotelById(hotelId);
		return ResponseEntity.status(HttpStatus.OK).body(hotel);
	}

	@GetMapping
	public ResponseEntity<List<HotelResponseDTO>> getAllHotels() {
		List<HotelResponseDTO> hotels = service.getAllHotel();
		return ResponseEntity.status(HttpStatus.OK).body(hotels);
	}

	@GetMapping("/owner/{ownerId}")
	public ResponseEntity<List<HotelResponseDTO>> getHotelsByOwner(@PathVariable int ownerId) {
		List<HotelResponseDTO> hotels = service.getHotelsByOwner(ownerId);
		return ResponseEntity.status(HttpStatus.OK).body(hotels);
	}

	@PostMapping("/search")
	public ResponseEntity<List<HotelResponseDTO>> getHotelsByLoaction(@Valid @RequestBody SearchHotelDTO dto) {
		List<HotelResponseDTO> hotels = service.searchHotel(dto);
		return ResponseEntity.status(HttpStatus.OK).body(hotels);
	}

	@DeleteMapping("/{hotelId}")
	public ResponseEntity<String> deleteHotel(@PathVariable int hotelId) {
		service.deleteHotel(hotelId);
		return ResponseEntity.status(HttpStatus.OK).body("Hotel Deleted Successfylly");
	}
}
