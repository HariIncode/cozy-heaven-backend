package com.hexaware.cozy_heaven.dto.response;

import java.time.LocalDateTime;

import com.hexaware.cozy_heaven.model.PaymentStatus;

import lombok.Data;

@Data
public class PaymentResponseDTO {

    private Long paymentId;
    private int amount;
    private PaymentStatus status;
    private String transactionId;
    private LocalDateTime paidAt;

    private int bookingId;
    private LocalDateTime bookingDate;
    private String hotelName;
    private String roomNumber;

    private Integer userId;
    private String userName;
    private String userEmail;
}
