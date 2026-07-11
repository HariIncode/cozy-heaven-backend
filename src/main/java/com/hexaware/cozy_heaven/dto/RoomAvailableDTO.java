package com.hexaware.cozy_heaven.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomAvailableDTO {

	@NotNull(message = "Hotel Id Required")
	private int hotelId;
	@NotNull(message = "Room Id Required")
	private int roomId;
	
	@NotNull(message = "CheckIn Date Required")
	@FutureOrPresent(message = "Date Must be In future or Present")
	private LocalDate checkIn;
	
	@NotNull(message = "CheckOut Date Required")
	@Future(message = "Date Must be In future")
	private LocalDate checkOut;
	
}
