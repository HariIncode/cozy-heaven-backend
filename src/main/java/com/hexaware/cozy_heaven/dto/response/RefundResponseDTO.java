package com.hexaware.cozy_heaven.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.hexaware.cozy_heaven.model.RefundStatus;

import lombok.Data;

@Data
public class RefundResponseDTO {

    private Long refundId;
    private int refundPercentage;
    private int refundAmount;
    private RefundStatus refundStatus;
    private String reason;

    private LocalDateTime refundedAt; 
    private LocalDateTime processedAt;  

    private int bookingId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int originalAmount;

    private Integer userId;
    private String userName;
    private String userEmail;
    private String userContact;

    private Long paymentId;
    private String transactionId;
}
