package com.hexaware.cozy_heaven.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancelBookingDTO {

	@NotNull(message = "Booking Id required")
	private int bookingId;
	
	@NotBlank(message = "Reason Required")
	@Size(min = 10, max = 500, message = "Length of the reasom must between 10 to 500 characters")
	private String reason;
	
}
