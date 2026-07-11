package com.hexaware.cozy_heaven.dto;

import com.hexaware.cozy_heaven.model.GuestType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GuestDTO {

	@NotNull(message = "Booking Id Required")
	private int bookingId;
	
	@NotBlank(message = "Name of The  Guest Required.")
	private String name;
	
	@NotNull(message = "Age of the Guest Required")
	private int age;
	
	private GuestType guestType;

}
