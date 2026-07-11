package com.hexaware.cozy_heaven.dto;

import com.hexaware.cozy_heaven.model.PaymentStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentDTO {

	@NotNull(message = "Booking Id Required")
	private int bookingId;
	@NotNull(message = "Enter The Amount Paid")
	private int amount;
	
	private PaymentStatus status = PaymentStatus.PENDING;

}
