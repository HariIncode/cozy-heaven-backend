package com.hexaware.cozy_heaven.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingDTO {

    @NotNull(message = "User Id Required")
    private Integer userId;

    @NotNull(message = "Room Id Required")
    private Integer roomId;

    @NotNull(message = "CheckIn Date Required")
    @FutureOrPresent(message = "Check-in must be today or a future date")
    private LocalDate checkIn;

    @NotNull(message = "CheckOut Date Required")
    @Future(message = "Check-out must be a future date")
    private LocalDate checkOut;

    @Min(value = 1, message = "At least 1 adult required")
    private int adults;

    @Min(value = 0, message = "Children count cannot be negative")
    private int childrens;

    // Optional — guests can also be added later via GuestService
    private List<GuestDTO> guestDetails;
}