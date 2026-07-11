package com.hexaware.cozy_heaven.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class RefundDTO {

    @NotNull(message = "Booking Id Required")
    private Integer bookingId;

    @NotNull(message = "Refund Percentage Required")
    private Integer refundPercentage;

    @NotBlank(message = "Reason Required")
    @Size(min = 10, message = "Reason must be at least 10 characters")
    private String reason;
}