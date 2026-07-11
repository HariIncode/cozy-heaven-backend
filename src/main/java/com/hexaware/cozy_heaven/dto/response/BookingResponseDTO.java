package com.hexaware.cozy_heaven.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.hexaware.cozy_heaven.model.BookingStatus;
import com.hexaware.cozy_heaven.model.PaymentStatus;

import lombok.Data;

@Data
public class BookingResponseDTO {

    private int bookingId;
    private BookingStatus status;

    // Stay details
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int totalNights;
    private int adults;
    private int childrens;
    private int totalAmount;

    // Cancellation info — null unless booking is CANCELLED
    private String cancellationReason;
    private LocalDateTime cancelledAt;

    // User info — denormalized
    private Integer userId;
    private String userName;
    private String userEmail;
    private String userContact;

    // Room info — denormalized
    private Integer roomId;
    private Integer roomNumber;
    private String roomType;

    // Hotel info — denormalized (admin needs this to know which property)
    private Integer hotelId;
    private String hotelName;
    private String hotelLocation;

    // Payment summary — admin needs to see payment state alongside booking
    private Long paymentId;
    private PaymentStatus paymentStatus;
    private String paymentMethod;
    private String transactionId;
    private int paymentAmount;

    // Guest list
    private List<GuestResponseDTO> guests;

    private LocalDateTime bookedAt;
}