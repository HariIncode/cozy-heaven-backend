package com.hexaware.cozy_heaven.dto;

import com.hexaware.cozy_heaven.model.PaymentStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePaymentStatusDTO {

	@NotNull(message = "Payment Id Reqiured")
	private int paymentId;
	
	@NotNull(message = "Payment Status Required")
	private PaymentStatus status;
	
}
