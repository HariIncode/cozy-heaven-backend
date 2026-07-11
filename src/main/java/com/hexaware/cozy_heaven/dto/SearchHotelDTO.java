package com.hexaware.cozy_heaven.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SearchHotelDTO {
	
	@NotBlank(message = "Location Required to Search Hotel")
	String location;
	
	@FutureOrPresent
	LocalDate checkIn;
	
	@Future
	LocalDate checkOut;

	int guest;

}
